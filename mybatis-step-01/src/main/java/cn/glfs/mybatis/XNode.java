package cn.glfs.mybatis;

import lombok.Data;

import java.util.Map;

/**
 * 解析结点
 */
public class XNode {
    /** 命名空间 */
    private String namespace;
    /** 主键id */
    private String id;
    /** 参数类型 */
    private String parameterType;
    /** 返回值类型 */
    private String resultType;
    /** sql语句 */
    private String sql;
    /** 参数容器 */
    private Map<Integer,String> parameter;


    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Map<Integer, String> getParameter() {
        return parameter;
    }

    public void setParameter(Map<Integer, String> parameter) {
        this.parameter = parameter;
    }
}
