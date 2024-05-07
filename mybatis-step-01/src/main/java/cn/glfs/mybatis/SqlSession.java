package cn.glfs.mybatis;

import java.util.List;

/**
 * 会话接口
 */
public interface SqlSession {
    <T> T selectOne(String statement);
    <T> T selectOne(String statement,Object parameter);
    <T> List<T> selectList(String statement);
    <T> List<T> selectList(String statement,Object parameter);
    void close();
}
