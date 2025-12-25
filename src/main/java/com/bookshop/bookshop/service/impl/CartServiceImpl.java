package com.bookshop.bookshop.service.impl;

import com.bookshop.bookshop.exception.ServiceException; // 👈 1. 记得导入你定义的异常
import com.bookshop.bookshop.pojo.Book;
import com.bookshop.bookshop.pojo.CartItem;
import com.bookshop.bookshop.service.BookService;
import com.bookshop.bookshop.service.CartAsyncService;
import com.bookshop.bookshop.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CartServiceImpl implements CartService {

    @Autowired private StringRedisTemplate stringRedisTemplate;
    @Autowired private BookService bookService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CartAsyncService cartAsyncService;
    @Autowired private RedissonClient redissonClient;

    private String getCartKey(Integer userId) {
        return "cart:user:" + userId;
    }

    @Override
    public void modifyCart(Integer userId, Integer bookId, Integer count) {
        String lockKey = "lock:cart:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试加锁
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    String key = getCartKey(userId);
                    String hashKey = String.valueOf(bookId);

                    Object jsonVal = stringRedisTemplate.opsForHash().get(key, hashKey);

                    if (jsonVal == null) {
                        // === 情况 A：购物车没书 ===
                        if (count <= 0) {
                            // 2. 使用自定义异常
                            throw new ServiceException("购物车中没有该商品，无法减少！");
                        }
                        addNewItem(userId, bookId, count, key, hashKey);
                    } else {
                        // === 情况 B：已有 ===
                        CartItem cartItem = objectMapper.readValue(jsonVal.toString(), CartItem.class);
                        int finalQuantity = cartItem.getCount() + count;

                        // 删
                        if (finalQuantity <= 0) {
                            stringRedisTemplate.opsForHash().delete(key, hashKey);
                            cartAsyncService.deleteCartInMySQL(userId, bookId);
                            return;
                        }

                        // 改 (查库存)
                        if (count > 0) {
                            Book book = bookService.getBookById(bookId);
                            if (finalQuantity > book.getQuantity()) {
                                throw new ServiceException("库存不足，最多只能买: " + book.getQuantity() + " 本");
                            }
                        }

                        cartItem.setCount(finalQuantity);
                        cartItem.setTotalPrice(cartItem.getPrice().multiply(BigDecimal.valueOf(finalQuantity)));
                        stringRedisTemplate.opsForHash().put(key, hashKey, objectMapper.writeValueAsString(cartItem));
                        cartAsyncService.syncCartToMySQL(cartItem);
                    }
                } finally {
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                throw new ServiceException("操作太快了，请稍后再试");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new ServiceException("系统繁忙");
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            }
            e.printStackTrace();
            throw new RuntimeException("系统内部错误: " + e.getMessage());
        }
    }

    @Override
    public List<CartItem> getCartList(Integer userId) {
        String key = getCartKey(userId);
        List<Object> jsonList = stringRedisTemplate.opsForHash().values(key);
        List<CartItem> resultList = new ArrayList<>();
        for (Object obj : jsonList) {
            try {
                resultList.add(objectMapper.readValue(obj.toString(), CartItem.class));
            } catch (Exception e) { e.printStackTrace(); }
        }
        return resultList;
    }

    @Override
    public void deleteCartItem(Integer userId, Integer bookId) {
        // 1. 获取锁 (防止清空时有人正在写入)
        String lockKey = "lock:cart:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    String key = getCartKey(userId);

                    // 判断 bookId 是否为空
                    if (bookId == null) {
                        // === 分支 A: 清空购物车 ===

                        // 1. Redis 直接删除整个 Key (DEL cart:user:1)
                        stringRedisTemplate.delete(key);

                        // 2. 异步清空 MySQL
                        cartAsyncService.clearCartInMySQL(userId);

                    } else {
                        // === 分支 B: 删除单本 ===

                        String hashKey = String.valueOf(bookId);

                        // 1. Redis 删除单个 HashKey (HDEL cart:user:1 101)
                        stringRedisTemplate.opsForHash().delete(key, hashKey);

                        // 2. 异步删除 MySQL 单条
                        cartAsyncService.deleteCartInMySQL(userId, bookId);
                    }
                } finally {
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                throw new ServiceException("系统繁忙，请稍后再试");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new ServiceException("系统错误");
        }
    }

    private void addNewItem(Integer userId, Integer bookId, Integer count, String key, String hashKey) throws Exception {
        Book book = bookService.getBookById(bookId);
        if (book == null) throw new ServiceException("图书不存在"); // 改为 ServiceException
        if (count > book.getQuantity()) throw new ServiceException("库存不足"); // 改为 ServiceException

        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setBookId(bookId);
        cartItem.setCount(count);
        cartItem.setBookName(book.getTitle());
        cartItem.setPrice(book.getPrice());
        cartItem.setTotalPrice(book.getPrice().multiply(BigDecimal.valueOf(count)));

        stringRedisTemplate.opsForHash().put(key, hashKey, objectMapper.writeValueAsString(cartItem));
        cartAsyncService.syncCartToMySQL(cartItem);
    }
}