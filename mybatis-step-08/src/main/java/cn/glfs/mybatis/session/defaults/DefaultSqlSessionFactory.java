package cn.glfs.mybatis.session.defaults;


import cn.glfs.mybatis.executor.Executor;
import cn.glfs.mybatis.mapping.Environment;
import cn.glfs.mybatis.session.Configuration;
import cn.glfs.mybatis.session.SqlSession;
import cn.glfs.mybatis.session.SqlSessionFactory;
import cn.glfs.mybatis.session.TransactionIsolationLevel;
import cn.glfs.mybatis.transaction.Transaction;
import cn.glfs.mybatis.transaction.TransactionFactory;

import java.sql.SQLException;

/**
 * 默认的DefaultSqlSessionFactory工厂
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {


    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        Transaction tx = null;
        try {

            final Environment environment = configuration.getEnvironment();
            TransactionFactory transactionFactory = environment.getTransactionFactory();
            tx = transactionFactory.newTransaction(configuration.getEnvironment().getDataSource(), TransactionIsolationLevel.READ_COMMITTED, false);
            // 创建执行器
            final Executor executor = configuration.newExecutor(tx);
            // 创建defaultSession
            return new DefaultSqlSession(configuration,executor);

        }catch (Exception e){
            try {
                assert tx != null;
                tx.close();;
            }catch (SQLException ignore){

            }
            throw new RuntimeException("Error opening session.  Cause: " + e);
        }
    }

}
