package cn.glfs.mybatis.session.defaults;


import cn.glfs.mybatis.mapping.MappedStatement;
import cn.glfs.mybatis.session.Configuration;
import cn.glfs.mybatis.session.SqlSession;


public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }



    @Override
    public <T> T selectOne(String statement) {
        return (T)("你的操作被代理了！"+statement);
    }

    /**
     * 通过sql语句类获取sql语句，但并没有实际执行
     * @param statement
     * @param parameter
     * @return
     * @param <T>
     */
    @Override
    public <T> T selectOne(String statement, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        return (T) ("你的操作被代理了！" + "\n方法：" + statement + "\n入参：" + parameter + "\n待执行SQL：" + mappedStatement.getSql());
    }

    /**
     * 获取type所对应的代理对象
     * @param type
     * @return
     * @param <T>
     */
    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}