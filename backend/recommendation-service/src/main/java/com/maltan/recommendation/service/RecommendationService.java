package com.maltan.recommendation.service;

import com.maltan.recommendation.dto.RecommendationDto;
import com.maltan.recommendation.entity.Recommendation;
import com.maltan.recommendation.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    
    private final RecommendationRepository recommendationRepository;
    
    /**
     * 사용자 선호 지역 기반 추천
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userRecommendations", key = "#userId")
    public List<RecommendationDto> getRecommendationsForUser(Long userId) {
        log.info("Getting recommendations for user: {}", userId);
        List<Recommendation> recommendations = recommendationRepository.findByUserIdOrderByScoreDesc(userId);
        return recommendations.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 지역별 추천
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "regionRecommendations", key = "#regionSi + '_' + #regionGu")
    public List<RecommendationDto> getRecommendationsByRegion(String regionSi, String regionGu) {
        log.info("Getting recommendations for region: {} {}", regionSi, regionGu);
        List<Recommendation> recommendations = recommendationRepository
            .findByRegionSiAndRegionGuOrderByScoreDesc(regionSi, regionGu);
        return recommendations.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 장소 타입별 추천
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "typeRecommendations", key = "#placeType")
    public List<RecommendationDto> getRecommendationsByType(String placeType) {
        log.info("Getting recommendations for type: {}", placeType);
        List<Recommendation> recommendations = recommendationRepository
            .findByPlaceTypeOrderByScoreDesc(placeType);
        return recommendations.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 인기 추천 (지역별 상위)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "popularRecommendations", key = "#regionSi")
    public List<RecommendationDto> getPopularRecommendations(String regionSi) {
        log.info("Getting popular recommendations for: {}", regionSi);
        List<Recommendation> recommendations = recommendationRepository.findTopByRegionSi(regionSi);
        return recommendations.stream()
            .limit(10) // 상위 10개만
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Entity to DTO 변환
     */
    private RecommendationDto convertToDto(Recommendation recommendation) {
        return RecommendationDto.builder()
            .id(recommendation.getId())
            .placeName(recommendation.getPlaceName())
            .placeType(recommendation.getPlaceType())
            .regionSi(recommendation.getRegionSi())
            .regionGu(recommendation.getRegionGu())
            .regionDong(recommendation.getRegionDong())
            .latitude(recommendation.getLatitude())
            .longitude(recommendation.getLongitude())
            .address(recommendation.getAddress())
            .description(recommendation.getDescription())
            .score(recommendation.getScore())
            .reason(recommendation.getReason())
            .createdAt(recommendation.getCreatedAt())
            .build();
    }
}

