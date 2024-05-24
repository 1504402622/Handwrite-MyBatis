package cn.glfs.mybatis.executor.statement;


import cn.glfs.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 *  语句处理器接口
 */
public interface StatementHandler {

    /**
     * 准备语句，就是实例化statement对象
     *
     */
    Statement prepare(Connection connection) throws SQLException;

    /**
     * 参数填充
     */
    void parameterize(Statement statement) throws SQLException;

    /**
     * 执行查询
     */
    <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;
}
