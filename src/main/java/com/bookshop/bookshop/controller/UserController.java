package com.bookshop.bookshop.controller;

import com.bookshop.bookshop.dto.LoginDTO;
import com.bookshop.bookshop.dto.RegisterDTO;
import com.bookshop.bookshop.pojo.Result;
import com.bookshop.bookshop.pojo.User;
import com.bookshop.bookshop.service.UserService;
import com.bookshop.bookshop.vo.UserVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 提取 Redis Key 常量
    private static final String REDIS_CODE_PREFIX = "register:code:";

    @PostMapping("/sendMsg")
    public Result sendMsg(String username, String email) {
        if (username == null || username.isEmpty()) return Result.error("用户名不能为空");
        if (email == null || email.isEmpty()) return Result.error("邮箱不能为空");

        // 查重逻辑... (保持不变)
        if (userService.findByUsername(username) != null) return Result.error("用户名已被注册");
        if (userService.findByEmail(email) != null) return Result.error("邮箱已被注册");

        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        System.out.println("【模拟发送】" + email + " : " + code);

        // 使用常量构造 Key
        String key = REDIS_CODE_PREFIX + email;
        String value = code + "::" + username;

        stringRedisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);
        return Result.success("验证码已发送");
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDTO) {
        // 获取参数
        String email = registerDTO.getEmail();
        String code = registerDTO.getCode();
        String username = registerDTO.getUsername();
        String password = registerDTO.getPassword();
        // 使用常量构造 Key
        String key = REDIS_CODE_PREFIX + email;
        String redisValue = stringRedisTemplate.opsForValue().get(key);

        if (redisValue == null) {
            return Result.error("验证码已失效，请重新获取");
        }

        String[] parts = redisValue.split("::");
        if (parts.length != 2) return Result.error("数据异常");

        if (!parts[0].equals(code)) return Result.error("验证码错误");
        if (!parts[1].equals(username)) return Result.error("用户名不一致");

        // 直接调用！如果报错，GlobalExceptionHandler 会自动接盘！
        userService.register(username, password, email);
        // 只有成功才会走到这里
        stringRedisTemplate.delete(key);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        // 1. 调用 Service
        User loginUser = userService.login(loginDTO.getUsername(), loginDTO.getPassword());

        // 2. 转换对象 (User -> UserVO)
        // 这一步实现了“物理隔离”，密码字段根本不在 UserVO 类里，绝对不会泄露
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(loginUser, userVO);

        // 3. 存 Session (Session 里依然存完整的 User，因为后端拦截器可能需要用)
        session.setAttribute("LOGIN_USER", loginUser);

        // 4. 返回 VO 给前端
        return Result.success(userVO);
    }

    // logout 保持不变...
    @PostMapping("/logout")
    public Result logout(HttpSession session) {
        session.invalidate();
        return Result.success("退出成功");
    }
}