package com.example.bizflow.dto;

public class PayOrderRequest {
    private String method;
    private String token;

    public PayOrderRequest() {
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
