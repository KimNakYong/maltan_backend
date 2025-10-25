package com.maltan.recommendation.controller;

import com.maltan.recommendation.dto.RecommendationDto;
import com.maltan.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/recommendation")
@RequiredArgsConstructor
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    /**
     * 사용자 기반 추천
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecommendationDto>> getRecommendationsForUser(
            @PathVariable Long userId
    ) {
        log.info("GET /recommendation/user/{}", userId);
        List<RecommendationDto> recommendations = recommendationService.getRecommendationsForUser(userId);
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * 지역 기반 추천
     */
    @GetMapping("/region")
    public ResponseEntity<List<RecommendationDto>> getRecommendationsByRegion(
            @RequestParam String regionSi,
            @RequestParam(required = false) String regionGu
    ) {
        log.info("GET /recommendation/region?regionSi={}&regionGu={}", regionSi, regionGu);
        List<RecommendationDto> recommendations = recommendationService
            .getRecommendationsByRegion(regionSi, regionGu != null ? regionGu : "");
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * 장소 타입별 추천
     */
    @GetMapping("/type/{placeType}")
    public ResponseEntity<List<RecommendationDto>> getRecommendationsByType(
            @PathVariable String placeType
    ) {
        log.info("GET /recommendation/type/{}", placeType);
        List<RecommendationDto> recommendations = recommendationService.getRecommendationsByType(placeType);
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * 인기 추천
     */
    @GetMapping("/popular")
    public ResponseEntity<List<RecommendationDto>> getPopularRecommendations(
            @RequestParam String regionSi
    ) {
        log.info("GET /recommendation/popular?regionSi={}", regionSi);
        List<RecommendationDto> recommendations = recommendationService.getPopularRecommendations(regionSi);
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * 헬스체크
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Recommendation Service is running");
    }
}

