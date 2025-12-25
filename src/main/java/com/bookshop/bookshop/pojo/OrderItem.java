package com.bookshop.bookshop.pojo;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderItem implements Serializable {
    private Integer id;
    private String orderId;
    private Integer bookId;
    private Integer count;
    private BigDecimal price; // 下单时的单价

    public OrderItem() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", orderId='" + orderId + '\'' +
                ", bookId=" + bookId +
                ", count=" + count +
                ", price=" + price +
                '}';
    }
}