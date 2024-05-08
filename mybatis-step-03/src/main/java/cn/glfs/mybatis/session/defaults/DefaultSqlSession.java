package cn.glfs.mybatis.session.defaults;

import cn.glfs.mybatis.binding.MapperRegistry;
import cn.glfs.mybatis.session.SqlSession;

public class DefaultSqlSession implements SqlSession {
    /**
     * 接口-代理工厂映射器的注册类
     */
    private MapperRegistry mapperRegistry;

    public DefaultSqlSession(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    @Override
    public <T> T selectOne(String statement) {
        return (T)("你的操作被代理了！"+statement);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return (T) ("你的操作被代理了！" + "方法：" + statement + " 入参：" + parameter);
    }

    /**
     * 获取type所对应的代理对象
     * @param type
     * @return
     * @param <T>
     */
    @Override
    public <T> T getMapper(Class<T> type) {
        return mapperRegistry.getMapper(type,this);
    }
}
