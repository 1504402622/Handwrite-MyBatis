package cn.glfs.mybatis.binding;

import cn.glfs.mybatis.mapping.MappedStatement;
import cn.glfs.mybatis.mapping.SqlCommandType;
import cn.glfs.mybatis.session.Configuration;
import cn.glfs.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 接口方法-sql执行器类
 */
public class MapperMethod {
    private final SqlCommand command;
    private final MethodSignature method;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
        this.command = new SqlCommand(configuration, mapperInterface, method);
        this.method = new MethodSignature(configuration, method);
    }

    /**
     * sql执行类
     * @param sqlSession
     * @param args
     * @return
     */
    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result = null;
        switch (command.getType()) {
            case INSERT:
                break;
            case DELETE:
                break;
            case UPDATE:
                break;
            case SELECT:
                // 获取参数值
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.selectOne(command.getName(), param);
                break;
            default:
                throw new RuntimeException("Unknown execution method for: " + command.getName());

        }
        return result;
    }

    /**
     * sql命令内部类，通过接口方法名获得语句的id（name）就是在这里接口方法名和xml（mapper）中的namespace+id进行映射,以及获取语句类型（type）
     * 最后通过sqlsession用id（映射接口方法名）和入参
     */
    public static class SqlCommand {
        private final String name;
        private final SqlCommandType type;
        public SqlCommand(Configuration configuration,Class<?> mapperInterface,Method method){
            String statementName = mapperInterface.getName() + "." + method.getName();
            MappedStatement ms = configuration.getMappedStatement(statementName);
            name = ms.getId();
            type = ms.getSqlCommandType();
        }
        public String getName() {
            return name;
        }

        public SqlCommandType getType() {
            return type;
        }
    }


    /**
     * 方法签名
     */
    public static class MethodSignature {

        // 参数位置-java参数类型映射
        private final SortedMap<Integer, String> params;

        public MethodSignature(Configuration configuration, Method method) {
            // 它接受一个 SortedMap 对象作为参数，并返回一个不可修改的 SortedMap 对象。不可修改的意思是，一旦创建了这个不可修改的映射，就不能再对其进行修改，任何对其修改的尝试都会抛出异常。
            this.params = Collections.unmodifiableSortedMap(getParams(method));
        }

        /**
         * 无参数返回null
         * 有一个参数返回参数值
         * 有多个参数返回Map<String, Object>，格式是param1:参数类型，param2:参数类型...
         */
        public Object convertArgsToSqlCommandParam(Object[] args) {
            final int paramCount = params.size();
            if (args == null || paramCount == 0) {
                //如果没参数
                return null;
            } else if (paramCount == 1) {
                return args[params.keySet().iterator().next().intValue()];
            } else {
                // 否则，返回一个ParamMap，修改参数名，参数名就是其位置
                final Map<String, Object> param = new ParamMap<Object>();
                int i = 0;
                // params.entrySet()返回每一个键值对
                for (Map.Entry<Integer, String> entry : params.entrySet()) {
                    // 1.先加一个#{0},#{1},#{2}...参数
                    param.put(entry.getValue(), args[entry.getKey().intValue()]);
                    // issue #71, add param names as param1, param2...but ensure backward compatibility
                    final String genericParamName = "param" + (i + 1);
                    if (!param.containsKey(genericParamName)) {
                        /*
                         * 2.再加一个#{param1},#{param2}...参数
                         * 你可以传递多个参数给一个映射器方法。如果你这样做了,
                         * 默认情况下它们将会以它们在参数列表中的位置来命名,比如:#{param1},#{param2}等。
                         * 如果你想改变参数的名称(只在多参数情况下) ,那么你可以在参数上使用@Param(“paramName”)注解。
                         */
                        param.put(genericParamName, args[entry.getKey()]);
                    }
                    i++;
                }
                return param;
            }
        }

        private SortedMap<Integer, String> getParams(Method method) {
            // 用一个TreeMap，这样就保证还是按参数的先后顺序
            final SortedMap<Integer, String> params = new TreeMap<Integer, String>();
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                String paramName = String.valueOf(argTypes[i]);
                params.put(i, paramName);
            }
            return params;
        }

    }


    /**
     * 参数map，静态内部类,更严格的get方法，如果没有相应的key，报错
     */
    public static class ParamMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -2212268410512043556L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new RuntimeException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }

    }

}
