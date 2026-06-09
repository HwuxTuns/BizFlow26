package com.example.bizflow.dto;

/**
 * Request body cho API refresh token.
 * Client gửi refreshToken để nhận accessToken mới.
 */
public class RefreshTokenRequest {

    private String refreshToken;

    public RefreshTokenRequest() {}

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
