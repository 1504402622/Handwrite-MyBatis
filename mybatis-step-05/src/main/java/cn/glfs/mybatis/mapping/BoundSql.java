package cn.glfs.mybatis.mapping;

import java.util.Map;

/**
 * 绑定的SQL, 是从SqlSource而来，将动态内容都处理完成得到的SQL语句字符串，其中包括?,还有绑定的参数
 */
public class BoundSql {
    /** 参数类型 */
    private String parameterType;
    /** 结果类型 */
    private String resultType;
    /** sql语句 */
    private String sql;
    /** 参数映射器（1：第一个参数名，2：第二个参数名） 与po对接 */
    private Map<Integer, String> parameterMappings;

    public BoundSql(String sql, Map<Integer, String> parameterMappings, String parameterType, String resultType) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.parameterType = parameterType;
        this.resultType = resultType;
    }

    public String getSql() {
        return sql;
    }

    public Map<Integer, String> getParameterMappings() {
        return parameterMappings;
    }

    public String getParameterType() {
        return parameterType;
    }

    public String getResultType() {
        return resultType;
    }
}
