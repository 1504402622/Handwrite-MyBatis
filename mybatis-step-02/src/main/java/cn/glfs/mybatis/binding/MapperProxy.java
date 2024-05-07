package cn.glfs.mybatis.binding;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *  代理类
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {
    private static final long serialVersionUID = -6424540398559729838L;
    private Map<String,String> sqlSession;
    //代理接口
    private final Class<T> mapperInterface;

    public MapperProxy(Map<String, String> sqlSession,Class<T> mapperInterface) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Object invoke(Object o, Method method, Object[]  args) throws Throwable {
        //method.getDeclaringClass() 返回的是声明了当前方法的类或接口的 Class 对象,
        //据传入的Method对象获取当前方法所在的类或接口的Class对象。如果当前方法所在的类或接口是Object类，则直接调用被代理对象的方法
        if(Object.class.equals(method.getDeclaringClass())){
            return method.invoke(this, args);
        }else {
            return "你的被代理了！" + sqlSession.get(mapperInterface.getName() + "." + method.getName());
        }
    }
}
