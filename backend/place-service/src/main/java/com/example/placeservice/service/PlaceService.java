package com.example.placeservice.service;

import com.example.placeservice.dto.PlaceDto;
import com.example.placeservice.entity.Category;
import com.example.placeservice.entity.Place;
import com.example.placeservice.repository.CategoryRepository;
import com.example.placeservice.repository.PlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 장소 서비스
 */
@Service
@Transactional
public class PlaceService {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 모든 활성화된 장소 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<PlaceDto> getAllActivePlaces(Pageable pageable) {
        return placeRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable)
                .map(PlaceDto::new);
    }

    /**
     * 장소 ID로 상세 조회
     */
    @Transactional(readOnly = true)
    public Optional<PlaceDto> getPlaceById(Long id) {
        Optional<Place> place = placeRepository.findByIdWithDetails(id);
        if (place.isPresent()) {
            // 조회수 증가
            placeRepository.incrementViewCount(id);
            return Optional.of(new PlaceDto(place.get(), true));
        }
        return Optional.empty();
    }

    /**
     * 장소 기본 정보 조회 (상세 정보 제외)
     */
    @Transactional(readOnly = true)
    public Optional<PlaceDto> getPlaceBasicInfo(Long id) {
        return placeRepository.findById(id)
                .filter(place -> place.getIsActive())
                .map(PlaceDto::new);
    }

    /**
     * 카테고리별 장소 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<PlaceDto> getPlacesByCategory(Long categoryId, Pageable pageable) {
        return placeRepository.findByCategoryIdAndIsActiveTrueOrderByCreatedAtDesc(categoryId, pageable)
                .map(PlaceDto::new);
    }

    /**
     * 장소 검색 (이름 또는 주소)
     */
    @Transactional(readOnly = true)
    public Page<PlaceDto> searchPlaces(String keyword, Pageable pageable) {
        return placeRepository.findByNameOrAddressContaining(keyword, pageable)
                .map(PlaceDto::new);
    }

    /**
     * 카테고리와 키워드로 장소 검색
     */
    @Transactional(readOnly = true)
    public Page<PlaceDto> searchPlacesByCategoryAndKeyword(Long categoryId, String keyword, Pageable pageable) {
        return placeRepository.findByCategoryAndKeyword(categoryId, keyword, pageable)
                .map(PlaceDto::new);
    }

    /**
     * 장소 생성
     */
    public PlaceDto createPlace(PlaceDto placeDto) {
        // 카테고리 존재 확인
        Category category = categoryRepository.findById(placeDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + placeDto.getCategoryId()));

        if (!category.getIsActive()) {
            throw new RuntimeException("비활성화된 카테고리입니다: " + category.getName());
        }

        // 중복 장소 확인 (같은 이름과 주소)
        if (placeRepository.existsByNameAndAddressAndIsActiveTrue(placeDto.getName(), placeDto.getAddress())) {
            throw new RuntimeException("이미 등록된 장소입니다: " + placeDto.getName() + " - " + placeDto.getAddress());
        }

        Place place = placeDto.toEntity();
        place.setCategory(category);

        Place savedPlace = placeRepository.save(place);
        return new PlaceDto(savedPlace);
    }

    /**
     * 장소 수정
     */
    public PlaceDto updatePlace(Long id, PlaceDto placeDto) {
        Place existingPlace = placeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("장소를 찾을 수 없습니다: " + id));

        // 카테고리 변경 시 존재 확인
        if (placeDto.getCategoryId() != null && !placeDto.getCategoryId().equals(existingPlace.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(placeDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + placeDto.getCategoryId()));
            
            if (!newCategory.getIsActive()) {
                throw new RuntimeException("비활성화된 카테고리입니다: " + newCategory.getName());
            }
            
            existingPlace.setCategory(newCategory);
        }

        // 중복 장소 확인 (수정 시 자신 제외)
        if (placeRepository.existsByNameAndAddressAndIsActiveTrueAndIdNot(
                placeDto.getName(), placeDto.getAddress(), id)) {
            throw new RuntimeException("이미 등록된 장소입니다: " + placeDto.getName() + " - " + placeDto.getAddress());
        }

        // 기존 장소 정보 업데이트
        updatePlaceFields(existingPlace, placeDto);

        Place updatedPlace = placeRepository.save(existingPlace);
        return new PlaceDto(updatedPlace);
    }

    /**
     * 장소 삭제 (비활성화)
     */
    public void deletePlace(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("장소를 찾을 수 없습니다: " + id));

        place.setIsActive(false);
        placeRepository.save(place);
    }

    /**
     * 장소 활성화/비활성화
     */
    public PlaceDto togglePlaceStatus(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("장소를 찾을 수 없습니다: " + id));

        place.setIsActive(!place.getIsActive());
        Place updatedPlace = placeRepository.save(place);
        return new PlaceDto(updatedPlace);
    }

    /**
     * 인기 장소 조회 (조회수 기준)
     */
    @Transactional(readOnly = true)
    public Page<PlaceDto> getPopularPlaces(Pageable pageable) {
        return placeRepository.findByIsActiveTrueOrderByViewCountDescCreatedAtDesc(pageable)
                .map(PlaceDto::new);
    }

    /**
     * 최신 장소 조회
     */
    @Transactional(readOnly = true)
    public Page<PlaceDto> getLatestPlaces(Pageable pageable) {
        return placeRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable)
                .map(PlaceDto::new);
    }

    /**
     * 평점 높은 장소 조회
     */
    @Transactional(readOnly = true)
    public Page<PlaceDto> getTopRatedPlaces(Integer minReviewCount, Pageable pageable) {
        return placeRepository.findByIsActiveTrueAndReviewCountGreaterThanOrderByAverageRatingDescReviewCountDesc(
                minReviewCount != null ? minReviewCount : 5, pageable)
                .map(PlaceDto::new);
    }

    /**
     * 추천 장소 조회
     */
    @Transactional(readOnly = true)
    public List<PlaceDto> getRecommendedPlaces(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return placeRepository.findRecommendedPlaces(
                BigDecimal.valueOf(4.0), 3, pageable)
                .stream()
                .map(PlaceDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 위치 기반 주변 장소 검색
     */
    @Transactional(readOnly = true)
    public List<PlaceDto> getNearbyPlaces(BigDecimal latitude, BigDecimal longitude, Double radius, Long categoryId) {
        List<Place> places = placeRepository.findNearbyPlaces(latitude, longitude, radius != null ? radius : 5.0);
        
        // 카테고리 필터링 (categoryId가 제공된 경우)
        if (categoryId != null) {
            places = places.stream()
                    .filter(place -> place.getCategory() != null && place.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }
        
        return places.stream()
                .map(place -> new PlaceDto(place, true)) // photos 포함
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 인기 장소 조회
     */
    @Transactional(readOnly = true)
    public List<PlaceDto> getPopularPlacesByCategory(Long categoryId) {
        Pageable pageable = PageRequest.of(0, 10);
        return placeRepository.findTop10ByCategoryIdAndIsActiveTrueOrderByViewCountDescAverageRatingDesc(categoryId, pageable)
                .stream()
                .map(PlaceDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 생성한 장소 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<PlaceDto> getPlacesByUser(Long userId, Pageable pageable) {
        return placeRepository.findByCreatedByOrderByCreatedAtDesc(userId, pageable)
                .map(PlaceDto::new);
    }

    /**
     * 평점 범위로 장소 검색
     */
    @Transactional(readOnly = true)
    public Page<PlaceDto> getPlacesByRatingRange(BigDecimal minRating, BigDecimal maxRating, Pageable pageable) {
        return placeRepository.findByAverageRatingBetweenAndIsActiveTrueOrderByAverageRatingDesc(
                minRating, maxRating, pageable)
                .map(PlaceDto::new);
    }

    /**
     * 장소 통계 정보 조회
     */
    @Transactional(readOnly = true)
    public PlaceDto getPlaceStatistics(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("장소를 찾을 수 없습니다: " + id));

        return new PlaceDto(place, true);
    }

    /**
     * 카테고리별 장소 개수 조회
     */
    @Transactional(readOnly = true)
    public long getPlaceCountByCategory(Long categoryId) {
        return placeRepository.countByCategoryIdAndIsActiveTrue(categoryId);
    }

    /**
     * 전체 활성화된 장소 개수 조회
     */
    @Transactional(readOnly = true)
    public long getTotalActivePlaceCount() {
        return placeRepository.countByIsActiveTrue();
    }

    /**
     * 장소 필드 업데이트 헬퍼 메서드
     */
    private void updatePlaceFields(Place existingPlace, PlaceDto placeDto) {
        if (placeDto.getName() != null) {
            existingPlace.setName(placeDto.getName());
        }
        if (placeDto.getDescription() != null) {
            existingPlace.setDescription(placeDto.getDescription());
        }
        if (placeDto.getAddress() != null) {
            existingPlace.setAddress(placeDto.getAddress());
        }
        if (placeDto.getDetailedAddress() != null) {
            existingPlace.setDetailedAddress(placeDto.getDetailedAddress());
        }
        if (placeDto.getLatitude() != null) {
            existingPlace.setLatitude(placeDto.getLatitude());
        }
        if (placeDto.getLongitude() != null) {
            existingPlace.setLongitude(placeDto.getLongitude());
        }
        if (placeDto.getPhoneNumber() != null) {
            existingPlace.setPhoneNumber(placeDto.getPhoneNumber());
        }
        if (placeDto.getWebsite() != null) {
            existingPlace.setWebsite(placeDto.getWebsite());
        }
        if (placeDto.getOpeningTime() != null) {
            existingPlace.setOpeningTime(placeDto.getOpeningTime());
        }
        if (placeDto.getClosingTime() != null) {
            existingPlace.setClosingTime(placeDto.getClosingTime());
        }
        if (placeDto.getIsOpen24h() != null) {
            existingPlace.setIsOpen24h(placeDto.getIsOpen24h());
        }
        if (placeDto.getClosedDays() != null) {
            existingPlace.setClosedDays(placeDto.getClosedDays());
        }
    }
}
