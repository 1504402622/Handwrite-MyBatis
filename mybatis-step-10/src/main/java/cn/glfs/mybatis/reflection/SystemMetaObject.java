package cn.glfs.mybatis.reflection;


import cn.glfs.mybatis.reflection.factory.DefaultObjectFactory;
import cn.glfs.mybatis.reflection.factory.ObjectFactory;
import cn.glfs.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import cn.glfs.mybatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * 系统级别元对象
 * 元对象（MetaObject）通常是指在程序中表示其他对象的对象,封装其他对象的对象
 * 包装器（Wrapper）是一种设计模式，用于将某个对象或者函数封装在另一个对象或者函数中，以提供额外的功能或者修改原始对象的行为，而不需要修改其原始代码
 */
public class SystemMetaObject {

    // 可能是用于提供默认的对象工厂
    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    // 可能是用于提供默认的对象包装器工厂
    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY =new DefaultObjectWrapperFactory();
    // 一个空的元对象
    public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(NullObject.class, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);

    private SystemMetaObject() {
        // Prevent Instantiation of Static Class
    }

    /**
     * 空对象
     */
    private static class NullObject {
    }

    public static MetaObject forObject(Object object) {
        return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
    }
}
