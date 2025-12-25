package com.bookshop.bookshop.mapper;

import com.bookshop.bookshop.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户
     * 用途：
     * 1. 注册时：检查用户名是否已经存在
     * 2. 登录时：根据用户名查找用户，再比对密码
     */
    @Select("select * from users where username = #{username}")
    User findByUsername(String username);
    //邮箱查询
    @Select("select * from users where email = #{email}")
    User findByEmail(String email);

    /**
     * 添加新用户（注册）
     * 用途：将注册信息写入数据库
     * 注意：now() 是 MySQL 的函数，获取当前时间
     */
    @Insert("insert into users(username, password, email, create_time, update_time)" +
            " values(#{username}, #{password}, #{email}, now(), now())")
    void add(String username, String password, String email);
}