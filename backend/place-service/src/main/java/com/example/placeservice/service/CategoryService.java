package com.example.placeservice.service;

import com.example.placeservice.dto.CategoryDto;
import com.example.placeservice.entity.Category;
import com.example.placeservice.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 카테고리 서비스
 */
@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 모든 활성화된 카테고리 조회
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(CategoryDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 ID로 조회
     */
    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryDto::new);
    }

    /**
     * 카테고리 이름으로 조회
     */
    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .map(CategoryDto::new);
    }

    /**
     * 카테고리 생성
     */
    public CategoryDto createCategory(CategoryDto categoryDto) {
        // 중복 이름 검사
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new RuntimeException("이미 존재하는 카테고리 이름입니다: " + categoryDto.getName());
        }

        Category category = categoryDto.toEntity();
        
        // 정렬 순서 설정 (마지막 순서 + 1)
        if (category.getSortOrder() == null || category.getSortOrder() == 0) {
            Integer maxSortOrder = categoryRepository.findMaxSortOrder();
            category.setSortOrder(maxSortOrder + 1);
        }

        Category savedCategory = categoryRepository.save(category);
        return new CategoryDto(savedCategory);
    }

    /**
     * 카테고리 수정
     */
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + id));

        // 중복 이름 검사 (자신 제외)
        if (categoryRepository.existsByNameAndIdNot(categoryDto.getName(), id)) {
            throw new RuntimeException("이미 존재하는 카테고리 이름입니다: " + categoryDto.getName());
        }

        // 기존 카테고리 정보 업데이트
        existingCategory.setName(categoryDto.getName());
        existingCategory.setDescription(categoryDto.getDescription());
        existingCategory.setIconUrl(categoryDto.getIconUrl());
        
        if (categoryDto.getIsActive() != null) {
            existingCategory.setIsActive(categoryDto.getIsActive());
        }
        
        if (categoryDto.getSortOrder() != null) {
            existingCategory.setSortOrder(categoryDto.getSortOrder());
        }

        Category updatedCategory = categoryRepository.save(existingCategory);
        return new CategoryDto(updatedCategory);
    }

    /**
     * 카테고리 삭제 (비활성화)
     */
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + id));

        // 해당 카테고리에 장소가 있는지 확인
        if (!category.getPlaces().isEmpty()) {
            // 완전 삭제 대신 비활성화
            category.setIsActive(false);
            categoryRepository.save(category);
        } else {
            // 장소가 없으면 완전 삭제
            categoryRepository.delete(category);
        }
    }

    /**
     * 카테고리 활성화/비활성화
     */
    public CategoryDto toggleCategoryStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + id));

        category.setIsActive(!category.getIsActive());
        Category updatedCategory = categoryRepository.save(category);
        return new CategoryDto(updatedCategory);
    }

    /**
     * 카테고리 정렬 순서 변경
     */
    public List<CategoryDto> updateCategorySortOrder(List<Long> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            Long categoryId = categoryIds.get(i);
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + categoryId));
            
            category.setSortOrder(i + 1);
            categoryRepository.save(category);
        }

        return getAllActiveCategories();
    }

    /**
     * 카테고리별 장소 개수 포함 조회
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesWithPlaceCount() {
        return categoryRepository.findActiveCategoriesWithPlaces()
                .stream()
                .map(category -> {
                    CategoryDto dto = new CategoryDto(category);
                    dto.setPlaceCount(category.getPlaces().size());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 이름 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isNameExists(String name) {
        return categoryRepository.existsByName(name);
    }

    /**
     * 카테고리 이름 중복 확인 (수정 시)
     */
    @Transactional(readOnly = true)
    public boolean isNameExistsForUpdate(String name, Long id) {
        return categoryRepository.existsByNameAndIdNot(name, id);
    }

    /**
     * 활성화된 카테고리 개수 조회
     */
    @Transactional(readOnly = true)
    public long getActiveCategoryCount() {
        return categoryRepository.countByIsActiveTrue();
    }

    /**
     * 카테고리 검색
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> searchCategories(String keyword) {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .filter(category -> 
                    category.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                    (category.getDescription() != null && 
                     category.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                )
                .map(CategoryDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 아이콘 URL 업데이트
     */
    public CategoryDto updateCategoryIcon(Long id, String iconUrl) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + id));

        category.setIconUrl(iconUrl);
        Category updatedCategory = categoryRepository.save(category);
        return new CategoryDto(updatedCategory);
    }
}
