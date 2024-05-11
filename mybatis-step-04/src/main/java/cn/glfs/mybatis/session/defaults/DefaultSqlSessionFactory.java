package cn.glfs.mybatis.session.defaults;


import cn.glfs.mybatis.session.Configuration;
import cn.glfs.mybatis.session.SqlSession;
import cn.glfs.mybatis.session.SqlSessionFactory;

/**
 * 默认的DefaultSqlSessionFactory工厂
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {


    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(configuration);
    }

}
