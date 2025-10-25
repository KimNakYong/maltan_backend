package com.example.placeservice.repository;

import com.example.placeservice.entity.Review;
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
 * 리뷰 Repository
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 장소별 활성화된 리뷰 목록 조회 (페이징)
     */
    @Query("SELECT r FROM Review r WHERE r.place.id = :placeId AND r.isActive = true ORDER BY r.createdAt DESC")
    Page<Review> findByPlaceIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("placeId") Long placeId, Pageable pageable);

    /**
     * 사용자별 리뷰 목록 조회 (페이징)
     */
    Page<Review> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 장소별 리뷰 개수 조회
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.place.id = :placeId AND r.isActive = true")
    long countByPlaceIdAndIsActiveTrue(@Param("placeId") Long placeId);

    /**
     * 사용자별 리뷰 개수 조회
     */
    long countByUserIdAndIsActiveTrue(Long userId);

    /**
     * 장소별 평균 평점 조회
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.place.id = :placeId AND r.isActive = true")
    Optional<BigDecimal> findAverageRatingByPlaceId(@Param("placeId") Long placeId);

    /**
     * 장소별 평점별 리뷰 개수 조회
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.place.id = :placeId AND r.isActive = true GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> findRatingDistributionByPlaceId(@Param("placeId") Long placeId);

    /**
     * 사용자가 특정 장소에 작성한 리뷰 조회
     */
    @Query("SELECT r FROM Review r WHERE r.place.id = :placeId AND r.userId = :userId AND r.isActive = true")
    Optional<Review> findByPlaceIdAndUserIdAndIsActiveTrue(@Param("placeId") Long placeId, @Param("userId") Long userId);

    /**
     * 사용자가 특정 장소에 리뷰를 작성했는지 확인
     */
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.place.id = :placeId AND r.userId = :userId AND r.isActive = true")
    boolean existsByPlaceIdAndUserIdAndIsActiveTrue(@Param("placeId") Long placeId, @Param("userId") Long userId);

    /**
     * 최신 리뷰 조회 (전체)
     */
    Page<Review> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 인기 리뷰 조회 (좋아요 수 기준)
     */
    Page<Review> findByIsActiveTrueOrderByLikeCountDescCreatedAtDesc(Pageable pageable);

    /**
     * 평점별 리뷰 조회
     */
    Page<Review> findByRatingAndIsActiveTrueOrderByCreatedAtDesc(BigDecimal rating, Pageable pageable);

    /**
     * 평점 범위별 리뷰 조회
     */
    Page<Review> findByRatingBetweenAndIsActiveTrueOrderByCreatedAtDesc(BigDecimal minRating, BigDecimal maxRating, Pageable pageable);

    /**
     * 리뷰 내용으로 검색
     */
    Page<Review> findByContentContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String keyword, Pageable pageable);

    /**
     * 장소별 최신 리뷰 조회 (제한된 개수)
     */
    @Query("SELECT r FROM Review r WHERE r.place.id = :placeId AND r.isActive = true ORDER BY r.createdAt DESC")
    List<Review> findTop5ByPlaceIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("placeId") Long placeId, Pageable pageable);

    /**
     * 장소별 인기 리뷰 조회 (좋아요 수 기준, 제한된 개수)
     */
    @Query("SELECT r FROM Review r WHERE r.place.id = :placeId AND r.isActive = true ORDER BY r.likeCount DESC, r.createdAt DESC")
    List<Review> findTop5ByPlaceIdAndIsActiveTrueOrderByLikeCountDescCreatedAtDesc(@Param("placeId") Long placeId, Pageable pageable);

    /**
     * 사진이 있는 리뷰 조회
     */
    @Query("SELECT r FROM Review r WHERE r.isActive = true AND SIZE(r.photos) > 0 ORDER BY r.createdAt DESC")
    Page<Review> findReviewsWithPhotos(Pageable pageable);

    /**
     * 장소별 사진이 있는 리뷰 조회
     */
    @Query("SELECT r FROM Review r WHERE r.place.id = :placeId AND r.isActive = true AND SIZE(r.photos) > 0 ORDER BY r.createdAt DESC")
    Page<Review> findReviewsWithPhotosByPlaceId(@Param("placeId") Long placeId, Pageable pageable);

    /**
     * 리뷰 좋아요 수 증가
     */
    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    /**
     * 리뷰 좋아요 수 감소
     */
    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount - 1 WHERE r.id = :id AND r.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    /**
     * 장소별 리뷰 통계 조회
     */
    @Query("SELECT " +
           "COUNT(r) as totalReviews, " +
           "AVG(r.rating) as averageRating, " +
           "SUM(CASE WHEN r.rating = 5 THEN 1 ELSE 0 END) as fiveStars, " +
           "SUM(CASE WHEN r.rating = 4 THEN 1 ELSE 0 END) as fourStars, " +
           "SUM(CASE WHEN r.rating = 3 THEN 1 ELSE 0 END) as threeStars, " +
           "SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END) as twoStars, " +
           "SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END) as oneStars " +
           "FROM Review r WHERE r.place.id = :placeId AND r.isActive = true")
    Object[] findReviewStatsByPlaceId(@Param("placeId") Long placeId);

    /**
     * 리뷰 상세 정보 조회 (사진 포함)
     */
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.photos WHERE r.id = :id AND r.isActive = true")
    Optional<Review> findByIdWithPhotos(@Param("id") Long id);
}
