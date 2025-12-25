package com.bookshop.bookshop.controller;

import com.bookshop.bookshop.pojo.CartItem;
import com.bookshop.bookshop.pojo.Result;
import com.bookshop.bookshop.service.CartService;
import com.bookshop.bookshop.utils.BaseContext; // 确保导入你的 BaseContext
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加/修改购物车
     * 前端只传: bookId, count
     */
    @GetMapping("/modify")
    public Result modify(@RequestParam Integer bookId,
                         @RequestParam Integer count) {

        // 1. 安全获取当前用户 ID
        Integer userId = BaseContext.getCurrentId();

        // 2. 传给 Service
        cartService.modifyCart(userId, bookId, count);

        return Result.success("操作成功");
    }

    /**
     * 查看购物车列表
     * 前端不传参 (自动查自己的)
     */
    @GetMapping("/list")
    public Result list() {
        // 1. 获取 ID
        Integer userId = BaseContext.getCurrentId();

        // 2. 调用 Service
        List<CartItem> list = cartService.getCartList(userId);

        return Result.success(list);
    }

    /**
     * 删除购物车
     * 场景: 删单本 或 清空
     */
    @GetMapping("/delete")
    public Result delete(@RequestParam(required = false) Integer bookId) {

        // 1. 获取 ID
        Integer userId = BaseContext.getCurrentId();

        // 2. 调用 Service
        cartService.deleteCartItem(userId, bookId);

        if (bookId == null) {
            return Result.success("购物车已清空");
        } else {
            return Result.success("删除成功");
        }
    }
}