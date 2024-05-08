package cn.glfs.mybatis.binding;

import cn.glfs.mybatis.session.SqlSession;
import cn.hutool.core.lang.ClassScanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 接口-代理工厂映射器的注册类
 */
public class MapperRegistry {
    /**
     * 接口-代理工厂映射器
     * 将已添加的映射器代理加入到 HashMap(?可以放多种不同类型)
     * <?>是通配符泛型的写法，表示"任意类型"。在这段代码中，<?>表示MapperProxyFactory所接受的参数类型是未知的
     */
    private final Map<Class<?>,MapperProxyFactory<?>> knownMappers = new HashMap<>();

    /**
     * 使用该类型的接口所对应的代理工厂去创建代理对象
     * @param type
     * @param sqlSession
     * @return 代理对象
     * @param <T>
     */
    public <T> T getMapper(Class<T> type, SqlSession sqlSession){
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null){
            throw new RuntimeException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e){
            throw new RuntimeException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    /**
     * 向接口-代理工厂映射器添加（接口，代理工厂）键值数据
     * @param type
     * @param <T>
     */
    public <T> void addMapper(Class<T> type){
        /**Mapper 必须是接口才会注册 */
        if(type.isInterface()){
            if(hasMapper(type)) throw new RuntimeException("Type " + type + " is already known to the MapperRegistry.");
            knownMappers.put(type,new MapperProxyFactory<>(type));
        }
    }

    /**
     * 向接口-代理工厂映射器添加（接口，代理工厂）一个包下的所有接口的键值数据
     * @param packageName
     */
    public void addMappers(String packageName) {
        /** 扫描该包下所有类 */
        Set<Class<?>> mapperSet = ClassScanner.scanPackage(packageName);
        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }


}
