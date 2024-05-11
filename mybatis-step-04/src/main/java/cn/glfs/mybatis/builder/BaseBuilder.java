package cn.glfs.mybatis.builder;


import cn.glfs.mybatis.session.Configuration;

/**
 * XML的基类
 */
public abstract class BaseBuilder {
    protected final Configuration configuration;
    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
