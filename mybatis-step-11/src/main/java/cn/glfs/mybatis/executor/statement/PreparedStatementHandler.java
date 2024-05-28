package cn.glfs.mybatis.executor.statement;


import cn.glfs.mybatis.executor.Executor;
import cn.glfs.mybatis.mapping.BoundSql;
import cn.glfs.mybatis.mapping.MappedStatement;
import cn.glfs.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 预处理语句处理器（PREPARED）
 */
public class PreparedStatementHandler extends BaseStatementHandler {
    public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, ResultHandler resultHandler, BoundSql boundSql) {
        super(executor, mappedStatement, parameterObject, resultHandler, boundSql);
    }


    /**
     * 创建Statement对象进行预编译
     * @param connection
     * @return
     * @throws SQLException
     */
    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        String sql = boundSql.getSql();
        //然后使用该语句创建了一个 PreparedStatement 对象（通过 connection.prepareStatement(sql)）。它可以预编译 SQL 语句，提高了执行效率
        // 预编译的作用是在 SQL 语句执行之前，将 SQL 语句编译成数据库的内部表示形式，并将该表示形式存储在数据库中。这样做的好处是每次执行 SQL 语句时不需要重新解析和编译 SQL 语句，而是直接使用预编译的表示形式，从而节省了解析和编译的时间，提高了执行效率。
        //预编译 SQL 语句还可以提高代码的安全性，特别是防止 SQL 注入攻击。因为在预编译阶段，参数的值会被作为参数传递给 SQL 语句，而不是将参数的值直接拼接到 SQL 语句中。这样可以有效地防止恶意用户通过在参数中注入 SQL 代码来执行恶意操作的攻击。
        return connection.prepareStatement(sql);
    }

    /**
     * 参数设置
     * @param statement
     * @throws SQLException
     */
    @Override
    public void parameterize(Statement statement) throws SQLException {
        parameterHandler.setParameters((PreparedStatement) statement);
    }

    /**
     * 执行查询并封装结果集返回
     * @param statement
     * @param resultHandler
     * @return
     * @param <E>
     * @throws SQLException
     */
    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        return resultSetHandler.<E>handleResultSets(ps);
    }
}
