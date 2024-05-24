package cn.glfs.mybatis.reflection;


import cn.glfs.mybatis.reflection.invoker.GetFieldInvoker;
import cn.glfs.mybatis.reflection.invoker.Invoker;
import cn.glfs.mybatis.reflection.invoker.MethodInvoker;
import cn.glfs.mybatis.reflection.property.PropertyTokenizer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * 元类
 */
public class MetaClass {

    private Reflector reflector;


    private MetaClass(Class<?> type) {
        this.reflector = Reflector.forClass(type);
    }

    // 获取反射器
    public static MetaClass forClass(Class<?> type) {
        return new MetaClass(type);
    }

    // 查看反射器缓存是否可用
    public static boolean isClassCacheEnabled() {
        return Reflector.isClassCacheEnabled();
    }

    // 设置反射器缓存是否可用
    public static void setClassCacheEnabled(boolean classCacheEnabled) {
        Reflector.setClassCacheEnabled(classCacheEnabled);
    }

    // 获取反射器中该字段对应的类型，并创建一个该类的映射器
    public MetaClass metaClassForProperty(String name) {
        Class<?> propType = reflector.getGetterType(name);
        return MetaClass.forClass(propType);
    }

    // 根据属性路径查找字段名称，可以选择是否使用驼峰命名法。可能是school_student_rule，我要在映射器中找到最终的属性名rule
    public String findProperty(String name, boolean useCamelCaseMapping) {
        if (useCamelCaseMapping) {
            name = name.replace("_", "");
        }
        return findProperty(name);
    }

    public String findProperty(String name) {
        StringBuilder prop = buildProperty(name, new StringBuilder());
        return prop.length() > 0 ? prop.toString() : null;
    }

    private StringBuilder buildProperty(String name, StringBuilder builder) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            // 获取该字段大写对应的原本形式
            String propertyName = reflector.findPropertyName(prop.getName());
            if (propertyName != null) {
                builder.append(propertyName);
                builder.append(".");
                // 创建一个基于该propertyName属性的元类
                MetaClass metaProp = metaClassForProperty(propertyName);
                metaProp.buildProperty(prop.getChildren(), builder);
            }
        } else {
            String propertyName = reflector.findPropertyName(name);
            if (propertyName != null) {
                builder.append(propertyName);
            }
        }
        return builder;
    }


    // 获取反射器可读字段名
    public String[] getGetterNames() {
        return reflector.getGetablePropertyNames();
    }
    // 获取反射器可写字段名
    public String[] getSetterNames() {
        return reflector.getSetablePropertyNames();
    }

    // 根据属性名获取对应的 setter 方法的参数类型
    public Class<?> getSetterType(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaClass metaProp = metaClassForProperty(prop.getName());
            return metaProp.getSetterType(prop.getChildren());
        } else {
            return reflector.getSetterType(prop.getName());
        }
    }

    // 根据属性名获取对应的 getter 方法的返回值类型
    public Class<?> getGetterType(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaClass metaProp = metaClassForProperty(prop);
            return metaProp.getGetterType(prop.getChildren());
        }
        // issue #506. Resolve the type inside a Collection Object
        return getGetterType(prop);
    }

    private MetaClass metaClassForProperty(PropertyTokenizer prop) {
        Class<?> propType = getGetterType(prop);
        return MetaClass.forClass(propType);
    }

    private Class<?> getGetterType(PropertyTokenizer prop) {
        Class<?> type = reflector.getGetterType(prop.getName());
        if (prop.getIndex() != null && Collection.class.isAssignableFrom(type)) {
            Type returnType = getGenericGetterType(prop.getName());
            if (returnType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnType = actualTypeArguments[0];
                    if (returnType instanceof Class) {
                        type = (Class<?>) returnType;
                    } else if (returnType instanceof ParameterizedType) {
                        type = (Class<?>) ((ParameterizedType) returnType).getRawType();
                    }
                }
            }
        }
        return type;
    }

    private Type getGenericGetterType(String propertyName) {
        try {
            Invoker invoker = reflector.getGetInvoker(propertyName);
            if (invoker instanceof MethodInvoker) {
                Field _method = MethodInvoker.class.getDeclaredField("method");
                _method.setAccessible(true);
                Method method = (Method) _method.get(invoker);
                return method.getGenericReturnType();
            } else if (invoker instanceof GetFieldInvoker) {
                Field _field = GetFieldInvoker.class.getDeclaredField("field");
                _field.setAccessible(true);
                Field field = (Field) _field.get(invoker);
                return field.getGenericType();
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return null;
    }


    // 这个方法用于在反射器set中查看有无该字段。如果字段有子字段，则会递归检查子属性的set。
    public boolean hasSetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            if (reflector.hasSetter(prop.getName())) {
                MetaClass metaProp = metaClassForProperty(prop.getName());
                return metaProp.hasSetter(prop.getChildren());
            } else {
                return false;
            }
        } else {
            return reflector.hasSetter(prop.getName());
        }
    }
    // 这个方法用于在映反射器get中查看有无该字段。如果字段有子字段，则会递归检查子属性的 get。
    public boolean hasGetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            if (reflector.hasGetter(prop.getName())) {
                MetaClass metaProp = metaClassForProperty(prop);
                return metaProp.hasGetter(prop.getChildren());
            } else {
                return false;
            }
        } else {
            return reflector.hasGetter(prop.getName());
        }
    }

    // 通过name获取反射器中该字段的getter方法的Invoker对象
    public Invoker getGetInvoker(String name) {
        return reflector.getGetInvoker(name);
    }
    // 通过name获取反射器中该字段的是setter方法的Invoker对象
    public Invoker getSetInvoker(String name) {
        return reflector.getSetInvoker(name);
    }
    public boolean hasDefaultConstructor() {
        return reflector.hasDefaultConstructor();
    }

}
