package com.example.userservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 토큰 생성 및 검증 유틸리티
 */
@Component
public class JwtUtil {
    
    @Value("${jwt.secret:maltan-secret-key-for-jwt-token-generation-minimum-256-bits-required}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}") // 24시간 (밀리초)
    private Long expiration;
    
    @Value("${jwt.refresh-expiration:604800000}") // 7일 (밀리초)
    private Long refreshExpiration;
    
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    /**
     * Access Token 생성
     */
    public String generateAccessToken(String email, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        return createToken(claims, email, expiration);
    }
    
    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(String email) {
        return createToken(new HashMap<>(), email, refreshExpiration);
    }
    
    /**
     * 토큰 생성
     */
    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 토큰에서 이메일 추출
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }
    
    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }
    
    /**
     * 토큰에서 역할 추출
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }
    
    /**
     * 토큰에서 만료 시간 추출
     */
    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }
    
    /**
     * 토큰에서 Claims 추출
     */
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 토큰 만료 여부 확인
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * 토큰 유효성 검증
     */
    public Boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }
}

