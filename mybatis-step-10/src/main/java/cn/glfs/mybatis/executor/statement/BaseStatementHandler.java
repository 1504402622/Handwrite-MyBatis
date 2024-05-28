package cn.glfs.mybatis.executor.statement;


import cn.glfs.mybatis.executor.Executor;
import cn.glfs.mybatis.executor.parameter.ParameterHandler;
import cn.glfs.mybatis.executor.resultset.ResultSetHandler;
import cn.glfs.mybatis.mapping.BoundSql;
import cn.glfs.mybatis.mapping.MappedStatement;
import cn.glfs.mybatis.session.Configuration;
import cn.glfs.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 语句处理器抽象基类
 */
public abstract class BaseStatementHandler implements StatementHandler {
    protected final Configuration configuration;
    protected final Executor executor;
    protected final MappedStatement mappedStatement;
    protected final Object parameterObject;
    protected final ResultSetHandler resultSetHandler;
    protected final ParameterHandler parameterHandler;
    protected BoundSql boundSql;
    public BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, ResultHandler resultHandler, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.boundSql = boundSql;

        this.parameterObject = parameterObject;
        this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
        this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, boundSql);
    }


    @Override
    public Statement prepare(Connection connection) throws SQLException{
        Statement statement = null;
        try {
            // 实例化Statement
            statement = instantiateStatement(connection);
            // 参数设置，可以被抽取，提供配置
            // 设置statement的查询超时时间为350秒，表示执行查询时的最大等待时间
            statement.setQueryTimeout(350);
            // 设置statement的数据抓取大小为10000，表示一次从数据库中获取的数据条数
            statement.setFetchSize(10000);
            return statement;
        }catch (Exception e) {
            throw new RuntimeException("Error preparing statement.  Cause: " + e, e);
        }
    }
    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;
}
