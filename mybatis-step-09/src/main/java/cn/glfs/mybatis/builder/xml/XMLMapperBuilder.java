package cn.glfs.mybatis.builder.xml;

import cn.glfs.mybatis.builder.BaseBuilder;
import cn.glfs.mybatis.io.Resources;
import cn.glfs.mybatis.session.Configuration;
import cn.hutool.json.XML;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

/**
 * XML映射构建器:mapper解析
 */
public class XMLMapperBuilder  extends BaseBuilder {
    private Element element;
    private String resource;
    private String currentNamespace;

    public XMLMapperBuilder(InputStream inputStream,Configuration configuration,String resource)throws DocumentException {
        this(new SAXReader().read(inputStream),configuration,resource);
    }

    private XMLMapperBuilder(Document document,Configuration configuration,String resource){
        super(configuration);
        this.element = document.getRootElement();
        this.resource = resource;
    }

    /**
     * 解析
     */
    public void parse() throws Exception{
        if(!configuration.isResourceLoaded(resource)){
            configurationElement(element);
            // 标记一下，已经加载过了
            configuration.addLoadedResource(resource);
            // 绑定映射器到namespace
            configuration.addMapper(Resources.classForName(currentNamespace));
        }
    }

    private void configurationElement(Element element){
        // 1.配置namespace
        currentNamespace = element.attributeValue("namespace");
        if(currentNamespace.equals("")){
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }

        // 2.配置select|insert|update|delete
        // 对每个查询类型的sql语句进行解析
        buildStatementFromContext(element.elements("select"));
    }

    private void buildStatementFromContext(List<Element> list){
        for (Element element : list) {
            final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, element, currentNamespace);
            statementParser.parseStatementNode();
        }
    }
}
