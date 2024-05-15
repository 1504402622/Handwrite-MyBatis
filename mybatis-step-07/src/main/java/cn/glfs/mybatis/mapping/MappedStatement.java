package cn.glfs.mybatis.mapping;


import cn.glfs.mybatis.session.Configuration;

/**
 * sql语句类
 */
public class MappedStatement {

    /** （含代理对象的注册类、接口方法id-sql语句类映射器）配置类 */
    private Configuration configuration;
    /** 这里的id就是接口的全列名+.方法名 */
    private String id;
    /** SQL语句类型 */
    private SqlCommandType sqlCommandType;
    private BoundSql boundSql;


    MappedStatement() {
        // constructor disabled
    }

    /**
     * 建造者
     * @return
     */
    public static class Builder{
        private MappedStatement mappedStatement = new MappedStatement();
        public Builder(Configuration configuration, String id, SqlCommandType sqlCommandType, BoundSql boundSql) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.boundSql = boundSql;
        }

        public MappedStatement build() {
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            return mappedStatement;
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public void setSqlCommandType(SqlCommandType sqlCommandType) {
        this.sqlCommandType = sqlCommandType;
    }

    public BoundSql getBoundSql() {
        return boundSql;
    }
}
