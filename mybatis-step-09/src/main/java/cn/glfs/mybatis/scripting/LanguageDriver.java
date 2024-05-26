package cn.glfs.mybatis.scripting;


import cn.glfs.mybatis.mapping.SqlSource;
import cn.glfs.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * 脚本语言驱动
 */
public interface LanguageDriver {
    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);
}
