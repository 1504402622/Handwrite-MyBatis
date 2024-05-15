package cn.glfs.mybatis.binding;


import cn.glfs.mybatis.session.Configuration;
import cn.glfs.mybatis.session.SqlSession;
import cn.hutool.core.lang.ClassScanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * （含接口-代理工厂映射器）代理对象的注册类
 */
public class MapperRegistry {
    private Configuration configuration;
    public MapperRegistry(Configuration configuration){
        this.configuration = configuration;
    }

    /**
     * 接口-代理工厂映射器
     */
    private final Map<Class<?>,MapperProxyFactory> knownMappers = new HashMap<>();

    public <T> T getMapper(Class<T> type, SqlSession sqlSession){
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if(mapperProxyFactory == null){
            throw new RuntimeException("Type "+type+" is not known to the MapperRegistry.");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        }catch (Exception e){
            throw new RuntimeException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    /**
     * 加入到接口-代理工厂映射器
     * @param type
     * @param <T>
     */
    public <T> void addMapper(Class<T> type){
        if(type.isInterface()){
            if(hasMapper(type)){
                throw new RuntimeException("Type " + type + " is already known to the MapperRegistry.");
            }
            // 注册映射器代理工厂
            knownMappers.put(type, new MapperProxyFactory<>(type));
        }
    }

    /**
     * 将一个包下的所有接口都加入到接口-代理工厂映射器
     * @param packageName
     */
    public void addMappers(String packageName) {
        Set<Class<?>> mapperSet = ClassScanner.scanPackage(packageName);
        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }

    /**
     * 判断接口-代理工厂映射器中是否含有该接口
     * @param type
     * @return
     * @param <T>
     */
    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }
}
