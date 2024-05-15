package cn.glfs.mybatis.test.dao;

import cn.glfs.mybatis.test.po.User;

public interface IUserDao {

    User queryUserInfoById(Long uId);

}
