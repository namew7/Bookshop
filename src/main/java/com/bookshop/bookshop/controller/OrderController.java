package com.bookshop.bookshop.controller;

import com.bookshop.bookshop.pojo.Result;
import com.bookshop.bookshop.service.OrderService;
import com.bookshop.bookshop.utils.BaseContext; // 导入你的工具类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 下单接口
     * 不需要传 userId，也不需要 HttpSession
     * URL: POST /order/create?bookId=101&count=1
     */
    @PostMapping("/create")
    public Result createOrder(@RequestParam Integer bookId,
                              @RequestParam Integer count) {

        // 1. 直接从 ThreadLocal 获取当前登录用户 ID
        Integer currentUserId = BaseContext.getCurrentId();

        // 2. 调用 Service
        // Service 层去处理 Lua 扣减库存、发 Stream 消息等逻辑
        String orderId = orderService.createOrder(currentUserId, bookId, count);

        return Result.success(orderId);
    }
}