package cn.glfs.mybatis.reflection.invoker;

import java.lang.reflect.Field;

/**
 * 不通过getter方法得到值而是直接得到值，属性取值调用者
 */
public class GetFieldInvoker implements Invoker {
    private Field field;
    public GetFieldInvoker(Field field) {
        this.field = field;
    }
    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        return field.get(target);
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
