package cn.glfs.mybatis.executor;

import cn.glfs.mybatis.executor.statement.StatementHandler;
import cn.glfs.mybatis.mapping.BoundSql;
import cn.glfs.mybatis.mapping.MappedStatement;
import cn.glfs.mybatis.session.Configuration;
import cn.glfs.mybatis.session.ResultHandler;
import cn.glfs.mybatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 简单执行器
 */
public class SimpleExecutor extends BaseExecutor {
    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    /**
     * sql语句处理器
     * @param ms
     * @param parameter
     * @param resultHandler
     * @param boundSql
     * @return
     * @param <E>
     */
    @Override
    protected <E> List<E> doQuery(MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
        try{
            Configuration configuration = ms.getConfiguration();
            // 语句预处理
            StatementHandler handler =configuration.newStatementHandler(this,ms,parameter,resultHandler,boundSql);
            Connection connection = transaction.getConnection();
            // 实例化statement对象prepare-》instantiateStatement
            Statement stmt = handler.prepare(connection);
            // 将参数列表装填在语句中
            handler.parameterize(stmt);
            // query
            return handler.query(stmt, resultHandler);
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }
}
