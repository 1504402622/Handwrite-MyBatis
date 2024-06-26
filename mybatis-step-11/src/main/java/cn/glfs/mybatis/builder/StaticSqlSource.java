package cn.glfs.mybatis.builder;

import cn.glfs.mybatis.mapping.BoundSql;
import cn.glfs.mybatis.mapping.ParameterMapping;
import cn.glfs.mybatis.mapping.SqlSource;
import cn.glfs.mybatis.session.Configuration;

import java.util.List;

/**
 * 静态SQL源码
 */
public class StaticSqlSource implements SqlSource {
    private String sql;
    private List<ParameterMapping> parameterMappings;
    private Configuration configuration;
    public StaticSqlSource(Configuration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }
}
