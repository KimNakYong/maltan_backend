package com.example.placeservice.dto;

import com.example.placeservice.entity.Photo;

import java.time.LocalDateTime;

/**
 * 사진 DTO
 */
public class PhotoDto {

    private Long id;
    private String originalName;
    private String storedName;
    private String filePath;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private Boolean isMain;
    private Integer sortOrder;
    private Long uploadedBy;
    private Long placeId;
    private Long reviewId;
    private LocalDateTime createdAt;

    // 기본 생성자
    public PhotoDto() {}

    // 생성자
    public PhotoDto(String originalName, String storedName, String filePath, Long fileSize, String contentType) {
        this.originalName = originalName;
        this.storedName = storedName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }

    // Entity -> DTO 변환
    public PhotoDto(Photo photo) {
        this.id = photo.getId();
        this.originalName = photo.getOriginalName();
        this.storedName = photo.getStoredName();
        this.filePath = photo.getFilePath();
        this.fileSize = photo.getFileSize();
        this.contentType = photo.getContentType();
        this.isMain = photo.getIsMain();
        this.sortOrder = photo.getSortOrder();
        this.uploadedBy = photo.getUploadedBy();
        this.placeId = photo.getPlace() != null ? photo.getPlace().getId() : null;
        this.reviewId = photo.getReview() != null ? photo.getReview().getId() : null;
        this.createdAt = photo.getCreatedAt();
    }

    // DTO -> Entity 변환
    public Photo toEntity() {
        Photo photo = new Photo();
        photo.setId(this.id);
        photo.setOriginalName(this.originalName);
        photo.setStoredName(this.storedName);
        photo.setFilePath(this.filePath);
        photo.setFileSize(this.fileSize);
        photo.setContentType(this.contentType);
        photo.setIsMain(this.isMain != null ? this.isMain : false);
        photo.setSortOrder(this.sortOrder != null ? this.sortOrder : 0);
        photo.setUploadedBy(this.uploadedBy);
        return photo;
    }

    // 파일 URL 생성
    public void generateFileUrl(String baseUrl) {
        if (this.filePath != null) {
            this.fileUrl = baseUrl + "/" + this.filePath;
        }
    }

    // 파일 크기를 사람이 읽기 쉬운 형태로 변환
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        long size = fileSize;
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }

    // 이미지 파일 여부 확인
    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoredName() {
        return storedName;
    }

    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Boolean getIsMain() {
        return isMain;
    }

    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Long uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
