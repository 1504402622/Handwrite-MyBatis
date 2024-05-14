package cn.glfs.mybatis.session;


import cn.glfs.mybatis.binding.MapperRegistry;
import cn.glfs.mybatis.datasource.druid.DruidDataSourceFactory;
import cn.glfs.mybatis.datasource.pooled.PooledDataSourceFactory;
import cn.glfs.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import cn.glfs.mybatis.mapping.Environment;
import cn.glfs.mybatis.mapping.MappedStatement;
import cn.glfs.mybatis.transaction.jdbc.JdbcTransactionFactory;
import cn.glfs.mybatis.type.TypeAliasRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * （含代理对象的注册类、接口方法名（id）-sql语句类映射器）配置类
 */
public class Configuration {
    /**
     * 环境
     */
    protected Environment environment;

    /**
     * （含接口-代理工厂映射器）代理对象的注册类
     */
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    /**
     * 接口方法名（id）-sql语句类映射器
     */
    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

    /**
     * 类型别名注册机
     */
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);

        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
    }

    /**
     * 代理对象的注册类相关
     */
    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    /**
     * 接口方法名（id）-sql语句映射器相关
     */
    //映射器增加 接口方法id-sql语句类对
    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    //通过接口方法名（id）得到sql语句类
    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }

    /**
     * 环境、类型别名注册机相关
     */
    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
