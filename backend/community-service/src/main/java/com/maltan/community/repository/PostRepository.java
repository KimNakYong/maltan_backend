package com.maltan.community.repository;

import com.maltan.community.model.Post;
import com.maltan.community.model.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 게시글 목록 조회 (삭제되지 않은 것만)
    Page<Post> findByIsDeletedFalse(Pageable pageable);
    
    // 카테고리별 조회
    Page<Post> findByIsDeletedFalseAndCategory(String category, Pageable pageable);
    
    // 지역별 조회
    Page<Post> findByIsDeletedFalseAndRegionSi(String regionSi, Pageable pageable);
    
    Page<Post> findByIsDeletedFalseAndRegionSiAndRegionGu(
        String regionSi, String regionGu, Pageable pageable
    );
    
    Page<Post> findByIsDeletedFalseAndRegionSiAndRegionGuAndRegionDong(
        String regionSi, String regionGu, String regionDong, Pageable pageable
    );
    
    // 모집 게시글 조회
    Page<Post> findByIsDeletedFalseAndIsRecruitmentTrue(Pageable pageable);
    
    // 사용자별 게시글 조회
    Page<Post> findByIsDeletedFalseAndUserId(Long userId, Pageable pageable);
    
    // ID로 조회 (삭제되지 않은 것만)
    Optional<Post> findByIdAndIsDeletedFalse(Long id);
    
    // 조회수 증가 (Batch Update)
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = :viewCount WHERE p.id = :postId")
    void updateViewCount(@Param("postId") Long postId, @Param("viewCount") Integer viewCount);
    
    // 마감된 모집 게시글 찾기
    @Query("SELECT p FROM Post p WHERE p.isRecruitment = true " +
           "AND p.status = :status " +
           "AND p.recruitmentDeadline < :now")
    List<Post> findExpiredRecruitmentPosts(
        @Param("status") PostStatus status, 
        @Param("now") LocalDateTime now
    );
    
    // 복합 검색 (카테고리 + 지역) - 인기 게시글 우선 정렬
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:regionSi IS NULL OR p.regionSi = :regionSi) " +
           "AND (:regionGu IS NULL OR p.regionGu = :regionGu) " +
           "AND (:regionDong IS NULL OR p.regionDong = :regionDong) " +
           "AND (:isRecruitment IS NULL OR p.isRecruitment = :isRecruitment) " +
           "ORDER BY " +
           "CASE WHEN p.isPinned = true AND p.pinnedUntil > CURRENT_TIMESTAMP THEN 0 ELSE 1 END, " +
           "p.createdAt DESC")
    Page<Post> findByFilters(
        @Param("category") String category,
        @Param("regionSi") String regionSi,
        @Param("regionGu") String regionGu,
        @Param("regionDong") String regionDong,
        @Param("isRecruitment") Boolean isRecruitment,
        Pageable pageable
    );
    
    // 인기 게시글 조회 (좋아요 많은 순)
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false " +
           "AND p.createdAt > :since " +
           "ORDER BY p.likeCount DESC, p.viewCount DESC")
    Page<Post> findPopularPosts(@Param("since") LocalDateTime since, Pageable pageable);
    
    // 특정 기간 이후 가장 많은 추천을 받은 게시글 조회
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false " +
           "AND p.createdAt >= :since " +
           "ORDER BY p.likeCount DESC, p.viewCount DESC")
    List<Post> findTopByLikeCountSince(@Param("since") LocalDateTime since, int limit);
    
    // 고정 기간이 만료된 게시글 조회
    List<Post> findByIsPinnedTrueAndPinnedUntilBefore(LocalDateTime now);
}

