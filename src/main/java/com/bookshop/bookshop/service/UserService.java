package com.bookshop.bookshop.service;

import com.bookshop.bookshop.pojo.User;


public interface UserService {
    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);
    /**
     * 根据邮箱查询用户
     */
    User findByEmail(String email);
    /**
     * 注册用户
     */
    void register(String username, String password, String email);
    /**
     * 新增：用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户对象，如果失败返回 null
     */
    User login(String username, String password);

}