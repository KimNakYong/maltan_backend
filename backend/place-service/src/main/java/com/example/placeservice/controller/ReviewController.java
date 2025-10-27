package com.example.placeservice.controller;

import com.example.placeservice.dto.ApiResponse;
import com.example.placeservice.dto.ReviewDto;
import com.example.placeservice.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 리뷰 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 장소별 리뷰 목록 조회 (페이징)
     */
    @GetMapping("/place/{placeId}")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getReviewsByPlace(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReviewDto> reviews = reviewService.getReviewsByPlace(placeId, pageable);
            return ResponseEntity.ok(ApiResponse.success("장소별 리뷰 조회 성공", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소별 리뷰 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 사용자별 리뷰 목록 조회 (페이징)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getReviewsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReviewDto> reviews = reviewService.getReviewsByUser(userId, pageable);
            return ResponseEntity.ok(ApiResponse.success("사용자별 리뷰 조회 성공", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자별 리뷰 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 리뷰 ID로 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewDto>> getReviewById(@PathVariable Long id) {
        try {
            return reviewService.getReviewById(id)
                    .map(review -> ResponseEntity.ok(ApiResponse.success("리뷰 조회 성공", review)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("리뷰를 찾을 수 없습니다: " + id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 리뷰 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(@Valid @RequestBody ReviewDto reviewDto) {
        try {
            ReviewDto createdReview = reviewService.createReview(reviewDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("리뷰 생성 성공", createdReview));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 생성 실패: " + e.getMessage()));
        }
    }

    /**
     * 리뷰 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewDto>> updateReview(
            @PathVariable Long id, 
            @Valid @RequestBody ReviewDto reviewDto) {
        try {
            ReviewDto updatedReview = reviewService.updateReview(id, reviewDto);
            return ResponseEntity.ok(ApiResponse.success("리뷰 수정 성공", updatedReview));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 수정 실패: " + e.getMessage()));
        }
    }

    /**
     * 리뷰 삭제 (비활성화)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long id, 
            @RequestParam Long userId) {
        try {
            reviewService.deleteReview(id, userId);
            return ResponseEntity.ok(ApiResponse.success("리뷰 삭제 성공"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 삭제 실패: " + e.getMessage()));
        }
    }

    /**
     * 리뷰 좋아요 추가
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<ReviewDto>> likeReview(@PathVariable Long id) {
        try {
            ReviewDto updatedReview = reviewService.likeReview(id);
            return ResponseEntity.ok(ApiResponse.success("리뷰 좋아요 성공", updatedReview));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 좋아요 실패: " + e.getMessage()));
        }
    }

    /**
     * 리뷰 좋아요 취소
     */
    @DeleteMapping("/{id}/like")
    public ResponseEntity<ApiResponse<ReviewDto>> unlikeReview(@PathVariable Long id) {
        try {
            ReviewDto updatedReview = reviewService.unlikeReview(id);
            return ResponseEntity.ok(ApiResponse.success("리뷰 좋아요 취소 성공", updatedReview));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 좋아요 취소 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소별 최신 리뷰 조회 (제한된 개수)
     */
    @GetMapping("/place/{placeId}/latest")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getLatestReviewsByPlace(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<ReviewDto> reviews = reviewService.getLatestReviewsByPlace(placeId, limit);
            return ResponseEntity.ok(ApiResponse.success("장소별 최신 리뷰 조회 성공", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소별 최신 리뷰 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소별 인기 리뷰 조회 (좋아요 수 기준)
     */
    @GetMapping("/place/{placeId}/popular")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getPopularReviewsByPlace(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<ReviewDto> reviews = reviewService.getPopularReviewsByPlace(placeId, limit);
            return ResponseEntity.ok(ApiResponse.success("장소별 인기 리뷰 조회 성공", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소별 인기 리뷰 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 전체 최신 리뷰 조회 (페이징)
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getLatestReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReviewDto> reviews = reviewService.getLatestReviews(pageable);
            return ResponseEntity.ok(ApiResponse.success("최신 리뷰 조회 성공", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("최신 리뷰 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 전체 인기 리뷰 조회 (페이징)
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getPopularReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReviewDto> reviews = reviewService.getPopularReviews(pageable);
            return ResponseEntity.ok(ApiResponse.success("인기 리뷰 조회 성공", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("인기 리뷰 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 평점별 리뷰 조회
     */
    @GetMapping("/rating/{rating}")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getReviewsByRating(
            @PathVariable BigDecimal rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReviewDto> reviews = reviewService.getReviewsByRating(rating, pageable);
            return ResponseEntity.ok(ApiResponse.success("평점별 리뷰 조회 성공", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("평점별 리뷰 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 평점 범위별 리뷰 조회
     */
    @GetMapping("/rating-range")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getReviewsByRatingRange(
            @RequestParam BigDecimal minRating,
            @RequestParam BigDecimal maxRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReviewDto> reviews = reviewService.getReviewsByRatingRange(minRating, maxRating, pageable);
            return ResponseEntity.ok(ApiResponse.success("평점 범위별 리뷰 조회 성공", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("평점 범위별 리뷰 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 리뷰 내용 검색
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> searchReviews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReviewDto> reviews = reviewService.searchReviews(keyword, pageable);
            return ResponseEntity.ok(ApiResponse.success("리뷰 검색 성공", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 검색 실패: " + e.getMessage()));
        }
    }

    /**
     * 사진이 있는 리뷰 조회
     */
    @GetMapping("/with-photos")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getReviewsWithPhotos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReviewDto> reviews = reviewService.getReviewsWithPhotos(pageable);
            return ResponseEntity.ok(ApiResponse.success("사진이 있는 리뷰 조회 성공", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사진이 있는 리뷰 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소별 사진이 있는 리뷰 조회
     */
    @GetMapping("/place/{placeId}/with-photos")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getReviewsWithPhotosByPlace(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReviewDto> reviews = reviewService.getReviewsWithPhotosByPlace(placeId, pageable);
            return ResponseEntity.ok(ApiResponse.success("장소별 사진이 있는 리뷰 조회 성공", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소별 사진이 있는 리뷰 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소별 리뷰 통계 조회
     */
    @GetMapping("/place/{placeId}/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReviewStatsByPlace(@PathVariable Long placeId) {
        try {
            Map<String, Object> stats = reviewService.getReviewStatsByPlace(placeId);
            return ResponseEntity.ok(ApiResponse.success("장소별 리뷰 통계 조회 성공", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소별 리뷰 통계 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소별 평균 평점 조회
     */
    @GetMapping("/place/{placeId}/average-rating")
    public ResponseEntity<ApiResponse<BigDecimal>> getAverageRatingByPlace(@PathVariable Long placeId) {
        try {
            BigDecimal averageRating = reviewService.getAverageRatingByPlace(placeId);
            return ResponseEntity.ok(ApiResponse.success("장소별 평균 평점 조회 성공", averageRating));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소별 평균 평점 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소별 리뷰 개수 조회
     */
    @GetMapping("/place/{placeId}/count")
    public ResponseEntity<ApiResponse<Long>> getReviewCountByPlace(@PathVariable Long placeId) {
        try {
            long count = reviewService.getReviewCountByPlace(placeId);
            return ResponseEntity.ok(ApiResponse.success("장소별 리뷰 개수 조회 성공", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소별 리뷰 개수 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 사용자별 리뷰 개수 조회
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<ApiResponse<Long>> getReviewCountByUser(@PathVariable Long userId) {
        try {
            long count = reviewService.getReviewCountByUser(userId);
            return ResponseEntity.ok(ApiResponse.success("사용자별 리뷰 개수 조회 성공", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자별 리뷰 개수 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 사용자가 특정 장소에 리뷰를 작성했는지 확인
     */
    @GetMapping("/place/{placeId}/user/{userId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> hasUserReviewedPlace(
            @PathVariable Long placeId, 
            @PathVariable Long userId) {
        try {
            boolean hasReviewed = reviewService.hasUserReviewedPlace(placeId, userId);
            return ResponseEntity.ok(ApiResponse.success("리뷰 작성 여부 확인 성공", hasReviewed));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 작성 여부 확인 실패: " + e.getMessage()));
        }
    }

    /**
     * 사용자의 특정 장소 리뷰 조회
     */
    @GetMapping("/place/{placeId}/user/{userId}")
    public ResponseEntity<ApiResponse<ReviewDto>> getUserReviewForPlace(
            @PathVariable Long placeId, 
            @PathVariable Long userId) {
        try {
            return reviewService.getUserReviewForPlace(placeId, userId)
                    .map(review -> ResponseEntity.ok(ApiResponse.success("사용자 리뷰 조회 성공", review)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("해당 장소에 작성한 리뷰가 없습니다.")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 리뷰 조회 실패: " + e.getMessage()));
        }
    }
}
