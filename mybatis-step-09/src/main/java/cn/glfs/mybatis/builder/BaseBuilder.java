package cn.glfs.mybatis.builder;


import cn.glfs.mybatis.session.Configuration;
import cn.glfs.mybatis.type.TypeAliasRegistry;

/**
 * XML的基类
 */
public abstract class BaseBuilder {
    protected final Configuration configuration;
    protected final TypeAliasRegistry typeAliasRegistry;
    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    }
    public Configuration getConfiguration() {
        return configuration;
    }
}
