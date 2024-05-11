package cn.glfs.mybatis.mapping;


import cn.glfs.mybatis.session.Configuration;

import java.util.Map;

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
    /** 参数类型 */
    private String parameterType;
    /** 结果类型 */
    private String resultType;
    /** sql语句 */
    private String sql;
    /** 参数映射器（1：第一个参数名，2：第二个参数名） 与po对接 */
    private Map<Integer, String> parameter;

    MappedStatement() {
        // constructor disabled
    }

    /**
     * 建造者
     * @return
     */
    public static class Builder{
        private MappedStatement mappedStatement = new MappedStatement();
        public Builder(Configuration configuration,String id,SqlCommandType sqlCommandType,String parameterType, String resultType, String sql, Map<Integer, String> parameter){
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.parameterType = parameterType;
            mappedStatement.resultType = resultType;
            mappedStatement.sql = sql;
            mappedStatement.parameter = parameter;
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

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Map<Integer, String> getParameter() {
        return parameter;
    }

    public void setParameter(Map<Integer, String> parameter) {
        this.parameter = parameter;
    }

}
