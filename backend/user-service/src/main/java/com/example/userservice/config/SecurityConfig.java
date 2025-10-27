package com.example.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 * CORS는 Gateway에서 처리
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Gateway에서 CORS 처리하므로 개별 서비스에서는 비활성화
            .cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("OPTIONS", "/**").permitAll() // OPTIONS 요청 허용
                .requestMatchers("/user/auth/login", "/user/auth/register", "/user/auth/health").permitAll()
                .requestMatchers("/user/auth/check-username/**", "/user/auth/check-email/**").permitAll()
                .requestMatchers("/user/auth/username/**", "/user/auth/email/**").permitAll()
                .requestMatchers("/user/auth/{id}").permitAll()
                .requestMatchers("/user/auth/me/**").permitAll() // 현재 사용자 관련 엔드포인트 허용
                .requestMatchers("/user/auth/me").permitAll() // 현재 사용자 정보 조회/업데이트 허용
                .requestMatchers("/user/admin/**").permitAll() // 관리자 API (임시로 모두 허용, 나중에 ADMIN 권한 체크 추가)
                .requestMatchers("/test/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.disable());
        
        return http.build();
    }
}