package com.maltan.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationWithPlacesResponse {
    private String regionName;  // 추천 지역 이름
    private Double centerLatitude;  // 지도 중심 위도
    private Double centerLongitude;  // 지도 중심 경도
    private List<PlaceDto> nearbyPlaces;  // 근처 장소 목록
}

