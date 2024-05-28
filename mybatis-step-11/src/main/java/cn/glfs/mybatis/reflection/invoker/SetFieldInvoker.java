package cn.glfs.mybatis.reflection.invoker;

import java.lang.reflect.Field;

/**
 * 不通过setter方法设置值而是直接设置值，属性赋值调用者
 */
public class SetFieldInvoker implements Invoker {

    //某个对象的属性
    private Field field;
    public SetFieldInvoker(Field field){
        this.field = field;
    }


    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        //将target对象中的field字段的值设置为20
        field.set(target, args[0]);
        return null;
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}