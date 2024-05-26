package cn.glfs.mybatis.builder;


import cn.glfs.mybatis.session.Configuration;
import cn.glfs.mybatis.type.TypeAliasRegistry;
import cn.glfs.mybatis.type.TypeHandlerRegistry;

/**
 * XML的基类
 */
public abstract class BaseBuilder {
    protected final Configuration configuration;
    protected final TypeAliasRegistry typeAliasRegistry;
    protected final TypeHandlerRegistry typeHandlerRegistry;
    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
    }
    public Configuration getConfiguration() {
        return configuration;
    }
    protected Class<?> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }
}
