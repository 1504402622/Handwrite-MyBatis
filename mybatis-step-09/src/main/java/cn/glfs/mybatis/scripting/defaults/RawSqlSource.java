package cn.glfs.mybatis.scripting.defaults;


import cn.glfs.mybatis.builder.SqlSourceBuilder;
import cn.glfs.mybatis.mapping.BoundSql;
import cn.glfs.mybatis.mapping.SqlSource;
import cn.glfs.mybatis.scripting.xmltags.DynamicContext;
import cn.glfs.mybatis.scripting.xmltags.SqlNode;
import cn.glfs.mybatis.session.Configuration;

import java.util.HashMap;
public class RawSqlSource implements SqlSource {

    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<>());
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }

    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        DynamicContext context = new DynamicContext(configuration, null);
        rootSqlNode.apply(context);
        // 删除拼接后两端的空格并返回拼接字符格式sql1
        return context.getSql();
    }

}