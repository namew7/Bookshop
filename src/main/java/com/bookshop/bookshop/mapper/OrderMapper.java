package com.bookshop.bookshop.mapper;

import com.bookshop.bookshop.pojo.Order;
import com.bookshop.bookshop.pojo.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {

    // 写入主订单
    @Insert("INSERT INTO orders(order_id, user_id, total_price, status, create_time) " +
            "VALUES(#{orderId}, #{userId}, #{totalPrice}, #{status}, #{createTime})")
    void insertOrder(Order order);

    // 写入订单详情
    @Insert("INSERT INTO order_items(order_id, book_id, count, price) " +
            "VALUES(#{orderId}, #{bookId}, #{count}, #{price})")
    void insertOrderItem(OrderItem orderItem);
}