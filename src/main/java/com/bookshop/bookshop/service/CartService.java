package com.bookshop.bookshop.service;

import com.bookshop.bookshop.pojo.CartItem;
import java.util.List;

public interface CartService {

    /**
     * 修改购物车商品数量
     * @param userId 用户ID
     * @param bookId 图书ID
     * @param count  数量 (正数增加，负数减少，虽然后端逻辑通常直接加)
     */
    void modifyCart(Integer userId, Integer bookId, Integer count);

    /**
     * 获取某用户的购物车列表
     * @param userId 用户ID
     * @return 购物车项列表
     */
    List<CartItem> getCartList(Integer userId);

    /**
     * 删除购物车中的某本书
     * @param userId 用户ID
     * @param bookId 图书ID
     */
    void deleteCartItem(Integer userId, Integer bookId);
}