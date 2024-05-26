package cn.glfs.mybatis.session;

import java.sql.Connection;

/**
 * 事务的隔离级别,NONE,RR,RC,RU,SERIALIZABLE==>0,1,2,3,4
 */
public enum TransactionIsolationLevel {
    NONE(Connection.TRANSACTION_NONE),
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    private final int level;
    TransactionIsolationLevel(int level){this.level = level;}

    public int getLevel() {
        return level;
    }
}
