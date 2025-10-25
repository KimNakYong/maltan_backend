package com.example.userservice.controller;

import com.example.userservice.dto.ApiResponse;
import com.example.userservice.dto.ServiceMetrics;
import com.example.userservice.dto.SystemMetrics;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.DockerMetricsService;
import com.example.userservice.service.SystemMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminUserController {
    
    private final UserRepository userRepository;
    private final SystemMetricsService systemMetricsService;
    private final DockerMetricsService dockerMetricsService;
    
    /**
     * 사용자 목록 조회 (페이징)
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        try {
            String[] sortParams = sort.split(",");
            Sort sortOrder = Sort.by(
                sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortParams[0]
            );
            
            Pageable pageable = PageRequest.of(page, size, sortOrder);
            Page<User> usersPage;
            
            if (search != null && !search.trim().isEmpty()) {
                usersPage = userRepository.findByEmailContainingOrNameContaining(search, search, pageable);
            } else {
                usersPage = userRepository.findAll(pageable);
            }
            
            List<UserResponse> users = usersPage.getContent().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("currentPage", usersPage.getNumber());
            response.put("totalItems", usersPage.getTotalElements());
            response.put("totalPages", usersPage.getTotalPages());
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("사용자 목록 조회 실패: " + e.getMessage(), 500));
        }
    }
    
    /**
     * 특정 사용자 조회
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            return ResponseEntity.ok(ApiResponse.success(new UserResponse(user)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("사용자 조회 실패: " + e.getMessage(), 500));
        }
    }
    
    /**
     * 사용자 역할 변경 (USER <-> ADMIN)
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role
    ) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            User.Role newRole = User.Role.valueOf(role.toUpperCase());
            user.setRole(newRole);
            userRepository.save(user);
            
            return ResponseEntity.ok(ApiResponse.success("역할이 변경되었습니다.", new UserResponse(user)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("잘못된 역할입니다. USER 또는 ADMIN만 가능합니다.", 400));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("역할 변경 실패: " + e.getMessage(), 500));
        }
    }
    
    /**
     * 사용자 활성화/비활성화
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean enabled
    ) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            user.setEnabled(enabled);
            userRepository.save(user);
            
            String message = enabled ? "사용자가 활성화되었습니다." : "사용자가 비활성화되었습니다.";
            return ResponseEntity.ok(ApiResponse.success(message, new UserResponse(user)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("상태 변경 실패: " + e.getMessage(), 500));
        }
    }
    
    /**
     * 사용자 삭제
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            userRepository.delete(user);
            
            return ResponseEntity.ok(ApiResponse.success("사용자가 삭제되었습니다.", "삭제 완료"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("사용자 삭제 실패: " + e.getMessage(), 500));
        }
    }
    
    /**
     * 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        try {
            long totalUsers = userRepository.count();
            long adminUsers = userRepository.countByRole(User.Role.ADMIN);
            long activeUsers = userRepository.countByIsEnabled(true);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("adminUsers", adminUsers);
            stats.put("activeUsers", activeUsers);
            stats.put("inactiveUsers", totalUsers - activeUsers);
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("통계 조회 실패: " + e.getMessage(), 500));
        }
    }
    
    /**
     * 시스템 메트릭 조회
     */
    @GetMapping("/system/metrics")
    public ResponseEntity<ApiResponse<SystemMetrics>> getSystemMetrics() {
        try {
            SystemMetrics metrics = systemMetricsService.getSystemMetrics();
            return ResponseEntity.ok(ApiResponse.success(metrics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("시스템 메트릭 조회 실패: " + e.getMessage(), 500));
        }
    }
    
    /**
     * 서비스별 메트릭 조회
     */
    @GetMapping("/services/metrics")
    public ResponseEntity<ApiResponse<List<ServiceMetrics>>> getServicesMetrics() {
        try {
            List<ServiceMetrics> metrics = dockerMetricsService.getAllServiceMetrics();
            return ResponseEntity.ok(ApiResponse.success(metrics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서비스 메트릭 조회 실패: " + e.getMessage(), 500));
        }
    }
}

