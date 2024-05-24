package cn.glfs.mybatis.datasource.unpooled;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 无池化数据源实现
 */
public class UnpooledDataSource implements DataSource {
    // 类加载器，用于加载数据库驱动类。连接数据库
    private ClassLoader driverClassLoader;
    // 驱动配置属性，例如设置编码等
    private Properties driverProperties;
    // 驱动注册器，一个静态的 Map，用于保存已注册的数据库驱动。
    private static Map<String, Driver> registeredDrivers = new ConcurrentHashMap<>();
    // 数据库连接所需的驱动类名、连接地址、用户名和密码。
    private String driver;
    private String url;
    private String username;
    private String password;

    // 是否自动提交
    private Boolean autoCommit;
    // 默认的事务隔离级别。
    private Integer defaultTransactionIsolationLevel;

    //在类加载时，通过 DriverManager.getDrivers() 获取所有已注册的驱动，并将其存储在 registeredDrivers 中。
    static {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            registeredDrivers.put(driver.getClass().getName(), driver);
        }
    }

    /**
     * 一个简单的驱动代理类，用于封装真实的驱动对象。这样做的目的可能是为了方便后续的一些扩展或操作。
     */
    private static class DriverProxy implements Driver {

        private Driver driver;

        DriverProxy(Driver driver) {
            this.driver = driver;
        }

        @Override
        public Connection connect(String u, Properties p) throws SQLException {
            return this.driver.connect(u, p);
        }

        @Override
        public boolean acceptsURL(String u) throws SQLException {
            return this.driver.acceptsURL(u);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
            return this.driver.getPropertyInfo(u, p);
        }

        @Override
        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return this.driver.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        }
    }

    /**
     * 初始化数据库驱动。如果尚未注册指定的驱动，则通过反射加载驱动类并注册到 DriverManager 中。
     */
    private synchronized void initializerDriver() throws SQLException {
        if (!registeredDrivers.containsKey(driver)) {
            try {
                // 返回通过指定的类加载器加载的指定名称的类或接口的类对象。
                Class<?> driverType = Class.forName(driver, true, driverClassLoader);
                // 通过driver的类对象获取，获取驱动实例，数据库驱动程序（Database Driver）会与数据库建立连接
                Driver driverInstance = (Driver) driverType.newInstance();
                // 向DriverManager注册自己
                DriverManager.registerDriver(new DriverProxy(driverInstance));
                registeredDrivers.put(driver, driverInstance);
            } catch (Exception e) {
                throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e);
            }
        }
    }

    // 获取数据库连接的核心方法。它会根据传入的用户名、密码或属性来获取数据库连接
    // 并根据配置进行一些初始化设置，如自动提交事务和事务隔离级别等。
    private Connection doGetConnection(String username, String password) throws SQLException {
        Properties props = new Properties();
        if (driverProperties != null) {
            props.putAll(driverProperties);
        }
        if (username != null) {
            props.setProperty("user", username);
        }
        if (password != null) {
            props.setProperty("password", password);
        }
        return doGetConnection(props);
    }

    private Connection doGetConnection(Properties properties) throws SQLException {
        initializerDriver();

        Connection connection = DriverManager.getConnection(url, properties);
        if (autoCommit != null && autoCommit != connection.getAutoCommit()) {
            connection.setAutoCommit(autoCommit);
        }
        if (defaultTransactionIsolationLevel != null) {
            connection.setTransactionIsolation(defaultTransactionIsolationLevel);
        }
        return connection;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return doGetConnection(username, password);
    }

    // 这个方法允许获取数据源实现类的代理对象
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    // 检查数据源是否是由指定的类所实现或派生的
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }


    // 返回 DriverManager 的日志记录器，以便与 DriverManager 共享日志记录
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    // 将传入的 logWriter 设置为 DriverManager 的日志记录器，以便与 DriverManager 共享日志记录
    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        DriverManager.setLogWriter(logWriter);
    }

    // 登录超时时间是尝试建立数据库连接的最长时间
    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    // 返回 DriverManager 的登录超时时间
    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    // 回全局日志记录器
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public ClassLoader getDriverClassLoader() {
        return driverClassLoader;
    }

    public void setDriverClassLoader(ClassLoader driverClassLoader) {
        this.driverClassLoader = driverClassLoader;
    }

    public Properties getDriverProperties() {
        return driverProperties;
    }

    public void setDriverProperties(Properties driverProperties) {
        this.driverProperties = driverProperties;
    }

    public static Map<String, Driver> getRegisteredDrivers() {
        return registeredDrivers;
    }

    public static void setRegisteredDrivers(Map<String, Driver> registeredDrivers) {
        UnpooledDataSource.registeredDrivers = registeredDrivers;
    }

    public String getDriver() {
        return driver;
    }

    public synchronized void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(Boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public Integer getDefaultTransactionIsolationLevel() {
        return defaultTransactionIsolationLevel;
    }

    public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
        this.defaultTransactionIsolationLevel = defaultTransactionIsolationLevel;
    }

}
