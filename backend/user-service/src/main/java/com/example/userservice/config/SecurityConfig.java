package com.example.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("OPTIONS", "/**").permitAll() // OPTIONS 요청 허용
                .requestMatchers("/user/auth/login", "/user/auth/register", "/user/auth/health").permitAll()
                .requestMatchers("/user/auth/check-username/**", "/user/auth/check-email/**").permitAll()
                .requestMatchers("/user/auth/username/**", "/user/auth/email/**").permitAll()
                .requestMatchers("/user/auth/{id}").permitAll()
                .requestMatchers("/user/auth/me/**").permitAll() // 현재 사용자 관련 엔드포인트 허용
                .requestMatchers("/user/auth/me").permitAll() // 현재 사용자 정보 조회/업데이트 허용
                .requestMatchers("/test/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.disable());
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "https://reserved-jolie-untastily.ngrok-free.app",
            "https://reserved-jolie-untastily.ngrok-free.dev",
            "https://*.ngrok-free.app",
            "https://*.ngrok-free.dev",
            "http://localhost:3000",
            "http://localhost:8080",
            "http://127.0.0.1:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}