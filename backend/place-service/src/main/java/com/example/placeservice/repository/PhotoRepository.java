package com.example.placeservice.repository;

import com.example.placeservice.entity.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사진 Repository
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    /**
     * 장소별 사진 목록 조회 (정렬 순서대로)
     */
    @Query("SELECT p FROM Photo p WHERE p.place.id = :placeId ORDER BY p.sortOrder ASC, p.createdAt DESC")
    List<Photo> findByPlaceIdOrderBySortOrderAscCreatedAtDesc(@Param("placeId") Long placeId);

    /**
     * 리뷰별 사진 목록 조회 (정렬 순서대로)
     */
    @Query("SELECT p FROM Photo p WHERE p.review.id = :reviewId ORDER BY p.sortOrder ASC, p.createdAt DESC")
    List<Photo> findByReviewIdOrderBySortOrderAscCreatedAtDesc(@Param("reviewId") Long reviewId);

    /**
     * 장소의 메인 사진 조회
     */
    @Query("SELECT p FROM Photo p WHERE p.place.id = :placeId AND p.isMain = true")
    Optional<Photo> findByPlaceIdAndIsMainTrue(@Param("placeId") Long placeId);

    /**
     * 장소별 사진 개수 조회
     */
    @Query("SELECT COUNT(p) FROM Photo p WHERE p.place.id = :placeId")
    long countByPlaceId(@Param("placeId") Long placeId);

    /**
     * 리뷰별 사진 개수 조회
     */
    @Query("SELECT COUNT(p) FROM Photo p WHERE p.review.id = :reviewId")
    long countByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 사용자가 업로드한 사진 목록 조회
     */
    Page<Photo> findByUploadedByOrderByCreatedAtDesc(Long uploadedBy, Pageable pageable);

    /**
     * 사용자가 업로드한 사진 개수 조회
     */
    long countByUploadedBy(Long uploadedBy);

    /**
     * 장소별 최신 사진 조회 (제한된 개수)
     */
    @Query("SELECT p FROM Photo p WHERE p.place.id = :placeId ORDER BY p.createdAt DESC")
    List<Photo> findTop10ByPlaceIdOrderByCreatedAtDesc(@Param("placeId") Long placeId, Pageable pageable);

    /**
     * 전체 최신 사진 조회 (페이징)
     */
    Page<Photo> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 파일 경로로 사진 조회
     */
    Optional<Photo> findByFilePath(String filePath);

    /**
     * 저장된 파일명으로 사진 조회
     */
    Optional<Photo> findByStoredName(String storedName);

    /**
     * 장소별 사진 목록 조회 (페이징)
     */
    @Query("SELECT p FROM Photo p WHERE p.place.id = :placeId ORDER BY p.isMain DESC, p.sortOrder ASC, p.createdAt DESC")
    Page<Photo> findByPlaceIdOrderByIsMainDescSortOrderAscCreatedAtDesc(@Param("placeId") Long placeId, Pageable pageable);

    /**
     * 리뷰별 사진 목록 조회 (페이징)
     */
    @Query("SELECT p FROM Photo p WHERE p.review.id = :reviewId ORDER BY p.sortOrder ASC, p.createdAt DESC")
    Page<Photo> findByReviewIdOrderBySortOrderAscCreatedAtDesc(@Param("reviewId") Long reviewId, Pageable pageable);

    /**
     * 특정 콘텐츠 타입의 사진 조회
     */
    List<Photo> findByContentTypeStartingWithOrderByCreatedAtDesc(String contentTypePrefix);

    /**
     * 이미지 파일만 조회
     */
    @Query("SELECT p FROM Photo p WHERE p.contentType LIKE 'image/%' ORDER BY p.createdAt DESC")
    Page<Photo> findImageFiles(Pageable pageable);

    /**
     * 장소별 이미지 파일만 조회
     */
    @Query("SELECT p FROM Photo p WHERE p.place.id = :placeId AND p.contentType LIKE 'image/%' ORDER BY p.isMain DESC, p.sortOrder ASC, p.createdAt DESC")
    List<Photo> findImageFilesByPlaceId(@Param("placeId") Long placeId);

    /**
     * 리뷰별 이미지 파일만 조회
     */
    @Query("SELECT p FROM Photo p WHERE p.review.id = :reviewId AND p.contentType LIKE 'image/%' ORDER BY p.sortOrder ASC, p.createdAt DESC")
    List<Photo> findImageFilesByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 파일 크기별 사진 조회
     */
    List<Photo> findByFileSizeBetweenOrderByCreatedAtDesc(Long minSize, Long maxSize);

    /**
     * 큰 파일 조회 (특정 크기 이상)
     */
    List<Photo> findByFileSizeGreaterThanOrderByFileSizeDesc(Long minSize);

    /**
     * 장소별 메인 사진이 아닌 사진들 조회
     */
    @Query("SELECT p FROM Photo p WHERE p.place.id = :placeId AND p.isMain = false ORDER BY p.sortOrder ASC, p.createdAt DESC")
    List<Photo> findByPlaceIdAndIsMainFalseOrderBySortOrderAscCreatedAtDesc(@Param("placeId") Long placeId);

    /**
     * 장소의 다음 정렬 순서 조회
     */
    @Query("SELECT COALESCE(MAX(p.sortOrder), 0) + 1 FROM Photo p WHERE p.place.id = :placeId")
    Integer findNextSortOrderByPlaceId(@Param("placeId") Long placeId);

    /**
     * 리뷰의 다음 정렬 순서 조회
     */
    @Query("SELECT COALESCE(MAX(p.sortOrder), 0) + 1 FROM Photo p WHERE p.review.id = :reviewId")
    Integer findNextSortOrderByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 전체 파일 크기 합계 조회
     */
    @Query("SELECT COALESCE(SUM(p.fileSize), 0) FROM Photo p")
    Long getTotalFileSize();

    /**
     * 사용자별 파일 크기 합계 조회
     */
    @Query("SELECT COALESCE(SUM(p.fileSize), 0) FROM Photo p WHERE p.uploadedBy = :userId")
    Long getTotalFileSizeByUser(@Param("userId") Long userId);
}
