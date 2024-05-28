package cn.glfs.mybatis.type;


import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * 类型处理器注册机,进行类型handler注册
 */
public final class TypeHandlerRegistry {

    // 用于存储不同 JdbcType 类型对应的处理器
    private final Map<JdbcType, TypeHandler<?>> JDBC_TYPE_HANDLER_MAP = new EnumMap<>(JdbcType.class);
    // 用于存储不同 Java 类型对应的处理器，键为 Java 类型，值为该类型对应的处理器 Map
    // 有时候，在使用某个Java类型与数据库交互时，需要明确指定数据库中对应的SQL数据类型，以确保数据的正确性。比如，对于String类型，可能在数据库中存储的是CHAR类型或VARCHAR类型，这取决于数据库表的设计。因此，有时候需要明确指定Java类型对应的JdbcType。
    // 在某些情况下，我们不关心数据库中的具体数据类型，只需要一个通用的处理器来处理该Java类型。这时候就可以不指定JdbcType，让处理器使用默认的处理方式。因为long类型兼容jdbc的int small long
    // 至于为什么一种Java类型可以对应多种JdbcType的处理器，这是因为不同的数据库可能对同一种Java类型有不同的存储方式。例如，String类型在不同数据库中可能存储为CHAR、VARCHAR或TEXT等不同的SQL数据类型。因此，为了兼容不同数据库，可以针对同一种Java类型注册多个处理器，每个处理器对应不同的JdbcType，以便在需要时选择合适的处理器进行数据转换。
    private final Map<Type, Map<JdbcType, TypeHandler<?>>> TYPE_HANDLER_MAP = new HashMap<>();
    // 存储所有处理器的 Map，键为处理器的 Class 对象，值为处理器对象
    private final Map<Class<?>, TypeHandler<?>> ALL_TYPE_HANDLERS_MAP = new HashMap<>();

    public TypeHandlerRegistry() {
        register(Long.class, new LongTypeHandler());
        register(long.class, new LongTypeHandler());

        register(String.class, new StringTypeHandler());
        register(String.class, JdbcType.CHAR, new StringTypeHandler());
        register(String.class, JdbcType.VARCHAR, new StringTypeHandler());
    }

    private <T> void register(Type javaType, TypeHandler<? extends T> typeHandler) {
        register(javaType, null, typeHandler);
    }

    private void register(Type javaType, JdbcType jdbcType, TypeHandler<?> handler) {
        if (null != javaType) {
            // 在 TYPE_HANDLER_MAP 中检查是否已经存在一个与 javaType 关联的 Map<JdbcType, TypeHandler<?>>，如果不存在，则创建一个新的 HashMap<>() 并将其与 javaType 关联，然后将其放入 TYPE_HANDLER_MAP 中。
            Map<JdbcType, TypeHandler<?>> map = TYPE_HANDLER_MAP.computeIfAbsent(javaType, k -> new HashMap<>());
            map.put(jdbcType, handler);
        }
        ALL_TYPE_HANDLERS_MAP.put(handler.getClass(), handler);
    }

    @SuppressWarnings("unchecked")
    public <T> TypeHandler<T> getTypeHandler(Class<T> type, JdbcType jdbcType) {
        return getTypeHandler((Type) type, jdbcType);
    }

    public boolean hasTypeHandler(Class<?> javaType) {
        return hasTypeHandler(javaType, null);
    }

    public boolean hasTypeHandler(Class<?> javaType, JdbcType jdbcType) {
        return javaType != null && getTypeHandler((Type) javaType, jdbcType) != null;
    }

    private <T> TypeHandler<T> getTypeHandler(Type type, JdbcType jdbcType) {
        // 通过java类型获取jdbc类型对应的handler
        Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = TYPE_HANDLER_MAP.get(type);
        TypeHandler<?> handler = null;
        if (jdbcHandlerMap != null) {
            handler = jdbcHandlerMap.get(jdbcType);
            if (handler == null) {
                handler = jdbcHandlerMap.get(null);
            }
        }
        // type drives generics here
        return (TypeHandler<T>) handler;
    }
}
