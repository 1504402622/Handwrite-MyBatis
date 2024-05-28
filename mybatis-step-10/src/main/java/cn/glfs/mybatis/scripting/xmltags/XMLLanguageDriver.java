package cn.glfs.mybatis.scripting.xmltags;


import cn.glfs.mybatis.executor.parameter.ParameterHandler;
import cn.glfs.mybatis.mapping.BoundSql;
import cn.glfs.mybatis.mapping.MappedStatement;
import cn.glfs.mybatis.mapping.SqlSource;
import cn.glfs.mybatis.scripting.LanguageDriver;
import cn.glfs.mybatis.scripting.defaults.DefaultParameterHandler;
import cn.glfs.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * XML语言驱动器
 */
public class XMLLanguageDriver implements LanguageDriver {

    // script：sql语句的根element
    @Override
    public SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType) {
        // 用XML脚本构建器解析
        XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }

    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }
}