package com.bookshop.bookshop.service;

import com.bookshop.bookshop.mapper.CartMapper;
import com.bookshop.bookshop.pojo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CartAsyncService {

    @Autowired
    private CartMapper cartMapper;

    /**
     * 异步方法：将 Redis 的变更同步到 MySQL
     * 加了 @Async，Spring 会把这个方法扔到线程池去跑，不卡主线程
     */
    @Async
    public void syncCartToMySQL(CartItem item) {
        try {
            // 1. 先查数据库有没有这条记录
            CartItem dbItem = cartMapper.findOne(item.getUserId(), item.getBookId());

            if (dbItem == null) {
                // 2. 没有 -> 新增
                cartMapper.insert(item);
            } else {
                // 3. 有 -> 更新数量
                cartMapper.updateCount(item.getUserId(), item.getBookId(), item.getCount());
            }
            System.out.println("[异步同步] 购物车数据已落库: " + item.getBookName());
        } catch (Exception e) {
            e.printStackTrace();
            // 生产环境这里应该记录错误日志，或者重试
        }
    }

    @Async
    public void deleteCartInMySQL(Integer userId, Integer bookId) {
        cartMapper.delete(userId, bookId);
        System.out.println("[异步同步] 数据库记录已删除");
    }

    @Async
    public void clearCartInMySQL(Integer userId) {
        cartMapper.deleteAll(userId);
        System.out.println("[异步同步] 用户 " + userId + " 的购物车已清空");
    }
}