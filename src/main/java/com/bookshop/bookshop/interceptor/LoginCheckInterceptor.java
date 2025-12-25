package com.bookshop.bookshop.interceptor;

import com.bookshop.bookshop.pojo.Result;
import com.bookshop.bookshop.pojo.User;
import com.bookshop.bookshop.utils.BaseContext;
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson的工具类
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    // 目标资源方法执行前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 获取 Session
        HttpSession session = request.getSession();

        // 2. 获取用户信息
        User user = (User) session.getAttribute("LOGIN_USER");

        // 3. 判断是否登录
        if (user != null) {
            // === 已登录 ===
            // 关键步骤：把用户ID存入 ThreadLocal
            BaseContext.setCurrentId(user.getId());

            // 放行
            return true;
        }

        // === 未登录 ===
        // 拦截，并返回错误信息 (JSON格式)
        // 因为拦截器不是 Controller，无法直接 return Result，需要手动写流
        Result errorResult = Result.error("NOT_LOGIN"); // 或者 "请先登录"
        String json = new ObjectMapper().writeValueAsString(errorResult);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(json);

        return false; // 不放行
    }

    // 视图渲染完毕后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 非常重要：请求结束时，清空 ThreadLocal，防止内存泄露
        BaseContext.remove();
    }
}