package cn.glfs.mybatis.executor.resultset;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @description
 */
public interface ResultSetHandler {
    // 结果处理
    <E> List<E> handleResultSets(Statement statement) throws SQLException;
}
