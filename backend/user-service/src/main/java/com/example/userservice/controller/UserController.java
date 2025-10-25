package com.example.userservice.controller;

import com.example.userservice.dto.ApiResponse;
import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user/auth")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 사용자 등록
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            UserResponse userResponse = userService.registerUser(request);
            ApiResponse<UserResponse> response = ApiResponse.success("회원가입이 성공적으로 완료되었습니다.", userResponse);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            ApiResponse<UserResponse> response = ApiResponse.error(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 사용자명으로 사용자 조회
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        Optional<UserResponse> user = userService.findByUsername(username);
        if (user.isPresent()) {
            ApiResponse<UserResponse> response = ApiResponse.success(user.get());
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<UserResponse> response = ApiResponse.error("사용자를 찾을 수 없습니다", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * 이메일로 사용자 조회
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        Optional<UserResponse> user = userService.findByEmail(email);
        if (user.isPresent()) {
            ApiResponse<UserResponse> response = ApiResponse.success(user.get());
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<UserResponse> response = ApiResponse.error("사용자를 찾을 수 없습니다", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * 사용자 ID로 사용자 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        Optional<UserResponse> user = userService.findById(id);
        if (user.isPresent()) {
            ApiResponse<UserResponse> response = ApiResponse.success(user.get());
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<UserResponse> response = ApiResponse.error("사용자를 찾을 수 없습니다", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * 사용자명 존재 여부 확인
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkUsernameExists(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("exists", exists);
        ApiResponse<Map<String, Object>> response = ApiResponse.success(data);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 이메일 존재 여부 확인
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("exists", exists);
        ApiResponse<Map<String, Object>> response = ApiResponse.success(data);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 사용자 정보 업데이트
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, 
                                       @Valid @RequestBody UserRegistrationRequest request) {
        try {
            UserResponse userResponse = userService.updateUser(id, request);
            ApiResponse<UserResponse> response = ApiResponse.success("사용자 정보가 성공적으로 업데이트되었습니다.", userResponse);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<UserResponse> response = ApiResponse.error(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 사용자 상태 업데이트 (활성화/비활성화)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(@PathVariable Long id, 
                                             @RequestParam boolean enabled) {
        try {
            UserResponse userResponse = userService.updateUserStatus(id, enabled);
            ApiResponse<UserResponse> response = ApiResponse.success("사용자 상태가 성공적으로 업데이트되었습니다.", userResponse);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<UserResponse> response = ApiResponse.error(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 사용자 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            ApiResponse<String> response = ApiResponse.success("사용자가 성공적으로 삭제되었습니다", "삭제 완료");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<String> response = ApiResponse.error(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 헬스 체크
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> healthCheck() {
        Map<String, String> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "user-service");
        data.put("version", "1.0.0");
        ApiResponse<Map<String, String>> response = ApiResponse.success(data);
        return ResponseEntity.ok(response);
    }
}