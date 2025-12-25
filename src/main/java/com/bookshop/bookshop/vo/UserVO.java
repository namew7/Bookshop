package com.bookshop.bookshop.vo;

import java.io.Serializable;

public class UserVO implements Serializable {
    private Integer id;
    private String username;
    private String email;

    // === 手动生成 Getter / Setter ===
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}