package com.example.placeservice.repository;

import com.example.placeservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 활성화된 카테고리 목록 조회 (정렬 순서대로)
     */
    List<Category> findByIsActiveTrueOrderBySortOrderAscNameAsc();

    /**
     * 카테고리 이름으로 조회
     */
    Optional<Category> findByName(String name);

    /**
     * 카테고리 이름 중복 확인
     */
    boolean existsByName(String name);

    /**
     * 카테고리 이름 중복 확인 (수정 시 자신 제외)
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 활성화된 카테고리 개수 조회
     */
    long countByIsActiveTrue();

    /**
     * 카테고리별 장소 개수 포함 조회
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.places p WHERE c.isActive = true AND (p.isActive = true OR p.isActive IS NULL) ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findActiveCategoriesWithPlaces();

    /**
     * 정렬 순서로 카테고리 조회
     */
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * 최대 정렬 순서 조회
     */
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM Category c WHERE c.isActive = true")
    Integer findMaxSortOrder();
}
