package com.example.placeservice.dto;

import com.example.placeservice.entity.Review;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 리뷰 DTO
 */
public class ReviewDto {

    private Long id;

    @NotNull(message = "평점은 필수입니다")
    @DecimalMin(value = "1.0", message = "평점은 1.0 이상이어야 합니다")
    @DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다")
    private BigDecimal rating;

    @Size(max = 1000, message = "리뷰 내용은 1000자 이하여야 합니다")
    private String content;

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    private String userName;
    private Boolean isActive;
    private Integer likeCount;

    @NotNull(message = "장소 ID는 필수입니다")
    private Long placeId;
    private String placeName;

    private List<PhotoDto> photos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 생성자
    public ReviewDto() {}

    // 생성자
    public ReviewDto(BigDecimal rating, String content, Long userId, String userName, Long placeId) {
        this.rating = rating;
        this.content = content;
        this.userId = userId;
        this.userName = userName;
        this.placeId = placeId;
    }

    // Entity -> DTO 변환
    public ReviewDto(Review review) {
        this.id = review.getId();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.userId = review.getUserId();
        this.userName = review.getUserName();
        this.isActive = review.getIsActive();
        this.likeCount = review.getLikeCount();
        this.placeId = review.getPlace() != null ? review.getPlace().getId() : null;
        this.placeName = review.getPlace() != null ? review.getPlace().getName() : null;
        this.photos = review.getPhotos() != null ? 
            review.getPhotos().stream()
                .map(PhotoDto::new)
                .collect(Collectors.toList()) : null;
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
    }

    // DTO -> Entity 변환
    public Review toEntity() {
        Review review = new Review();
        review.setId(this.id);
        review.setRating(this.rating);
        review.setContent(this.content);
        review.setUserId(this.userId);
        review.setUserName(this.userName);
        review.setIsActive(this.isActive != null ? this.isActive : true);
        review.setLikeCount(this.likeCount != null ? this.likeCount : 0);
        return review;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public List<PhotoDto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<PhotoDto> photos) {
        this.photos = photos;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
