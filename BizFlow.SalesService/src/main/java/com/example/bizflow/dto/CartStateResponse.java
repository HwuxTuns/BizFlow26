package com.example.bizflow.dto;

import java.time.LocalDateTime;

public class CartStateResponse {
    private Long userId;
    private String username;
    private Object state;
    private LocalDateTime updatedAt;

    public CartStateResponse() {
    }

    public CartStateResponse(Long userId, String username, Object state, LocalDateTime updatedAt) {
        this.userId = userId;
        this.username = username;
        this.state = state;
        this.updatedAt = updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Object getState() {
        return state;
    }

    public void setState(Object state) {
        this.state = state;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
