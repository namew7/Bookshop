package com.bookshop.bookshop.exception;

/**
 * 专门用于“预期内的业务错误” (如库存不足、密码错误)
 */
public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}