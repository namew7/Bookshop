package com.bookshop.bookshop.service;

import com.bookshop.bookshop.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class OrderService {

    @Autowired private StringRedisTemplate stringRedisTemplate;

    // Lua 脚本对象
    private DefaultRedisScript<Long> stockScript;

    @PostConstruct
    public void init() {
        stockScript = new DefaultRedisScript<>();
        stockScript.setResultType(Long.class);
        stockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/stock_deduct.lua")));
    }

    /**
     * 创建订单 (只操作 Redis，极速返回)
     */
    public String createOrder(Integer userId, Integer bookId, Integer count) {
        // 1. 【新增】参数防御性校验
        if (bookId == null || bookId <= 0) {
            throw new ServiceException("非法商品 ID");
        }
        if (count == null || count <= 0) {
            throw new ServiceException("下单数量必须大于 0");
        }
        if (count > 100) {
            // 比如防止有人恶意刷接口，一次买10000本
            throw new ServiceException("单次限购 100 本");
        }
        // 1. 执行 Lua 脚本扣库存
        List<String> keys = Collections.singletonList("stock:book:" + bookId);
        Long result = stringRedisTemplate.execute(stockScript, keys, String.valueOf(count));

        // 2. 判断 Lua 结果
        if (result == null || result == -1) {
            throw new ServiceException("商品未上架或缓存异常");
        }
        if (result == -2) {
            throw new ServiceException("购买数量参数非法");
        }
        if (result == 0) {
            throw new ServiceException("手慢了，库存不足！");
        }

        // 3. 生成订单 ID
        String orderId = UUID.randomUUID().toString().replace("-", "");

        // 4. 发送消息到 Redis Stream
        // 消息结构: [orderId, userId, bookId, count]
        Map<String, String> msg = new HashMap<>();
        msg.put("orderId", orderId);
        msg.put("userId", String.valueOf(userId));
        msg.put("bookId", String.valueOf(bookId));
        msg.put("count", String.valueOf(count));

        stringRedisTemplate.opsForStream().add("stream.orders", msg);

        // 5. 返回订单号给前端
        return orderId;
    }
}