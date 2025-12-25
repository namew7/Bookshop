package com.bookshop.bookshop.service;

import com.bookshop.bookshop.mapper.BookMapper;
import com.bookshop.bookshop.mapper.OrderMapper;
import com.bookshop.bookshop.pojo.Book;
import com.bookshop.bookshop.pojo.Order;
import com.bookshop.bookshop.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class OrderPersistenceService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private BookMapper bookMapper; // 需要查一下书的单价

    /**
     * 真正落库逻辑 (运行在后台线程中)
     */
    @Transactional(rollbackFor = Exception.class) // 确保事务生效
    public void createOrderInDB(String orderId, Integer userId, Integer bookId, Integer count) {

        // 1. 查图书信息 (为了获取当前价格)
        Book book = bookMapper.findById(bookId);

        // 2. 组装主订单
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setCreateTime(new Date());
        // 计算总价
        order.setTotalPrice(book.getPrice().multiply(BigDecimal.valueOf(count)));

        orderMapper.insertOrder(order);

        // 3. 组装订单详情
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setBookId(bookId);
        item.setCount(count);
        item.setPrice(book.getPrice()); // 记录下单时的单价

        orderMapper.insertOrderItem(item);

        System.out.println("[MySQL] 订单落库成功: " + orderId);
    }
}