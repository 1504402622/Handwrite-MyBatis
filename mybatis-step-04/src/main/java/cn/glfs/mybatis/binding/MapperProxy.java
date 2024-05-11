package cn.glfs.mybatis.binding;


import cn.glfs.mybatis.session.SqlSession;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class MapperProxy<T> implements InvocationHandler, Serializable {
    private static final long serialVersionUID = -6424540398559729838L;

    private SqlSession sqlSession;
    private final Class<T> mapperInterface;
    private final Map<Method,MapperMethod> methodCache;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if(Object.class.equals(method.getDeclaringClass())){
            return method.invoke(this,objects);
        }else {
            final MapperMethod mapperMethod = cachedMapperMethod(method);
            return mapperMethod.execute(sqlSession, objects);
        }
    }

    /**
     * 去缓存中寻找MapperMethod
     * @param method
     * @return
     */
    private MapperMethod cachedMapperMethod(Method method){
        MapperMethod mapperMethod = methodCache.get(method);
        if(mapperMethod == null){
            mapperMethod = new MapperMethod(mapperInterface,method,sqlSession.getConfiguration());
            methodCache.put(method,mapperMethod);
        }
        return mapperMethod;
    }
}
