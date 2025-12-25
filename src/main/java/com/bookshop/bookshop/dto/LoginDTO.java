package com.bookshop.bookshop.dto;

import java.io.Serializable;

public class LoginDTO implements Serializable {
    private String username;
    private String password;

    // === 手动生成 Getter / Setter ===
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}