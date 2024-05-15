package cn.glfs.mybatis.session.defaults;


import cn.glfs.mybatis.executor.Executor;
import cn.glfs.mybatis.mapping.BoundSql;
import cn.glfs.mybatis.mapping.Environment;
import cn.glfs.mybatis.mapping.MappedStatement;
import cn.glfs.mybatis.session.Configuration;
import cn.glfs.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;
    private Executor executor;

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }


    @Override
    public <T> T selectOne(String statement) {
        return (T)("你的操作被代理了！"+statement);
    }

    /**
     * 通过sql语句类获取sql语句，实际执行
     * //        try {
     * //            MappedStatement mappedStatement = configuration.getMappedStatement(statement);
     * //            Environment environment = configuration.getEnvironment();
     * //
     * //            Connection connection = environment.getDataSource().getConnection();
     * //
     * //            BoundSql boundSql = mappedStatement.getBoundSql();
     * //            PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSql());
     * //            preparedStatement.setLong(1, Long.parseLong(((Object[]) parameter)[0].toString()));
     * //            ResultSet resultSet = preparedStatement.executeQuery();
     * //            List<T> objList = resultSet2Obj(resultSet, Class.forName(boundSql.getResultType()));
     * //            return objList.get(0);
     * //        }catch (Exception e) {
     * //            e.printStackTrace();
     * //            return null;
     * //        }
     */
    @Override
    public <T> T selectOne(String statement, Object parameter) {
        MappedStatement ms = configuration.getMappedStatement(statement);
        List<T> list = executor.query(ms, parameter, Executor.NO_RESULT_HANDLER, ms.getBoundSql());
        return list.get(0);
    }


    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}