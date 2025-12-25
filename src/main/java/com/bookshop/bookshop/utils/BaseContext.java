package com.bookshop.bookshop.utils;

/**
 * 基于 ThreadLocal 封装的工具类
 * 用于保存和获取当前登录用户的 ID
 */
public class BaseContext {

    // 定义一个 ThreadLocal 变量，用来存 ID (Integer类型)
    private static ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    // 设置当前线程的用户ID
    public static void setCurrentId(Integer id) {
        threadLocal.set(id);
    }

    // 获取当前线程的用户ID
    public static Integer getCurrentId() {
        return threadLocal.get();
    }

    // 移除当前线程的值
    public static void remove() {
        threadLocal.remove();
    }
}