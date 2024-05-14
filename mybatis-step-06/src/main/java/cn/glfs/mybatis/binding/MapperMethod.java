package cn.glfs.mybatis.binding;

import cn.glfs.mybatis.mapping.MappedStatement;
import cn.glfs.mybatis.mapping.SqlCommandType;
import cn.glfs.mybatis.session.Configuration;
import cn.glfs.mybatis.session.SqlSession;

import java.lang.reflect.Method;

/**
 * 接口方法-sql执行器类
 */
public class MapperMethod {
    private final SqlCommand command;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
        this.command = new SqlCommand(configuration, mapperInterface, method);
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
                result = sqlSession.selectOne(command.getName(), args);
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
}
