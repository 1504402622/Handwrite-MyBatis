package cn.glfs.mybatis.session.defaults;

import cn.glfs.mybatis.binding.MapperRegistry;
import cn.glfs.mybatis.session.SqlSession;
import cn.glfs.mybatis.session.SqlSessionFactory;

public class DefaultSqlSessionFactory implements SqlSessionFactory {
    private final MapperRegistry mapperRegistry;
    public DefaultSqlSessionFactory(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(mapperRegistry);
    }
}
