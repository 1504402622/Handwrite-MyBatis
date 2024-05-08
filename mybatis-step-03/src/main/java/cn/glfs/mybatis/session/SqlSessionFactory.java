package cn.glfs.mybatis.session;

/**
 * SqlSession工厂
 */
public interface SqlSessionFactory {
    /**
     * 打开一个session
     */
    SqlSession openSession();
}
