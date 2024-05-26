package cn.glfs.mybatis.reflection.wrapper;

import cn.glfs.mybatis.reflection.MetaObject;

/**
 * 默认对象包装工厂
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory {

    /**
     * 判断有没有包装器
     */
    @Override
    public boolean hasWrapperFor(Object object) {
        return false;
    }

    /**
     * 得到包装器
     */
    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        throw new RuntimeException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
    }
}
