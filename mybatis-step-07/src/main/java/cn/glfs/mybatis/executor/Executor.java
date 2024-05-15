package cn.glfs.mybatis.executor;


import cn.glfs.mybatis.mapping.BoundSql;
import cn.glfs.mybatis.mapping.MappedStatement;
import cn.glfs.mybatis.session.ResultHandler;
import cn.glfs.mybatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * 执行器
 */
public interface Executor {
    ResultHandler NO_RESULT_HANDLER = null;
    //使用 <T> 作为泛型类型的占位符，表示 "Type"。但是，如果方法的用途和参数类型与某种特定的实体类型或者元素类型相关，
    // 那么选择一个更有意义的标识符，比如 <E> (代表 "Element")，可能会更清晰地表达方法的意图，提高代码的可读性和可维护性。
    <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql);
    Transaction getTransaction();
    void commit(boolean required) throws SQLException;

    void rollback(boolean required) throws SQLException;

    void close(boolean forceRollback);


}