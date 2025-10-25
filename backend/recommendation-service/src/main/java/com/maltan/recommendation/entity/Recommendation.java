package com.maltan.recommendation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_region", columnList = "region_si, region_gu"),
    @Index(name = "idx_score", columnList = "score")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "place_name", nullable = false, length = 200)
    private String placeName;
    
    @Column(name = "place_type", length = 50)
    private String placeType; // 맛집, 카페, 관광지 등
    
    @Column(name = "region_si", nullable = false, length = 50)
    private String regionSi;
    
    @Column(name = "region_gu", length = 50)
    private String regionGu;
    
    @Column(name = "region_dong", length = 50)
    private String regionDong;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "address", length = 500)
    private String address;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "score")
    private Double score; // 추천 점수
    
    @Column(name = "reason", length = 500)
    private String reason; // 추천 이유
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

