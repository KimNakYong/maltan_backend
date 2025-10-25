package com.example.placeservice.service;

import com.example.placeservice.dto.ReviewDto;
import com.example.placeservice.entity.Place;
import com.example.placeservice.entity.Review;
import com.example.placeservice.repository.PlaceRepository;
import com.example.placeservice.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 리뷰 서비스
 */
@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PlaceRepository placeRepository;

    /**
     * 장소별 리뷰 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsByPlace(Long placeId, Pageable pageable) {
        return reviewRepository.findByPlaceIdAndIsActiveTrueOrderByCreatedAtDesc(placeId, pageable)
                .map(ReviewDto::new);
    }

    /**
     * 사용자별 리뷰 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsByUser(Long userId, Pageable pageable) {
        return reviewRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId, pageable)
                .map(ReviewDto::new);
    }

    /**
     * 리뷰 ID로 상세 조회
     */
    @Transactional(readOnly = true)
    public Optional<ReviewDto> getReviewById(Long id) {
        return reviewRepository.findByIdWithPhotos(id)
                .map(ReviewDto::new);
    }

    /**
     * 리뷰 생성
     */
    public ReviewDto createReview(ReviewDto reviewDto) {
        // 장소 존재 확인
        Place place = placeRepository.findById(reviewDto.getPlaceId())
                .orElseThrow(() -> new RuntimeException("장소를 찾을 수 없습니다: " + reviewDto.getPlaceId()));

        if (!place.getIsActive()) {
            throw new RuntimeException("비활성화된 장소입니다: " + place.getName());
        }

        // 중복 리뷰 확인 (한 사용자가 같은 장소에 여러 리뷰 작성 방지)
        if (reviewRepository.existsByPlaceIdAndUserIdAndIsActiveTrue(reviewDto.getPlaceId(), reviewDto.getUserId())) {
            throw new RuntimeException("이미 해당 장소에 리뷰를 작성하셨습니다.");
        }

        Review review = reviewDto.toEntity();
        review.setPlace(place);

        Review savedReview = reviewRepository.save(review);

        // 장소의 평균 평점 업데이트
        updatePlaceRating(reviewDto.getPlaceId());

        return new ReviewDto(savedReview);
    }

    /**
     * 리뷰 수정
     */
    public ReviewDto updateReview(Long id, ReviewDto reviewDto) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다: " + id));

        // 작성자 확인
        if (!existingReview.getUserId().equals(reviewDto.getUserId())) {
            throw new RuntimeException("리뷰 수정 권한이 없습니다.");
        }

        // 리뷰 정보 업데이트
        if (reviewDto.getRating() != null) {
            existingReview.setRating(reviewDto.getRating());
        }
        if (reviewDto.getContent() != null) {
            existingReview.setContent(reviewDto.getContent());
        }

        Review updatedReview = reviewRepository.save(existingReview);

        // 장소의 평균 평점 업데이트
        updatePlaceRating(existingReview.getPlace().getId());

        return new ReviewDto(updatedReview);
    }

    /**
     * 리뷰 삭제 (비활성화)
     */
    public void deleteReview(Long id, Long userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다: " + id));

        // 작성자 확인
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("리뷰 삭제 권한이 없습니다.");
        }

        review.setIsActive(false);
        reviewRepository.save(review);

        // 장소의 평균 평점 업데이트
        updatePlaceRating(review.getPlace().getId());
    }

    /**
     * 리뷰 좋아요 추가
     */
    public ReviewDto likeReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다: " + id));

        if (!review.getIsActive()) {
            throw new RuntimeException("비활성화된 리뷰입니다.");
        }

        reviewRepository.incrementLikeCount(id);
        
        // 업데이트된 리뷰 조회
        Review updatedReview = reviewRepository.findById(id).orElseThrow();
        return new ReviewDto(updatedReview);
    }

    /**
     * 리뷰 좋아요 취소
     */
    public ReviewDto unlikeReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다: " + id));

        if (!review.getIsActive()) {
            throw new RuntimeException("비활성화된 리뷰입니다.");
        }

        reviewRepository.decrementLikeCount(id);
        
        // 업데이트된 리뷰 조회
        Review updatedReview = reviewRepository.findById(id).orElseThrow();
        return new ReviewDto(updatedReview);
    }

    /**
     * 장소별 최신 리뷰 조회 (제한된 개수)
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getLatestReviewsByPlace(Long placeId, int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(limit, 100)); // 최대 100개로 제한
        List<Review> reviews = limit <= 5 ? 
            reviewRepository.findTop5ByPlaceIdAndIsActiveTrueOrderByCreatedAtDesc(placeId, PageRequest.of(0, 5)) :
            reviewRepository.findByPlaceIdAndIsActiveTrueOrderByCreatedAtDesc(placeId, pageable).getContent();
        
        return reviews.stream()
                .map(ReviewDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 장소별 인기 리뷰 조회 (좋아요 수 기준)
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getPopularReviewsByPlace(Long placeId, int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(limit, 100)); // 최대 100개로 제한
        List<Review> reviews = limit <= 5 ? 
            reviewRepository.findTop5ByPlaceIdAndIsActiveTrueOrderByLikeCountDescCreatedAtDesc(placeId, PageRequest.of(0, 5)) :
            reviewRepository.findByPlaceIdAndIsActiveTrueOrderByCreatedAtDesc(placeId, pageable).getContent();
        
        return reviews.stream()
                .map(ReviewDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 전체 최신 리뷰 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getLatestReviews(Pageable pageable) {
        return reviewRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable)
                .map(ReviewDto::new);
    }

    /**
     * 전체 인기 리뷰 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getPopularReviews(Pageable pageable) {
        return reviewRepository.findByIsActiveTrueOrderByLikeCountDescCreatedAtDesc(pageable)
                .map(ReviewDto::new);
    }

    /**
     * 평점별 리뷰 조회
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsByRating(BigDecimal rating, Pageable pageable) {
        return reviewRepository.findByRatingAndIsActiveTrueOrderByCreatedAtDesc(rating, pageable)
                .map(ReviewDto::new);
    }

    /**
     * 평점 범위별 리뷰 조회
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsByRatingRange(BigDecimal minRating, BigDecimal maxRating, Pageable pageable) {
        return reviewRepository.findByRatingBetweenAndIsActiveTrueOrderByCreatedAtDesc(minRating, maxRating, pageable)
                .map(ReviewDto::new);
    }

    /**
     * 리뷰 내용 검색
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> searchReviews(String keyword, Pageable pageable) {
        return reviewRepository.findByContentContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(keyword, pageable)
                .map(ReviewDto::new);
    }

    /**
     * 사진이 있는 리뷰 조회
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsWithPhotos(Pageable pageable) {
        return reviewRepository.findReviewsWithPhotos(pageable)
                .map(ReviewDto::new);
    }

    /**
     * 장소별 사진이 있는 리뷰 조회
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsWithPhotosByPlace(Long placeId, Pageable pageable) {
        return reviewRepository.findReviewsWithPhotosByPlaceId(placeId, pageable)
                .map(ReviewDto::new);
    }

    /**
     * 장소별 리뷰 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getReviewStatsByPlace(Long placeId) {
        Object[] stats = reviewRepository.findReviewStatsByPlaceId(placeId);
        
        return Map.of(
            "totalReviews", stats[0] != null ? stats[0] : 0L,
            "averageRating", stats[1] != null ? stats[1] : BigDecimal.ZERO,
            "fiveStars", stats[2] != null ? stats[2] : 0L,
            "fourStars", stats[3] != null ? stats[3] : 0L,
            "threeStars", stats[4] != null ? stats[4] : 0L,
            "twoStars", stats[5] != null ? stats[5] : 0L,
            "oneStars", stats[6] != null ? stats[6] : 0L
        );
    }

    /**
     * 장소별 평균 평점 조회
     */
    @Transactional(readOnly = true)
    public BigDecimal getAverageRatingByPlace(Long placeId) {
        return reviewRepository.findAverageRatingByPlaceId(placeId)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 장소별 리뷰 개수 조회
     */
    @Transactional(readOnly = true)
    public long getReviewCountByPlace(Long placeId) {
        return reviewRepository.countByPlaceIdAndIsActiveTrue(placeId);
    }

    /**
     * 사용자별 리뷰 개수 조회
     */
    @Transactional(readOnly = true)
    public long getReviewCountByUser(Long userId) {
        return reviewRepository.countByUserIdAndIsActiveTrue(userId);
    }

    /**
     * 사용자가 특정 장소에 리뷰를 작성했는지 확인
     */
    @Transactional(readOnly = true)
    public boolean hasUserReviewedPlace(Long placeId, Long userId) {
        return reviewRepository.existsByPlaceIdAndUserIdAndIsActiveTrue(placeId, userId);
    }

    /**
     * 사용자의 특정 장소 리뷰 조회
     */
    @Transactional(readOnly = true)
    public Optional<ReviewDto> getUserReviewForPlace(Long placeId, Long userId) {
        return reviewRepository.findByPlaceIdAndUserIdAndIsActiveTrue(placeId, userId)
                .map(ReviewDto::new);
    }

    /**
     * 장소의 평균 평점 업데이트
     */
    private void updatePlaceRating(Long placeId) {
        Place place = placeRepository.findById(placeId).orElse(null);
        if (place != null) {
            place.updateAverageRating();
            placeRepository.save(place);
        }
    }
}
