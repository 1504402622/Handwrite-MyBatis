package cn.glfs.mybatis.test;

import cn.glfs.mybatis.binding.MapperRegistry;
import cn.glfs.mybatis.session.SqlSession;
import cn.glfs.mybatis.session.defaults.DefaultSqlSessionFactory;
import cn.glfs.mybatis.test.dao.IUserDao;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiTest {
    private Logger logger = LoggerFactory.getLogger(ApiTest.class);
    @Test
    public void test_MapperProxyFactory() {
        //1.注册Mapper
        MapperRegistry mapperRegistry = new MapperRegistry();
        mapperRegistry.addMappers("cn.glfs.mybatis.test.dao");

        //2.从SqlSession 工厂获取Session
        DefaultSqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(mapperRegistry);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        //3.获取映射器对象
        IUserDao mapper = sqlSession.getMapper(IUserDao.class);

        //4.测试验证
        String res = mapper.queryUserName("10001");
        logger.info("测试结果:{}",res);
    }
}
