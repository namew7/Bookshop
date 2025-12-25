package com.bookshop.bookshop.exception;

import com.bookshop.bookshop.pojo.Result;
import org.springframework.dao.DuplicateKeyException; // 导入 Spring 的数据库异常
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 专门捕获数据库“重复键”异常 (并发注册时会触发这个)
    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        // 这里的 e.getMessage() 会包含具体的 SQL 错误，比如 "Duplicate entry 'zhangsan'..."
        // 我们可以模糊处理，或者解析字符串
        return Result.error("该用户名或邮箱已被注册");
    }

    //  1. 专门捕获我们自己抛出的业务异常
    @ExceptionHandler(ServiceException.class)
    public Result handleServiceException(ServiceException e) {
        // 这是预期内的，直接告诉用户原因
        return Result.error(e.getMessage());
    }

    //  2. 捕获所有其他未知的 RuntimeException (比如空指针、数组越界)
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        e.printStackTrace(); // 打印后台日志给程序员看
        // 给用户返回通用的错误，别暴露细节
        return Result.error("服务器开小差了，请联系管理员");
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleJsonException(HttpMessageNotReadableException e) {
        return Result.error("请求参数格式有误，请检查 JSON 格式");
    }
    // 3. 兜底
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        e.printStackTrace();
        return Result.error("系统繁忙，请稍后重试");
    }
}