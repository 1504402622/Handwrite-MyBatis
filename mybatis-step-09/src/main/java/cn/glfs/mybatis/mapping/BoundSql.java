package cn.glfs.mybatis.mapping;

import cn.glfs.mybatis.reflection.MetaObject;
import cn.glfs.mybatis.session.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL语句详情类, 是从SqlSource而来，将动态内容都处理完成得到的SQL语句字符串，其中包括?,还有绑定的参数
 */
public class BoundSql {

    /** sql语句 */
    private String sql;
    /** 参数映射器（1：第一个参数名）与po对接 */
    private List<ParameterMapping> parameterMappings;
    /** 参数类型 */
    private Object parameterObject;
    private Map<String, Object> additionalParameters;
    /** 元数据对象 */
    private MetaObject metaParameters;


    public BoundSql(Configuration configuration, String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.parameterObject = parameterObject;
        this.additionalParameters = new HashMap<>();
        this.metaParameters = configuration.newMetaObject(additionalParameters);
    }

    public String getSql() {
        return sql;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

    public Object getParameterObject() {
        return parameterObject;
    }

    public boolean hasAdditionalParameter(String name) {
        return metaParameters.hasGetter(name);
    }

    public void setAdditionalParameter(String name, Object value) {
        metaParameters.setValue(name, value);
    }

    public Object getAdditionalParameter(String name) {
        return metaParameters.getValue(name);
    }



}
