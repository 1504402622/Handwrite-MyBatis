package cn.glfs.mybatis.datasource.pooled;

import cn.glfs.mybatis.datasource.unpooled.UnpooledDataSource;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.logging.Logger;

/**
 * 有连接池的数据源
 */
public class PooledDataSource implements DataSource {

    // 用于记录日志的 Logger 对象。
    private org.slf4j.Logger logger = LoggerFactory.getLogger(PooledDataSource.class);

    // 用于维护连接池的状态信息。
    private final  PoolState state = new PoolState(this);

    // 管理连接池的基本数据源
    private final UnpooledDataSource dataSource;

    // 活跃连接数
    protected int poolMaximumActiveConnections = 10;
    // 空闲连接数
    protected int poolMaximumIdleConnections = 5;

    // 连接从池中被借出后的最大允许活跃时间
    // 参数定义了连接在从池中被借出后，被允许处于活跃状态的最长时间。在这段时间内，连接被认为是有效的，可以被使用。当这个时间到期时，
    // 如果连接还未被返回到池中，连接会被强制回收或关闭，以确保连接资源的有效利用和池的稳定性。
    protected int poolMaximumCheckoutTime = 20000;

    // 如果连接池中没有可用连接，应用程序将等待的最长时间。
    // 当连接池中的所有连接都被借出且正在被使用时，新的连接请求可能会被阻塞，直到有连接可用或者超过了设置的等待时间。
    protected int poolTimeToWait = 20000;

    // 用于连接健康检查的查询语句
    // 在数据库连接池中，连接的有效性需要定期检查，以确保连接仍然可用。这
    // 种检查通常通过向数据库发送一个简单的查询来完成，如果数据库成功响应，则说明连接仍然有效。这个查询就是所谓的“侦测查询”或“ping 查询”。
    // "NO PING QUERY SET" 意味着数据库连接池没有设置任何侦测查询，用来检测连接的有效性。换句话说，连接池在验证连接是否有效时没有发送任何查询给数据库。
    // 这可能导致一些问题，因为没有检查连接的有效性，连接池可能会持续使用失效的连接，导致应用程序出现错误或性能下降
    protected String poolPingQuery = "NO PING QUERY SET";

    //  是否开启连接健康检查
    // 开启或禁用侦测查询
    protected boolean poolPingEnabled = false;

    // 指定多久未使用的连接执行一次健康检查
    // 用来配置 poolPingQuery 多次时间被用一次
    protected int poolPingConnectionsNotUsedFor = 0;

    // 期望的连接类型码,实际上就是url，username，password字符串的hashcode
    //这个变量的作用是用于标识期望的连接类型码。
    // 在连接池中，当从连接池中获取连接时，会检查连接的类型码是否与期望的连接类型码一致，如果不一致，则会重新创建新的连接。实际上就是url，username，password字符串的hashcode
    private int expectedConnectionTypeCode;

    public PooledDataSource() {
        this.dataSource = new UnpooledDataSource();
    }


    // pushConnection方法用于将连接推送回连接池
    protected void pushConnection(PooledConnection connection) throws SQLException {
        synchronized (state) {
            // 从活动连接中移除当前连接
            state.activeConnections.remove(connection);

            // 判断连接是否有效
            if (connection.isValid()) {
                // 如果空闲连接数小于最大允许空闲连接数，并且连接类型与预期连接类型一致
                if (state.idleConnections.size() < poolMaximumIdleConnections && connection.getConnectionTypeCode() == expectedConnectionTypeCode) {
                    // 累加连接被借出的时间
                    state.accumulatedCheckoutTime += connection.getCheckoutTime();
                    if (!connection.getRealConnection().getAutoCommit()) {
                        // 如果连接未自动提交，则回滚事务
                        connection.getRealConnection().rollback();
                    }
                    // 实例化一个新的连接，加入到空闲连接列表中
                    PooledConnection newConnection = new PooledConnection(connection.getRealConnection(), this);
                    state.idleConnections.add(newConnection);
                    newConnection.setCreatedTimestamp(connection.getCreatedTimestamp());
                    newConnection.setLastUsedTimestamp(connection.getLastUsedTimestamp());
                    connection.invalidate();
                    logger.info("Returned connection " + newConnection.getRealHashCode() + " to pool.");

                    // 通知其他线程可以来抢占连接
                    state.notifyAll();
                } else {
                    // 否则，空闲连接充足，关闭当前连接
                    state.accumulatedCheckoutTime += connection.getCheckoutTime();
                    if (!connection.getRealConnection().getAutoCommit()) {
                        connection.getRealConnection().rollback();
                    }
                    // 关闭连接
                    connection.getRealConnection().close();
                    logger.info("Closed connection " + connection.getRealHashCode() + ".");
                    connection.invalidate();
                }
            } else {
                // 如果连接无效，则丢弃连接
                logger.info("A bad connection (" + connection.getRealHashCode() + ") attempted to return to the pool, discarding connection.");
                state.badConnectionCount++;
            }
        }
    }


    // popConnection方法用于从连接池中获取连接
    private PooledConnection popConnection(String username, String password) throws SQLException {
        boolean countedWait = false; // 标记是否已经计数等待时间
        PooledConnection conn = null; // 初始化连接为null
        long t = System.currentTimeMillis(); // 记录方法开始时间
        int localBadConnectionCount = 0; // 本地失败连接计数

        while (conn == null) { // 当连接为null时，继续循环
            synchronized (state) { // 使用同步块，保证线程安全
                // 如果有空闲连接，则直接从空闲连接中取出
                if (!state.idleConnections.isEmpty()) {
                    conn = state.idleConnections.remove(0); // 移除空闲连接列表的第一个连接
                    logger.info("Checked out connection " + conn.getRealHashCode() + " from pool."); // 记录连接被取出的日志
                } else { // 如果没有空闲连接，则创建新连接或等待可用连接
                    // 如果活跃连接数未达到最大限制，则创建新连接
                    if (state.activeConnections.size() < poolMaximumActiveConnections) {
                        // 通过驱动创建一个新的连接
                        conn = new PooledConnection(dataSource.getConnection(), this); // 创建新连接
                        logger.info("Created connection " + conn.getRealHashCode() + "."); // 记录新连接被创建的日志
                    } else { // 如果活跃连接数已达到最大限制，则考虑等待或关闭空闲连接
                        PooledConnection oldestActiveConnection = state.activeConnections.get(0); // 获取最老的活跃连接
                        long longestCheckoutTime = oldestActiveConnection.getCheckoutTime(); // 获取最老连接的检出时间

                        // 如果最老连接的检出时间超过最大检出时间，则认为连接过期
                        if (longestCheckoutTime > poolMaximumCheckoutTime) {
                            state.claimedOverdueConnectionCount++; // 统计过期连接的数量
                            state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime; // 统计过期连接的累计检出时间
                            state.accumulatedCheckoutTime += longestCheckoutTime; // 统计总的累计检出时间
                            state.activeConnections.remove(oldestActiveConnection); // 移除最老的活跃连接

                            // 回滚并重新实例化一个新连接
                            if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {
                                // 在连接池中，当一个连接被重新实例化为一个新连接时，需要确保这个新连接处于一个干净的状态，即没有未提交的事务
                                oldestActiveConnection.getRealConnection().rollback();
                            }
                            conn = new PooledConnection(oldestActiveConnection.getRealConnection(), this); // 创建新连接
                            oldestActiveConnection.invalidate(); // 标记最老连接为失效
                            logger.info("Claimed overdue connection " + conn.getRealHashCode() + "."); // 记录过期连接被取出的日志
                        } else { // 如果最老连接的检出时间未超过最大检出时间，则等待可用连接
                            try {
                                if (!countedWait) { // 如果未计数等待时间，则计数
                                    state.hadToWaitCount++; // 统计等待次数
                                    countedWait = true; // 标记已计数等待时间
                                }
                                logger.info("Waiting as long as " + poolTimeToWait + " milliseconds for connection."); // 记录等待连接的日志
                                long wt = System.currentTimeMillis();
                                state.wait(poolTimeToWait); // 等待指定的时间
                                state.accumulatedWaitTime += System.currentTimeMillis() - wt; // 统计累计等待时间
                            } catch (InterruptedException e) {
                                break; // 捕获中断异常，结束等待
                            }
                        }
                    }

                    // 如果获取到连接，则进行相关处理
                    if (conn != null) {
                        if (conn.isValid()) { // 如果连接有效
                            if (!conn.getRealConnection().getAutoCommit()) {
                                conn.getRealConnection().rollback(); // 回滚未提交的事务
                            }
                            conn.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(), username, password)); // 设置连接类型码
                            conn.setCheckoutTimestamp(System.currentTimeMillis()); // 记录连接的检出时间
                            conn.setLastUsedTimestamp(System.currentTimeMillis()); // 记录连接的最后使用时间
                            state.activeConnections.add(conn); // 将连接添加到活跃连接列表
                            state.requestCount++; // 统计连接请求次数
                            state.accumulatedRequestTime += System.currentTimeMillis() - t; // 统计累计请求时间
                        } else { // 如果连接无效
                            logger.info("A bad connection (" + conn.getRealHashCode() + ") was returned from the pool, getting another connection."); // 记录无效连接的日志
                            state.badConnectionCount++; // 统计无效连接的数量
                            localBadConnectionCount++; // 本地统计无效连接的数量
                            conn = null; // 将连接置为null
                            // 如果本地统计的无效连接数量超过限制，则抛出异常
                            if (localBadConnectionCount > (poolMaximumIdleConnections + 3)) {
                                logger.debug("PooledDataSource: Could not get a good connection to the database.");
                                throw new SQLException("PooledDataSource: Could not get a good connection to the database.");
                            }
                        }
                    }

                }
            }
        }
        return conn; // 返回获取到的连接
    }


    /**
     * 强制关闭所有连接
     */
    public void forceCloseAll() {
        synchronized (state) {
            expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
            // 关闭活跃链接
            for (int i = state.activeConnections.size(); i > 0; i--) {
                try {
                    PooledConnection conn = state.activeConnections.remove(i - 1);
                    conn.invalidate();

                    Connection realConn = conn.getRealConnection();
                    if (!realConn.getAutoCommit()) {
                        realConn.rollback();
                    }
                    realConn.close();
                } catch (Exception ignore) {

                }
            }
            // 关闭空闲链接
            for (int i = state.idleConnections.size(); i > 0; i--) {
                try {
                    PooledConnection conn = state.idleConnections.remove(i - 1);
                    conn.invalidate();

                    Connection realConn = conn.getRealConnection();
                    if (!realConn.getAutoCommit()) {
                        realConn.rollback();
                    }
                } catch (Exception ignore) {

                }
            }
            logger.info("PooledDataSource forcefully closed/removed all connections.");
        }
    }

    /**
     * 对连接进行健康检查
     * @param conn
     * @return
     */
    protected boolean pingConnection(PooledConnection conn) {
        boolean result = true;

        try {
            result = !conn.getRealConnection().isClosed();
        } catch (SQLException e) {
            logger.info("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
            result = false;
        }

        if (result) {
            if (poolPingEnabled) {
                if (poolPingConnectionsNotUsedFor >= 0 && conn.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor) {
                    try {
                        logger.info("Testing connection " + conn.getRealHashCode() + " ...");
                        Connection realConn = conn.getRealConnection();
                        Statement statement = realConn.createStatement();
                        ResultSet resultSet = statement.executeQuery(poolPingQuery);
                        resultSet.close();
                        if (!realConn.getAutoCommit()) {
                            realConn.rollback();
                        }
                        result = true;
                        logger.info("Connection " + conn.getRealHashCode() + " is GOOD!");
                    } catch (Exception e) {
                        logger.info("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
                        try {
                            conn.getRealConnection().close();
                        } catch (SQLException ignore) {
                        }
                        result = false;
                        logger.info("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
                    }
                }
            }
        }

        return result;
    }

    // 如果连接是代理对象，则返回其真实连接
    public static Connection unwrapConnection(Connection conn) {
        if (Proxy.isProxyClass(conn.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(conn);
            if (handler instanceof PooledConnection) {
                return ((PooledConnection) handler).getRealConnection();
            }
        }
        return conn;
    }

    // 组装连接类型码
    private int assembleConnectionTypeCode(String url, String username, String password) {
        return ("" + url + username + password).hashCode();
    }

    // 获取连接
    @Override
    public Connection getConnection() throws SQLException {
        return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
    }

    // 获取连接
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return popConnection(username, password).getProxyConnection();
    }

    // 在对象被垃圾回收之前，强制关闭所有连接
    protected void finalize() throws Throwable {
        forceCloseAll();
        super.finalize();
    }

    // 抛出异常，该类不是包装器
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    // 返回 false，该类不是包装器
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    // 获取日志写入器
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    // 设置日志写入器
    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        DriverManager.setLogWriter(logWriter);
    }

    // 设置登录超时时间
    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    // 获取登录超时时间
    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    // 获取父级记录器
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public void setDriver(String driver) {
        dataSource.setDriver(driver);
        forceCloseAll();
    }

    public void setUrl(String url) {
        dataSource.setUrl(url);
        forceCloseAll();
    }

    public void setUsername(String username) {
        dataSource.setUsername(username);
        forceCloseAll();
    }

    public void setPassword(String password) {
        dataSource.setPassword(password);
        forceCloseAll();
    }


    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        dataSource.setAutoCommit(defaultAutoCommit);
        forceCloseAll();
    }

    public int getPoolMaximumActiveConnections() {
        return poolMaximumActiveConnections;
    }

    public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
        this.poolMaximumActiveConnections = poolMaximumActiveConnections;
    }

    public int getPoolMaximumIdleConnections() {
        return poolMaximumIdleConnections;
    }

    public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
        this.poolMaximumIdleConnections = poolMaximumIdleConnections;
    }

    public int getPoolMaximumCheckoutTime() {
        return poolMaximumCheckoutTime;
    }

    public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
        this.poolMaximumCheckoutTime = poolMaximumCheckoutTime;
    }

    public int getPoolTimeToWait() {
        return poolTimeToWait;
    }

    public void setPoolTimeToWait(int poolTimeToWait) {
        this.poolTimeToWait = poolTimeToWait;
    }

    public String getPoolPingQuery() {
        return poolPingQuery;
    }

    public void setPoolPingQuery(String poolPingQuery) {
        this.poolPingQuery = poolPingQuery;
    }

    public boolean isPoolPingEnabled() {
        return poolPingEnabled;
    }

    public void setPoolPingEnabled(boolean poolPingEnabled) {
        this.poolPingEnabled = poolPingEnabled;
    }

    public int getPoolPingConnectionsNotUsedFor() {
        return poolPingConnectionsNotUsedFor;
    }

    public void setPoolPingConnectionsNotUsedFor(int poolPingConnectionsNotUsedFor) {
        this.poolPingConnectionsNotUsedFor = poolPingConnectionsNotUsedFor;
    }

    public int getExpectedConnectionTypeCode() {
        return expectedConnectionTypeCode;
    }

    public void setExpectedConnectionTypeCode(int expectedConnectionTypeCode) {
        this.expectedConnectionTypeCode = expectedConnectionTypeCode;
    }

}
