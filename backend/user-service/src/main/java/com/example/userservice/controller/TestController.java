package com.example.userservice.controller;

import com.example.userservice.dto.ApiResponse;
import com.example.userservice.dto.PreferredRegionDto;
import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 테스트용 샘플 데이터 생성
     */
    @PostMapping("/create-sample-data")
    public ResponseEntity<ApiResponse<String>> createSampleData() {
        try {
            // 샘플 사용자 데이터 생성
            List<UserRegistrationRequest> sampleUsers = new ArrayList<>();
            
            // 샘플 선호 지역 데이터
            List<PreferredRegionDto> adminRegions = List.of(
                new PreferredRegionDto("seoul", "서울특별시", "gangnam", "강남구", 1)
            );
            
            List<PreferredRegionDto> user1Regions = List.of(
                new PreferredRegionDto("seoul", "서울특별시", "gangnam", "강남구", 1),
                new PreferredRegionDto("seoul", "서울특별시", "mapo", "마포구", 2)
            );
            
            List<PreferredRegionDto> user2Regions = List.of(
                new PreferredRegionDto("busan", "부산광역시", "haeundae", "해운대구", 1)
            );
            
            List<PreferredRegionDto> user3Regions = List.of(
                new PreferredRegionDto("seoul", "서울특별시", "jongno", "종로구", 1),
                new PreferredRegionDto("seoul", "서울특별시", "jung", "중구", 2),
                new PreferredRegionDto("seoul", "서울특별시", "yongsan", "용산구", 3)
            );
            
            sampleUsers.add(new UserRegistrationRequest(
                "admin@example.com", "admin123", "관리자", "010-0000-0000", adminRegions
            ));
            
            sampleUsers.add(new UserRegistrationRequest(
                "user1@example.com", "password123", "홍길동", "010-1111-1111", user1Regions
            ));
            
            sampleUsers.add(new UserRegistrationRequest(
                "user2@example.com", "password123", "김철수", "010-2222-2222", user2Regions
            ));
            
            sampleUsers.add(new UserRegistrationRequest(
                "user3@example.com", "password123", "이영희", "010-3333-3333", user3Regions
            ));
            
            int createdCount = 0;
            for (UserRegistrationRequest request : sampleUsers) {
                try {
                    userService.registerUser(request);
                    createdCount++;
                } catch (Exception e) {
                    // 이미 존재하는 사용자는 무시
                }
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("message", "샘플 데이터 생성 완료");
            data.put("createdCount", createdCount);
            data.put("totalRequested", sampleUsers.size());
            
            ApiResponse<String> response = ApiResponse.success("샘플 데이터가 성공적으로 생성되었습니다.", 
                "생성된 사용자 수: " + createdCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error("샘플 데이터 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 모든 사용자 조회 (테스트용)
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers() {
        try {
            // 실제 구현에서는 UserService에 findAll 메서드를 추가해야 함
            Map<String, Object> data = new HashMap<>();
            data.put("message", "모든 사용자 조회 기능은 UserService에 findAll 메서드를 추가하여 구현해야 합니다.");
            
            List<Map<String, Object>> users = new ArrayList<>();
            users.add(data);
            
            ApiResponse<List<Map<String, Object>>> response = ApiResponse.success(users);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ApiResponse<List<Map<String, Object>>> response = ApiResponse.error("사용자 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API 테스트 엔드포인트
     */
    @GetMapping("/api-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApiInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "User Service");
        data.put("version", "1.0.0");
        data.put("endpoints", List.of(
            "POST /api/users/register - 회원가입",
            "GET /api/users/{id} - 사용자 조회",
            "GET /api/users/username/{username} - 사용자명으로 조회",
            "GET /api/users/email/{email} - 이메일로 조회",
            "GET /api/users/check-username/{username} - 사용자명 중복 확인",
            "GET /api/users/check-email/{email} - 이메일 중복 확인",
            "PUT /api/users/{id} - 사용자 정보 수정",
            "DELETE /api/users/{id} - 사용자 삭제",
            "GET /api/users/health - 헬스 체크"
        ));
        data.put("cors", "ngrok URL 허용됨");
        data.put("database", "MySQL");
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success(data);
        return ResponseEntity.ok(response);
    }
}
