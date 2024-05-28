package cn.glfs.mybatis.builder.xml;

import cn.glfs.mybatis.builder.BaseBuilder;
import cn.glfs.mybatis.mapping.MappedStatement;
import cn.glfs.mybatis.mapping.SqlCommandType;
import cn.glfs.mybatis.mapping.SqlSource;
import cn.glfs.mybatis.scripting.LanguageDriver;
import cn.glfs.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.Locale;

/**
 * XML语句构建器:sql模块
 */
public class XMLStatementBuilder extends BaseBuilder {

    private String currentNamespace;
    private Element element;


    public XMLStatementBuilder(Configuration configuration, Element element, String currentNamespace) {
        super(configuration);
        this.element = element;
        this.currentNamespace = currentNamespace;
    }

    public void parseStatementNode(){
        String id = element.attributeValue("id");
        String parameterType = element.attributeValue("parameterType");
        Class<?> parameterTypeClass = resolveAlias(parameterType);
        String resultType = element.attributeValue("resultType");
        Class<?> resultTypeClass = resolveAlias(resultType);
        // 获取命令类型(select|insert|update|delete)
        String nodeName = element.getName();
        // 通过 valueOf() 方法将大写后的字符串与 SqlCommandType 枚举中的值进行匹配，如果匹配成功则返回相应的枚举值，否则返回 UNKNOWN。
        // Locale.ENGLISH 是 Java 中表示特定地区或国家的类。在这里，它用于将字符串转换为大写字母形式时，指定了使用英语的规则，以确保大小写转换的一致性。
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

        // 获取默认语言驱动器
        Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        LanguageDriver langDriver = configuration.getLanguageRegistry().getDriver(langClass);

        SqlSource sqlSource = langDriver.createSqlSource(configuration, element, parameterTypeClass);

        MappedStatement mappedStatement = new MappedStatement.Builder(configuration, currentNamespace + "." + id, sqlCommandType, sqlSource, resultTypeClass).build();

        // 添加解析 SQL
        configuration.addMappedStatement(mappedStatement);

    }
}
