package cn.glfs.mybatis;

/**
 * 会话工厂
 */
public interface SqlSessionFactory {

    SqlSession openSession();

}
