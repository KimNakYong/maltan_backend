package com.example.userservice.controller;

import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 다른 마이크로서비스에서 호출하는 Internal API
 */
@RestController
@RequestMapping("/api/user/internal")
@RequiredArgsConstructor
public class InternalUserController {
    
    private final UserRepository userRepository;
    
    /**
     * 사용자 ID로 이름 조회
     */
    @GetMapping("/users/{userId}/name")
    public ResponseEntity<Map<String, String>> getUserName(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        Map<String, String> response = new HashMap<>();
        response.put("userId", String.valueOf(userId));
        response.put("name", user.getName());
        
        return ResponseEntity.ok(response);
    }
}

