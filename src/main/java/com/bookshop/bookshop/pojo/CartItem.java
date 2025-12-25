package com.bookshop.bookshop.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
public class CartItem implements Serializable {
    // === 数据库映射字段 ===
    private Integer id;
    private Integer userId;
    private Integer bookId;
    private Integer count;

    // === Redis 冗余字段 (用于前端展示) ===
    private String bookName;
    private BigDecimal price;
    private BigDecimal totalPrice;

    // 1. 必须提供无参构造器 (Jackson 反序列化需要)
    public CartItem() {
    }

    // 2. 手动生成 Getter 和 Setter 方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }


    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    // toString 也稍微改一下类型显示
    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", count=" + count +
                ", price=" + price +
                ", totalPrice=" + totalPrice +
                '}';
    }
}