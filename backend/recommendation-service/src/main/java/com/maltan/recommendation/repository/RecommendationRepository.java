package com.maltan.recommendation.repository;

import com.maltan.recommendation.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    
    // 사용자별 추천 조회
    List<Recommendation> findByUserIdOrderByScoreDesc(Long userId);
    
    // 지역별 추천 조회
    List<Recommendation> findByRegionSiAndRegionGuOrderByScoreDesc(String regionSi, String regionGu);
    
    // 장소 타입별 추천 조회
    List<Recommendation> findByPlaceTypeOrderByScoreDesc(String placeType);
    
    // 점수 기준 상위 N개 조회
    @Query("SELECT r FROM Recommendation r WHERE r.regionSi = :regionSi ORDER BY r.score DESC")
    List<Recommendation> findTopByRegionSi(@Param("regionSi") String regionSi);
}

