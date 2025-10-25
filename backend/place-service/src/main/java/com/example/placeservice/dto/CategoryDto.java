package com.example.placeservice.dto;

import com.example.placeservice.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 카테고리 DTO
 */
public class CategoryDto {

    private Long id;

    @NotBlank(message = "카테고리 이름은 필수입니다")
    @Size(max = 50, message = "카테고리 이름은 50자 이하여야 합니다")
    private String name;

    @Size(max = 200, message = "설명은 200자 이하여야 합니다")
    private String description;

    private String iconUrl;
    private Boolean isActive;
    private Integer sortOrder;
    private Integer placeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 생성자
    public CategoryDto() {}

    // 생성자
    public CategoryDto(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Entity -> DTO 변환
    public CategoryDto(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.iconUrl = category.getIconUrl();
        this.isActive = category.getIsActive();
        this.sortOrder = category.getSortOrder();
        this.placeCount = category.getPlaces() != null ? category.getPlaces().size() : 0;
        this.createdAt = category.getCreatedAt();
        this.updatedAt = category.getUpdatedAt();
    }

    // DTO -> Entity 변환
    public Category toEntity() {
        Category category = new Category();
        category.setId(this.id);
        category.setName(this.name);
        category.setDescription(this.description);
        category.setIconUrl(this.iconUrl);
        category.setIsActive(this.isActive != null ? this.isActive : true);
        category.setSortOrder(this.sortOrder != null ? this.sortOrder : 0);
        return category;
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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getPlaceCount() {
        return placeCount;
    }

    public void setPlaceCount(Integer placeCount) {
        this.placeCount = placeCount;
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
