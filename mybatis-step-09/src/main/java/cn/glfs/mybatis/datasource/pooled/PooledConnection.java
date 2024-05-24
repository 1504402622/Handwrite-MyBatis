package cn.glfs.mybatis.datasource.pooled;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 连接池对象
 */
public class PooledConnection implements InvocationHandler {

    private static final String CLOSE = "close";
    private static final Class<?>[] IFACES = new Class<?>[]{Connection.class};

    // 存储连接的哈希码，用于标识连接对象
    private int hashCode = 0;

    // 指向管理连接池的 PooledDataSource 对象，用于将连接返回给连接池或检查连接的有效性。
    private PooledDataSource dataSource;

    // 实际的 JDBC 连接对象，代表从数据库获取的真实连接
    private Connection realConnection;

    // 一个动态代理对象，拦截对 realConnection 的方法调用，以便在必要时将连接返回给连接池。
    private Connection proxyConnection;

    // 表示此连接从池中检出的时间戳，用于计算连接的使用时长
    private long checkoutTimestamp;

    // 表示此连接创建的时间戳，用于计算连接的年龄。
    private long createdTimestamp;

    // 表示此连接上次使用的时间戳，用于计算连接的空闲时长
    private long lastUsedTimestamp;

    // 表示连接类型的代码，可能用于标识不同类型的连接
    private int connectionTypeCode;

    // 布尔标志，指示连接是否有效，如果连接失效，则需要从连接池中移除
    private boolean valid;


    public PooledConnection(Connection connection, PooledDataSource dataSource) {
        this.hashCode = connection.hashCode();
        this.realConnection = connection;
        this.dataSource = dataSource;
        this.createdTimestamp = System.currentTimeMillis();
        this.lastUsedTimestamp = System.currentTimeMillis();
        this.valid = true;
        //这里的IFACES实际上是Connection接口数组
        this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);
    }

    // 实现了 InvocationHandler 接口的方法，用于拦截代理对象上的方法调用
    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        // 如果是调用 CLOSE 关闭链接方法，则将链接推送回连接池中，并返回null
        if(CLOSE.hashCode() == methodName.hashCode() && CLOSE.equals(methodName)){
            dataSource.pushConnection(this);
            return null;
        } else {
            // method.getDeclaringClass返回定义了方法的类的 Class 对象
            if(!Object.class.equals(method.getDeclaringClass())){
                // 除了toString()方法，其他方法调用之前要检查connection是否还是合法的,不合法要抛出SQLException
                checkConnection();
            }
            return method.invoke(realConnection,args);
        }
    }

    // 检查连接是否有效。如果无效，则抛出 SQLException
    private void checkConnection() throws SQLException {
        if (!valid) {
            throw new SQLException("Error accessing PooledConnection. Connection is invalid.");
        }
    }

    // 将连接标记为无效
    public void invalidate() {
        valid = false;
    }

    // 检查连接是否有效，并且底层的真实连接不为 null，并且对 ping 响应。
    public boolean isValid() {
        return valid && realConnection != null && dataSource.pingConnection(this);
    }



    public Connection getRealConnection() {
        return realConnection;
    }

    public Connection getProxyConnection() {
        return proxyConnection;
    }

    public int getRealHashCode() {
        return realConnection == null ? 0 : realConnection.hashCode();
    }

    public int getConnectionTypeCode() {
        return connectionTypeCode;
    }

    public void setConnectionTypeCode(int connectionTypeCode) {
        this.connectionTypeCode = connectionTypeCode;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getLastUsedTimestamp() {
        return lastUsedTimestamp;
    }

    public void setLastUsedTimestamp(long lastUsedTimestamp) {
        this.lastUsedTimestamp = lastUsedTimestamp;
    }

    public long getTimeElapsedSinceLastUse() {
        return System.currentTimeMillis() - lastUsedTimestamp;
    }

    public long getAge() {
        return System.currentTimeMillis() - createdTimestamp;
    }

    public long getCheckoutTimestamp() {
        return checkoutTimestamp;
    }

    public void setCheckoutTimestamp(long timestamp) {
        this.checkoutTimestamp = timestamp;
    }

    public long getCheckoutTime() {
        return System.currentTimeMillis() - checkoutTimestamp;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PooledConnection) {
            return realConnection.hashCode() == (((PooledConnection) obj).realConnection.hashCode());
        } else if (obj instanceof Connection) {
            return hashCode == obj.hashCode();
        } else {
            return false;
        }
    }
}


