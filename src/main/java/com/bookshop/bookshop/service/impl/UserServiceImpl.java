package com.bookshop.bookshop.service.impl;

import com.bookshop.bookshop.exception.ServiceException;
import com.bookshop.bookshop.mapper.UserMapper;
import com.bookshop.bookshop.pojo.User;
import com.bookshop.bookshop.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils; // 导入加密工具

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    // 定义 Redis Key 前缀常量，方便管理
    private static final String CACHE_USER_KEY = "user:info:";
    // 定义加密盐值 (随便写一串复杂的字符)
    private static final String SALT = "hw892&*(#@JJ";

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    @Override
    public void register(String username, String password, String email) {
        // 🔒 优化：对密码进行 MD5 加密
        String md5Password = DigestUtils.md5DigestAsHex((password + SALT).getBytes());

        try {
            // 存入数据库
            userMapper.add(username, md5Password, email);
        } catch (Exception e) {
            // 🚨 捕获数据库唯一索引冲突异常
            // 把它转化成我们自己的 RuntimeException，这样 GlobalHandler 就能返回具体的错误文字了
            e.printStackTrace(); // 打印出来看看是不是 DuplicateKeyException
            throw new ServiceException("注册失败：用户名或邮箱可能已被他人抢先注册");
        }
    }

    @Override
    public User login(String username, String password) {
        // 1. 根据用户名查询用户
        User user = getUserFromCacheOrDb(username);

        // 2. 判断用户是否存在
        if (user == null) {
            throw new ServiceException("用户名不存在");
        }

        // 3. 比对密码
        String inputMd5Password = DigestUtils.md5DigestAsHex((password + SALT).getBytes());

        if (!user.getPassword().equals(inputMd5Password)) {
            throw new ServiceException("密码错误");
        }

        // 4. 登录成功
        return user;
    }

    private User getUserFromCacheOrDb(String username) {
        // 使用常量 Key
        String key = CACHE_USER_KEY + username;

        // 1. 先查 Redis
        String userJson = stringRedisTemplate.opsForValue().get(key);

        if (userJson != null) {
            try {
                // 如果 Redis 里存的是空字符串(防止缓存穿透用的)，直接返回 null
                if ("".equals(userJson)) {
                    return null;
                }
                System.out.println("登录走缓存：命中 " + username);
                return objectMapper.readValue(userJson, User.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        // 2. Redis 没有，查数据库
        System.out.println("登录走数据库：查询 " + username);
        User user = userMapper.findByUsername(username);

        // 3. 写入 Redis
        try {
            if (user != null) {
                String json = objectMapper.writeValueAsString(user);
                stringRedisTemplate.opsForValue().set(key, json, 1, TimeUnit.HOURS);
            }
            // 即使数据库没查到，也可以存一个空字符串，防止缓存穿透
            else {
                stringRedisTemplate.opsForValue().set(key, "", 5, TimeUnit.MINUTES);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return user;
    }
}