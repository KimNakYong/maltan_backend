package com.example.placeservice.controller;

import com.example.placeservice.dto.ApiResponse;
import com.example.placeservice.dto.PhotoDto;
import com.example.placeservice.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 파일 업로드 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 단일 파일 업로드
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<PhotoDto>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long uploadedBy) {
        try {
            PhotoDto uploadedPhoto = fileUploadService.uploadFile(file, uploadedBy);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("파일 업로드 성공", uploadedPhoto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("파일 업로드 실패: " + e.getMessage()));
        }
    }

    /**
     * 다중 파일 업로드
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse<List<PhotoDto>>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam Long uploadedBy) {
        try {
            List<PhotoDto> uploadedPhotos = fileUploadService.uploadFiles(files, uploadedBy);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("파일 업로드 성공", uploadedPhotos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("파일 업로드 실패: " + e.getMessage()));
        }
    }

    /**
     * 장소 사진 업로드
     */
    @PostMapping("/upload/place/{placeId}")
    public ResponseEntity<ApiResponse<List<PhotoDto>>> uploadPlacePhotos(
            @PathVariable Long placeId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam Long uploadedBy) {
        try {
            List<PhotoDto> uploadedPhotos = fileUploadService.uploadPlacePhotos(files, placeId, uploadedBy);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("장소 사진 업로드 성공", uploadedPhotos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("장소 사진 업로드 실패: " + e.getMessage()));
        }
    }

    /**
     * 리뷰 사진 업로드
     */
    @PostMapping("/upload/review/{reviewId}")
    public ResponseEntity<ApiResponse<List<PhotoDto>>> uploadReviewPhotos(
            @PathVariable Long reviewId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam Long uploadedBy) {
        try {
            List<PhotoDto> uploadedPhotos = fileUploadService.uploadReviewPhotos(files, reviewId, uploadedBy);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("리뷰 사진 업로드 성공", uploadedPhotos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 사진 업로드 실패: " + e.getMessage()));
        }
    }

    /**
     * 파일 삭제
     */
    @DeleteMapping("/{photoId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long photoId) {
        try {
            boolean deleted = fileUploadService.deleteFile(photoId);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("파일 삭제 성공"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("파일을 찾을 수 없습니다: " + photoId));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("파일 삭제 실패: " + e.getMessage()));
        }
    }

    /**
     * 메인 사진 설정
     */
    @PatchMapping("/{photoId}/set-main")
    public ResponseEntity<ApiResponse<Void>> setMainPhoto(
            @PathVariable Long photoId,
            @RequestParam Long placeId) {
        try {
            boolean success = fileUploadService.setMainPhoto(photoId, placeId);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("메인 사진 설정 성공"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("메인 사진 설정 실패"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("메인 사진 설정 실패: " + e.getMessage()));
        }
    }

    /**
     * 파일 업로드 설정 정보 조회
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUploadConfig() {
        try {
            Map<String, Object> config = Map.of(
                "uploadDir", fileUploadService.getUploadDir(),
                "maxFileSize", fileUploadService.getMaxFileSize(),
                "allowedTypes", fileUploadService.getAllowedTypes(),
                "maxFileSizeMB", fileUploadService.getMaxFileSize() / 1024 / 1024
            );
            return ResponseEntity.ok(ApiResponse.success("업로드 설정 조회 성공", config));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("업로드 설정 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkFileExists(@RequestParam String filePath) {
        try {
            boolean exists = fileUploadService.fileExists(filePath);
            return ResponseEntity.ok(ApiResponse.success("파일 존재 여부 확인 성공", exists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("파일 존재 여부 확인 실패: " + e.getMessage()));
        }
    }

    /**
     * 파일 크기 조회
     */
    @GetMapping("/size")
    public ResponseEntity<ApiResponse<Long>> getFileSize(@RequestParam String filePath) {
        try {
            long size = fileUploadService.getFileSize(filePath);
            return ResponseEntity.ok(ApiResponse.success("파일 크기 조회 성공", size));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("파일 크기 조회 실패: " + e.getMessage()));
        }
    }
}
