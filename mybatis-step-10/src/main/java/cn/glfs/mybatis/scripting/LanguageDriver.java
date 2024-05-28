package cn.glfs.mybatis.scripting;


import cn.glfs.mybatis.executor.parameter.ParameterHandler;
import cn.glfs.mybatis.mapping.BoundSql;
import cn.glfs.mybatis.mapping.MappedStatement;
import cn.glfs.mybatis.mapping.SqlSource;
import cn.glfs.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * 脚本语言驱动
 */
public interface LanguageDriver {
    /**
     * 创建SQL源码(mapper xml方式)
     */
    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);

    /**
     * 创建参数处理器
     */
    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);
}
