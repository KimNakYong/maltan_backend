package com.example.userservice.dto;

/**
 * 인증 응답 DTO (로그인/회원가입 시 반환)
 */
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    private UserResponse user;
    
    // Constructors
    public AuthResponse() {}
    
    public AuthResponse(String accessToken, String refreshToken, UserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }
    
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public UserResponse getUser() {
        return user;
    }
    
    public void setUser(UserResponse user) {
        this.user = user;
    }
}

