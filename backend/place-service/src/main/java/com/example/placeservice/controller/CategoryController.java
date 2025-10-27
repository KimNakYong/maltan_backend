package com.example.placeservice.controller;

import com.example.placeservice.dto.ApiResponse;
import com.example.placeservice.dto.CategoryDto;
import com.example.placeservice.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 모든 활성화된 카테고리 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        try {
            List<CategoryDto> categories = categoryService.getAllActiveCategories();
            return ResponseEntity.ok(ApiResponse.success("카테고리 목록 조회 성공", categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 목록 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 카테고리별 장소 개수 포함 조회
     */
    @GetMapping("/with-count")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategoriesWithCount() {
        try {
            List<CategoryDto> categories = categoryService.getCategoriesWithPlaceCount();
            return ResponseEntity.ok(ApiResponse.success("카테고리 목록 조회 성공", categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 목록 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 카테고리 ID로 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(@PathVariable Long id) {
        try {
            return categoryService.getCategoryById(id)
                    .map(category -> ResponseEntity.ok(ApiResponse.success("카테고리 조회 성공", category)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("카테고리를 찾을 수 없습니다: " + id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 카테고리 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        try {
            CategoryDto createdCategory = categoryService.createCategory(categoryDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("카테고리 생성 성공", createdCategory));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 생성 실패: " + e.getMessage()));
        }
    }

    /**
     * 카테고리 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody CategoryDto categoryDto) {
        try {
            CategoryDto updatedCategory = categoryService.updateCategory(id, categoryDto);
            return ResponseEntity.ok(ApiResponse.success("카테고리 수정 성공", updatedCategory));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 수정 실패: " + e.getMessage()));
        }
    }

    /**
     * 카테고리 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("카테고리 삭제 성공"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 삭제 실패: " + e.getMessage()));
        }
    }

    /**
     * 카테고리 활성화/비활성화 토글
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<CategoryDto>> toggleCategoryStatus(@PathVariable Long id) {
        try {
            CategoryDto updatedCategory = categoryService.toggleCategoryStatus(id);
            return ResponseEntity.ok(ApiResponse.success("카테고리 상태 변경 성공", updatedCategory));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 상태 변경 실패: " + e.getMessage()));
        }
    }

    /**
     * 카테고리 정렬 순서 변경
     */
    @PutMapping("/sort-order")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> updateSortOrder(@RequestBody List<Long> categoryIds) {
        try {
            List<CategoryDto> updatedCategories = categoryService.updateCategorySortOrder(categoryIds);
            return ResponseEntity.ok(ApiResponse.success("카테고리 정렬 순서 변경 성공", updatedCategories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 정렬 순서 변경 실패: " + e.getMessage()));
        }
    }

    /**
     * 카테고리 검색
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> searchCategories(@RequestParam String keyword) {
        try {
            List<CategoryDto> categories = categoryService.searchCategories(keyword);
            return ResponseEntity.ok(ApiResponse.success("카테고리 검색 성공", categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 검색 실패: " + e.getMessage()));
        }
    }

    /**
     * 카테고리 이름 중복 확인
     */
    @GetMapping("/check-name")
    public ResponseEntity<ApiResponse<Boolean>> checkNameExists(
            @RequestParam String name,
            @RequestParam(required = false) Long excludeId) {
        try {
            boolean exists = excludeId != null ? 
                categoryService.isNameExistsForUpdate(name, excludeId) :
                categoryService.isNameExists(name);
            
            return ResponseEntity.ok(ApiResponse.success("이름 중복 확인 완료", exists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("이름 중복 확인 실패: " + e.getMessage()));
        }
    }

    /**
     * 카테고리 아이콘 업데이트
     */
    @PatchMapping("/{id}/icon")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategoryIcon(
            @PathVariable Long id, 
            @RequestParam String iconUrl) {
        try {
            CategoryDto updatedCategory = categoryService.updateCategoryIcon(id, iconUrl);
            return ResponseEntity.ok(ApiResponse.success("카테고리 아이콘 업데이트 성공", updatedCategory));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 아이콘 업데이트 실패: " + e.getMessage()));
        }
    }

    /**
     * 활성화된 카테고리 개수 조회
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getActiveCategoryCount() {
        try {
            long count = categoryService.getActiveCategoryCount();
            return ResponseEntity.ok(ApiResponse.success("카테고리 개수 조회 성공", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 개수 조회 실패: " + e.getMessage()));
        }
    }
}
