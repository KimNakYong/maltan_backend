package com.example.placeservice.repository;

import com.example.placeservice.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 장소 Repository
 */
@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    /**
     * 활성화된 장소 목록 조회 (페이징)
     */
    Page<Place> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 카테고리별 활성화된 장소 목록 조회 (페이징)
     */
    @Query("SELECT p FROM Place p WHERE p.category.id = :categoryId AND p.isActive = true ORDER BY p.createdAt DESC")
    Page<Place> findByCategoryIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 장소 이름으로 검색 (페이징)
     */
    Page<Place> findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String name, Pageable pageable);

    /**
     * 주소로 검색 (페이징)
     */
    Page<Place> findByAddressContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String address, Pageable pageable);

    /**
     * 장소 이름 또는 주소로 검색 (페이징)
     */
    @Query("SELECT p FROM Place p WHERE p.isActive = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY p.createdAt DESC")
    Page<Place> findByNameOrAddressContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 카테고리와 키워드로 검색 (페이징)
     */
    @Query("SELECT p FROM Place p WHERE p.category.id = :categoryId AND p.isActive = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY p.createdAt DESC")
    Page<Place> findByCategoryAndKeyword(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 평점 범위로 검색 (페이징)
     */
    Page<Place> findByAverageRatingBetweenAndIsActiveTrueOrderByAverageRatingDesc(BigDecimal minRating, BigDecimal maxRating, Pageable pageable);

    /**
     * 인기 장소 조회 (조회수 기준, 페이징)
     */
    Page<Place> findByIsActiveTrueOrderByViewCountDescCreatedAtDesc(Pageable pageable);


    /**
     * 평점 높은 장소 조회 (페이징)
     */
    Page<Place> findByIsActiveTrueAndReviewCountGreaterThanOrderByAverageRatingDescReviewCountDesc(Integer minReviewCount, Pageable pageable);

    /**
     * 위치 기반 검색 (반경 내 장소)
     */
    @Query("SELECT p FROM Place p WHERE p.isActive = true AND " +
           "6371 * acos(cos(radians(:latitude)) * cos(radians(p.latitude)) * " +
           "cos(radians(p.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(p.latitude))) <= :radius " +
           "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(p.latitude)) * " +
           "cos(radians(p.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(p.latitude))))")
    List<Place> findNearbyPlaces(@Param("latitude") BigDecimal latitude, 
                                 @Param("longitude") BigDecimal longitude, 
                                 @Param("radius") Double radius);

    /**
     * 사용자가 생성한 장소 목록 조회
     */
    Page<Place> findByCreatedByOrderByCreatedAtDesc(Long createdBy, Pageable pageable);

    /**
     * 장소 조회수 증가
     */
    @Modifying
    @Query("UPDATE Place p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 카테고리별 장소 개수 조회
     */
    @Query("SELECT COUNT(p) FROM Place p WHERE p.category.id = :categoryId AND p.isActive = true")
    long countByCategoryIdAndIsActiveTrue(@Param("categoryId") Long categoryId);

    /**
     * 전체 활성화된 장소 개수 조회
     */
    long countByIsActiveTrue();

    /**
     * 장소 이름 중복 확인
     */
    boolean existsByNameAndAddressAndIsActiveTrue(String name, String address);

    /**
     * 장소 이름 중복 확인 (수정 시 자신 제외)
     */
    boolean existsByNameAndAddressAndIsActiveTrueAndIdNot(String name, String address, Long id);

    /**
     * 추천 장소 조회 (평점과 리뷰 수 기준)
     */
    @Query("SELECT p FROM Place p WHERE p.isActive = true AND p.averageRating >= :minRating AND p.reviewCount >= :minReviewCount ORDER BY (p.averageRating * 0.7 + (p.reviewCount * 0.1)) DESC, p.viewCount DESC")
    List<Place> findRecommendedPlaces(@Param("minRating") BigDecimal minRating, 
                                     @Param("minReviewCount") Integer minReviewCount, 
                                     Pageable pageable);

    /**
     * 카테고리별 인기 장소 조회
     */
    @Query("SELECT p FROM Place p WHERE p.category.id = :categoryId AND p.isActive = true ORDER BY p.viewCount DESC, p.averageRating DESC")
    List<Place> findTop10ByCategoryIdAndIsActiveTrueOrderByViewCountDescAverageRatingDesc(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 최근 등록된 장소 조회
     */
    List<Place> findTop10ByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * 장소 상세 정보 조회 (리뷰와 사진 포함)
     */
    @Query("SELECT p FROM Place p LEFT JOIN FETCH p.reviews r LEFT JOIN FETCH p.photos ph WHERE p.id = :id AND p.isActive = true AND (r.isActive = true OR r.isActive IS NULL)")
    Optional<Place> findByIdWithDetails(@Param("id") Long id);
}
