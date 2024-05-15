package cn.glfs.mybatis.session;


import cn.glfs.mybatis.builder.xml.XMLConfigBuilder;
import cn.glfs.mybatis.session.defaults.DefaultSqlSessionFactory;

import java.io.Reader;

/**
 * 构建SqlSessionFactory的工厂(会在建造工厂的时候对传入的字符输入流资源进行xml的解析)
 */
public class SqlSessionFactoryBuilder {

    /**
     * 这里通过xml工具将 xml中的配置 注入到 配置类 中
     * @param reader
     * @return
     */
    public SqlSessionFactory build(Reader reader) {
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader);
        return build(xmlConfigBuilder.parse());
    }

    public SqlSessionFactory build(Configuration config) {
        return new DefaultSqlSessionFactory(config);
    }

}