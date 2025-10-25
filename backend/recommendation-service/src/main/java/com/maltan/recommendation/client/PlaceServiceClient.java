package com.maltan.recommendation.client;

import com.maltan.recommendation.dto.PlaceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${place.service.url:http://localhost:8082}")
    private String placeServiceUrl;
    
    /**
     * 특정 좌표 근처의 장소 조회
     * @param latitude 위도
     * @param longitude 경도
     * @param radius 반경 (km)
     * @return 근처 장소 목록
     */
    public List<PlaceDto> getNearbyPlaces(Double latitude, Double longitude, Double radius) {
        try {
            String url = String.format("%s/api/places/nearby?latitude=%f&longitude=%f&radius=%f",
                placeServiceUrl, latitude, longitude, radius);
            
            ResponseEntity<List<PlaceDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PlaceDto>>() {}
            );
            
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            log.error("Place Service 호출 실패: lat={}, lng={}, radius={}", latitude, longitude, radius, e);
            // Place Service가 아직 구현되지 않았거나 에러 발생 시 빈 리스트 반환
            return new ArrayList<>();
        }
    }
    
    /**
     * 지역별 장소 조회
     * @param region 지역명 (예: "강남구")
     * @return 해당 지역의 장소 목록
     */
    public List<PlaceDto> getPlacesByRegion(String region) {
        try {
            String url = String.format("%s/api/places?region=%s", placeServiceUrl, region);
            
            ResponseEntity<List<PlaceDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PlaceDto>>() {}
            );
            
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            log.error("Place Service 호출 실패: region={}", region, e);
            return new ArrayList<>();
        }
    }
}

