package com.bookshop.bookshop.pojo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Book {
    private Integer id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price; // 涉及金额通常用 BigDecimal
    private Integer quantity;
    private String coverImage;
    private String description;
    private LocalDateTime createTime;

    // ================= 手动 Getter/Setter =================
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}