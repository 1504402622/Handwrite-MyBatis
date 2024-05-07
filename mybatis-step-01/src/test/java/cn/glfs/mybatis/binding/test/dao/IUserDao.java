package cn.glfs.mybatis.binding.test.dao;

import cn.glfs.mybatis.test.po.User;

public interface IUserDao {
    User queryUserInfoById(Long id);
}
