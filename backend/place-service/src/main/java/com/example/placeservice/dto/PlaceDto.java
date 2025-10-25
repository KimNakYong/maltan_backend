package com.example.placeservice.dto;

import com.example.placeservice.entity.Place;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 장소 DTO
 */
public class PlaceDto {

    private Long id;

    @NotBlank(message = "장소 이름은 필수입니다")
    @Size(max = 100, message = "장소 이름은 100자 이하여야 합니다")
    private String name;

    private String description;

    @NotBlank(message = "주소는 필수입니다")
    @Size(max = 300, message = "주소는 300자 이하여야 합니다")
    private String address;

    private String detailedAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phoneNumber;
    private String website;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Boolean isOpen24h;
    private String closedDays;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private Long viewCount;
    private Boolean isActive;
    private Long createdBy;

    @NotNull(message = "카테고리는 필수입니다")
    private Long categoryId;
    private CategoryDto category;

    private List<ReviewDto> reviews;
    private List<PhotoDto> photos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 생성자
    public PlaceDto() {}

    // 생성자
    public PlaceDto(String name, String address, Long categoryId) {
        this.name = name;
        this.address = address;
        this.categoryId = categoryId;
    }

    // Entity -> DTO 변환 (기본 정보만)
    public PlaceDto(Place place) {
        this.id = place.getId();
        this.name = place.getName();
        this.description = place.getDescription();
        this.address = place.getAddress();
        this.detailedAddress = place.getDetailedAddress();
        this.latitude = place.getLatitude();
        this.longitude = place.getLongitude();
        this.phoneNumber = place.getPhoneNumber();
        this.website = place.getWebsite();
        this.openingTime = place.getOpeningTime();
        this.closingTime = place.getClosingTime();
        this.isOpen24h = place.getIsOpen24h();
        this.closedDays = place.getClosedDays();
        this.averageRating = place.getAverageRating();
        this.reviewCount = place.getReviewCount();
        this.viewCount = place.getViewCount();
        this.isActive = place.getIsActive();
        this.createdBy = place.getCreatedBy();
        this.categoryId = place.getCategory() != null ? place.getCategory().getId() : null;
        this.category = place.getCategory() != null ? new CategoryDto(place.getCategory()) : null;
        this.createdAt = place.getCreatedAt();
        this.updatedAt = place.getUpdatedAt();
    }

    // Entity -> DTO 변환 (상세 정보 포함)
    public PlaceDto(Place place, boolean includeDetails) {
        this(place);
        if (includeDetails) {
            this.reviews = place.getReviews() != null ? 
                place.getReviews().stream()
                    .filter(review -> review.getIsActive())
                    .map(ReviewDto::new)
                    .collect(Collectors.toList()) : null;
            this.photos = place.getPhotos() != null ? 
                place.getPhotos().stream()
                    .map(PhotoDto::new)
                    .collect(Collectors.toList()) : null;
        }
    }

    // DTO -> Entity 변환
    public Place toEntity() {
        Place place = new Place();
        place.setId(this.id);
        place.setName(this.name);
        place.setDescription(this.description);
        place.setAddress(this.address);
        place.setDetailedAddress(this.detailedAddress);
        place.setLatitude(this.latitude);
        place.setLongitude(this.longitude);
        place.setPhoneNumber(this.phoneNumber);
        place.setWebsite(this.website);
        place.setOpeningTime(this.openingTime);
        place.setClosingTime(this.closingTime);
        place.setIsOpen24h(this.isOpen24h != null ? this.isOpen24h : false);
        place.setClosedDays(this.closedDays);
        place.setIsActive(this.isActive != null ? this.isActive : true);
        place.setCreatedBy(this.createdBy);
        return place;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDetailedAddress() {
        return detailedAddress;
    }

    public void setDetailedAddress(String detailedAddress) {
        this.detailedAddress = detailedAddress;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public Boolean getIsOpen24h() {
        return isOpen24h;
    }

    public void setIsOpen24h(Boolean isOpen24h) {
        this.isOpen24h = isOpen24h;
    }

    public String getClosedDays() {
        return closedDays;
    }

    public void setClosedDays(String closedDays) {
        this.closedDays = closedDays;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public CategoryDto getCategory() {
        return category;
    }

    public void setCategory(CategoryDto category) {
        this.category = category;
    }

    public List<ReviewDto> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDto> reviews) {
        this.reviews = reviews;
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
