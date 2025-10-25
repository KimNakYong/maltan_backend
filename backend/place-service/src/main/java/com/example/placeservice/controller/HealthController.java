package com.example.placeservice.controller;

import com.example.placeservice.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 헬스 체크 및 시스템 정보 컨트롤러
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    /**
     * 헬스 체크
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 데이터베이스 연결 확인
            try (Connection connection = dataSource.getConnection()) {
                health.put("database", "UP");
                health.put("databaseUrl", connection.getMetaData().getURL());
            }
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("databaseError", e.getMessage());
        }
        
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "place-service");
        
        return ResponseEntity.ok(ApiResponse.success("헬스 체크 성공", health));
    }

    /**
     * 서비스 정보 조회
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getServiceInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("service", "Place Service");
        info.put("description", "장소 및 리뷰 관리 서비스");
        info.put("version", buildProperties != null ? buildProperties.getVersion() : "1.0.0");
        info.put("buildTime", buildProperties != null ? buildProperties.getTime() : null);
        
        // API 엔드포인트 정보
        Map<String, Object> endpoints = new HashMap<>();
        endpoints.put("categories", Map.of(
            "GET /api/categories", "카테고리 목록 조회",
            "POST /api/categories", "카테고리 생성",
            "PUT /api/categories/{id}", "카테고리 수정",
            "DELETE /api/categories/{id}", "카테고리 삭제"
        ));
        
        endpoints.put("places", Map.of(
            "GET /api/places", "장소 목록 조회",
            "GET /api/places/{id}", "장소 상세 조회",
            "POST /api/places", "장소 생성",
            "PUT /api/places/{id}", "장소 수정",
            "DELETE /api/places/{id}", "장소 삭제",
            "GET /api/places/search", "장소 검색",
            "GET /api/places/popular", "인기 장소 조회",
            "GET /api/places/nearby", "주변 장소 조회"
        ));
        
        endpoints.put("reviews", Map.of(
            "GET /api/reviews/place/{placeId}", "장소별 리뷰 조회",
            "POST /api/reviews", "리뷰 생성",
            "PUT /api/reviews/{id}", "리뷰 수정",
            "DELETE /api/reviews/{id}", "리뷰 삭제",
            "POST /api/reviews/{id}/like", "리뷰 좋아요",
            "GET /api/reviews/search", "리뷰 검색"
        ));
        
        endpoints.put("files", Map.of(
            "POST /api/files/upload", "파일 업로드",
            "POST /api/files/upload/place/{placeId}", "장소 사진 업로드",
            "POST /api/files/upload/review/{reviewId}", "리뷰 사진 업로드",
            "DELETE /api/files/{photoId}", "파일 삭제"
        ));
        
        info.put("endpoints", endpoints);
        info.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(ApiResponse.success("서비스 정보 조회 성공", info));
    }

    /**
     * 서비스 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getServiceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 실제 구현에서는 각 서비스에서 통계 데이터를 가져와야 함
        stats.put("totalCategories", "N/A");
        stats.put("totalPlaces", "N/A");
        stats.put("totalReviews", "N/A");
        stats.put("totalPhotos", "N/A");
        stats.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(ApiResponse.success("서비스 통계 조회 성공", stats));
    }
}
