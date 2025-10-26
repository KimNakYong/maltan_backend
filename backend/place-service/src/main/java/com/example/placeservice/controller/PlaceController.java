package com.example.placeservice.controller;

import com.example.placeservice.dto.ApiResponse;
import com.example.placeservice.dto.PhotoDto;
import com.example.placeservice.dto.PlaceDto;
import com.example.placeservice.service.FileUploadService;
import com.example.placeservice.service.PlaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 장소 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = "*")
public class PlaceController {

    @Autowired
    private PlaceService placeService;

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 모든 활성화된 장소 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PlaceDto>>> getAllPlaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<PlaceDto> places = placeService.getAllActivePlaces(pageable);
            return ResponseEntity.ok(ApiResponse.success("장소 목록 조회 성공", places));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소 목록 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소 ID로 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaceDto>> getPlaceById(@PathVariable Long id) {
        try {
            return placeService.getPlaceById(id)
                    .map(place -> ResponseEntity.ok(ApiResponse.success("장소 조회 성공", place)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("장소를 찾을 수 없습니다: " + id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소 기본 정보 조회 (상세 정보 제외)
     */
    @GetMapping("/{id}/basic")
    public ResponseEntity<ApiResponse<PlaceDto>> getPlaceBasicInfo(@PathVariable Long id) {
        try {
            return placeService.getPlaceBasicInfo(id)
                    .map(place -> ResponseEntity.ok(ApiResponse.success("장소 기본 정보 조회 성공", place)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("장소를 찾을 수 없습니다: " + id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소 기본 정보 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 카테고리별 장소 조회 (페이징)
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<PlaceDto>>> getPlacesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PlaceDto> places = placeService.getPlacesByCategory(categoryId, pageable);
            return ResponseEntity.ok(ApiResponse.success("카테고리별 장소 조회 성공", places));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리별 장소 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소 검색 (이름 또는 주소)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PlaceDto>>> searchPlaces(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PlaceDto> places;
            
            if (categoryId != null) {
                places = placeService.searchPlacesByCategoryAndKeyword(categoryId, keyword, pageable);
            } else {
                places = placeService.searchPlaces(keyword, pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success("장소 검색 성공", places));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소 검색 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소 생성 (JSON만)
     */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ApiResponse<PlaceDto>> createPlace(@Valid @RequestBody PlaceDto placeDto) {
        try {
            PlaceDto createdPlace = placeService.createPlace(placeDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("장소 생성 성공", createdPlace));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소 생성 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소 생성 (이미지 포함)
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<PlaceDto>> createPlaceWithImage(
            @RequestParam("name") String name,
            @RequestParam("address") String address,
            @RequestParam("latitude") BigDecimal latitude,
            @RequestParam("longitude") BigDecimal longitude,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "detailedAddress", required = false) String detailedAddress,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "website", required = false) String website,
            @RequestParam(value = "openingTime", required = false) String openingTime,
            @RequestParam(value = "closingTime", required = false) String closingTime,
            @RequestParam(value = "closedDays", required = false) String closedDays,
            @RequestParam(value = "isOpen24h", required = false, defaultValue = "false") Boolean isOpen24h,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // PlaceDto 생성
            PlaceDto placeDto = new PlaceDto();
            placeDto.setName(name);
            placeDto.setAddress(address);
            placeDto.setLatitude(latitude);
            placeDto.setLongitude(longitude);
            placeDto.setCategoryId(categoryId);
            placeDto.setDescription(description);
            placeDto.setDetailedAddress(detailedAddress);
            placeDto.setPhoneNumber(phoneNumber);
            placeDto.setWebsite(website);
            
            // LocalTime 변환
            if (openingTime != null && !openingTime.isEmpty()) {
                try {
                    placeDto.setOpeningTime(LocalTime.parse(openingTime));
                } catch (DateTimeParseException e) {
                    // 파싱 실패 시 무시
                }
            }
            if (closingTime != null && !closingTime.isEmpty()) {
                try {
                    placeDto.setClosingTime(LocalTime.parse(closingTime));
                } catch (DateTimeParseException e) {
                    // 파싱 실패 시 무시
                }
            }
            
            placeDto.setClosedDays(closedDays);
            placeDto.setIsOpen24h(isOpen24h);
            placeDto.setCreatedBy(1L); // 임시
            
            // 장소 생성
            PlaceDto createdPlace = placeService.createPlace(placeDto);
            
            // 이미지가 있으면 업로드
            if (file != null && !file.isEmpty()) {
                MultipartFile[] files = new MultipartFile[] { file };
                fileUploadService.uploadPlacePhotos(files, createdPlace.getId(), 1L);
                
                // 업데이트된 장소 정보 다시 조회 (이미지 포함)
                createdPlace = placeService.getPlaceById(createdPlace.getId()).orElse(createdPlace);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("장소 생성 성공", createdPlace));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소 생성 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaceDto>> updatePlace(
            @PathVariable Long id, 
            @Valid @RequestBody PlaceDto placeDto) {
        try {
            PlaceDto updatedPlace = placeService.updatePlace(id, placeDto);
            return ResponseEntity.ok(ApiResponse.success("장소 수정 성공", updatedPlace));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소 수정 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소 삭제 (비활성화)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlace(@PathVariable Long id) {
        try {
            placeService.deletePlace(id);
            return ResponseEntity.ok(ApiResponse.success("장소 삭제 성공"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소 삭제 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소 활성화/비활성화 토글
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<PlaceDto>> togglePlaceStatus(@PathVariable Long id) {
        try {
            PlaceDto updatedPlace = placeService.togglePlaceStatus(id);
            return ResponseEntity.ok(ApiResponse.success("장소 상태 변경 성공", updatedPlace));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소 상태 변경 실패: " + e.getMessage()));
        }
    }

    /**
     * 인기 장소 조회 (조회수 기준)
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<Page<PlaceDto>>> getPopularPlaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PlaceDto> places = placeService.getPopularPlaces(pageable);
            return ResponseEntity.ok(ApiResponse.success("인기 장소 조회 성공", places));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("인기 장소 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 최신 장소 조회
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<Page<PlaceDto>>> getLatestPlaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PlaceDto> places = placeService.getLatestPlaces(pageable);
            return ResponseEntity.ok(ApiResponse.success("최신 장소 조회 성공", places));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("최신 장소 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 평점 높은 장소 조회
     */
    @GetMapping("/top-rated")
    public ResponseEntity<ApiResponse<Page<PlaceDto>>> getTopRatedPlaces(
            @RequestParam(defaultValue = "5") Integer minReviewCount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PlaceDto> places = placeService.getTopRatedPlaces(minReviewCount, pageable);
            return ResponseEntity.ok(ApiResponse.success("평점 높은 장소 조회 성공", places));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("평점 높은 장소 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 추천 장소 조회
     */
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<PlaceDto>>> getRecommendedPlaces(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<PlaceDto> places = placeService.getRecommendedPlaces(limit);
            return ResponseEntity.ok(ApiResponse.success("추천 장소 조회 성공", places));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("추천 장소 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 위치 기반 주변 장소 검색
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<PlaceDto>>> getNearbyPlaces(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(defaultValue = "5.0") Double radius,
            @RequestParam(required = false) Long categoryId) {
        try {
            List<PlaceDto> places = placeService.getNearbyPlaces(latitude, longitude, radius, categoryId);
            return ResponseEntity.ok(ApiResponse.success("주변 장소 조회 성공", places));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("주변 장소 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 카테고리별 인기 장소 조회
     */
    @GetMapping("/category/{categoryId}/popular")
    public ResponseEntity<ApiResponse<List<PlaceDto>>> getPopularPlacesByCategory(@PathVariable Long categoryId) {
        try {
            List<PlaceDto> places = placeService.getPopularPlacesByCategory(categoryId);
            return ResponseEntity.ok(ApiResponse.success("카테고리별 인기 장소 조회 성공", places));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리별 인기 장소 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 사용자가 생성한 장소 목록 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<PlaceDto>>> getPlacesByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PlaceDto> places = placeService.getPlacesByUser(userId, pageable);
            return ResponseEntity.ok(ApiResponse.success("사용자별 장소 조회 성공", places));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자별 장소 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 평점 범위로 장소 검색
     */
    @GetMapping("/rating-range")
    public ResponseEntity<ApiResponse<Page<PlaceDto>>> getPlacesByRatingRange(
            @RequestParam BigDecimal minRating,
            @RequestParam BigDecimal maxRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PlaceDto> places = placeService.getPlacesByRatingRange(minRating, maxRating, pageable);
            return ResponseEntity.ok(ApiResponse.success("평점 범위별 장소 조회 성공", places));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("평점 범위별 장소 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소 통계 정보 조회
     */
    @GetMapping("/{id}/statistics")
    public ResponseEntity<ApiResponse<PlaceDto>> getPlaceStatistics(@PathVariable Long id) {
        try {
            PlaceDto placeStats = placeService.getPlaceStatistics(id);
            return ResponseEntity.ok(ApiResponse.success("장소 통계 조회 성공", placeStats));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소 통계 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 카테고리별 장소 개수 조회
     */
    @GetMapping("/category/{categoryId}/count")
    public ResponseEntity<ApiResponse<Long>> getPlaceCountByCategory(@PathVariable Long categoryId) {
        try {
            long count = placeService.getPlaceCountByCategory(categoryId);
            return ResponseEntity.ok(ApiResponse.success("카테고리별 장소 개수 조회 성공", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리별 장소 개수 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 전체 활성화된 장소 개수 조회
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getTotalPlaceCount() {
        try {
            long count = placeService.getTotalActivePlaceCount();
            return ResponseEntity.ok(ApiResponse.success("전체 장소 개수 조회 성공", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("전체 장소 개수 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소 사진 업로드
     */
    @PostMapping("/{placeId}/photos")
    public ResponseEntity<ApiResponse<PhotoDto>> uploadPlacePhoto(
            @PathVariable Long placeId,
            @RequestParam("file") MultipartFile file) {
        try {
            // uploadedBy는 현재 인증된 사용자 ID로 설정해야 하지만, 
            // 임시로 1L을 사용 (추후 Spring Security에서 가져오도록 수정)
            Long uploadedBy = 1L;
            
            // 단일 파일을 배열로 변환
            MultipartFile[] files = new MultipartFile[] { file };
            List<PhotoDto> uploadedPhotos = fileUploadService.uploadPlacePhotos(files, placeId, uploadedBy);
            
            if (!uploadedPhotos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("장소 사진 업로드 성공", uploadedPhotos.get(0)));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("사진 업로드에 실패했습니다"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소 사진 업로드 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소 사진 삭제
     */
    @DeleteMapping("/{placeId}/photos/{photoId}")
    public ResponseEntity<ApiResponse<Void>> deletePlacePhoto(
            @PathVariable Long placeId,
            @PathVariable Long photoId) {
        try {
            boolean deleted = fileUploadService.deleteFile(photoId);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("사진 삭제 성공"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("사진을 찾을 수 없습니다: " + photoId));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사진 삭제 실패: " + e.getMessage()));
        }
    }
}
