package cn.glfs.mybatis.type;

import cn.glfs.mybatis.io.Resources;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 类型别名注册机
 */
public class   TypeAliasRegistry {
    //别名 - 类对象注册机器
    private final Map<String, Class<?>> TYPE_ALIASES = new HashMap<>();
    //在
    public TypeAliasRegistry() {
        // 构造函数里注册系统内置的类型别名
        registerAlias("string", String.class);
        // 基本包装类型
        registerAlias("byte", Byte.class);
        registerAlias("long", Long.class);
        registerAlias("short", Short.class);
        registerAlias("int", Integer.class);
        registerAlias("integer", Integer.class);
        registerAlias("double", Double.class);
        registerAlias("float", Float.class);
        registerAlias("boolean", Boolean.class);
    }

    public void registerAlias(String alias, Class<?> value){
        String key = alias.toLowerCase(Locale.ENGLISH);
        TYPE_ALIASES.put(key,value);
    }
    public <T> Class<T> resolveAlias(String string) {
        try {
            if (string == null) {
                return null;
            }
            String key = string.toLowerCase(Locale.ENGLISH);
            Class<T> value;
            if (TYPE_ALIASES.containsKey(key)) {
                value = (Class<T>) TYPE_ALIASES.get(key);
            } else {
                value = (Class<T>) Resources.classForName(string);
            }
            return value;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not resolve type alias '" + string + "'.  Cause: " + e, e);
        }
    }

}
