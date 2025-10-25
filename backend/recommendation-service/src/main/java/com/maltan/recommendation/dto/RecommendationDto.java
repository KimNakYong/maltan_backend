package com.maltan.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {
    private Long id;
    private String placeName;
    private String placeType;
    private String regionSi;
    private String regionGu;
    private String regionDong;
    private Double latitude;
    private Double longitude;
    private String address;
    private String description;
    private Double score;
    private String reason;
    private LocalDateTime createdAt;
}

