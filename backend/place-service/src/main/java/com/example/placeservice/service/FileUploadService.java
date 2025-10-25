package com.example.placeservice.service;

import com.example.placeservice.dto.PhotoDto;
import com.example.placeservice.entity.Photo;
import com.example.placeservice.entity.Place;
import com.example.placeservice.entity.Review;
import com.example.placeservice.repository.PhotoRepository;
import com.example.placeservice.repository.PlaceRepository;
import com.example.placeservice.repository.ReviewRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 파일 업로드 서비스
 */
@Service
public class FileUploadService {

    @Autowired
    private PhotoRepository photoRepository;
    
    @Autowired
    private PlaceRepository placeRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:10485760}") // 10MB
    private long maxFileSize;

    @Value("${app.upload.allowed-types:image/jpeg,image/jpg,image/png,image/gif,image/webp}")
    private String allowedTypes;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    /**
     * 단일 파일 업로드
     */
    public PhotoDto uploadFile(MultipartFile file, Long uploadedBy) throws IOException {
        validateFile(file);
        
        String storedName = generateStoredName(file.getOriginalFilename());
        String filePath = createFilePath(storedName);
        
        // 디렉토리 생성
        createDirectories(filePath);
        
        // 파일 저장
        Path targetPath = Paths.get(uploadDir, filePath);
        Files.copy(file.getInputStream(), targetPath);
        
        // Photo 엔티티 생성 및 저장
        Photo photo = new Photo(
            file.getOriginalFilename(),
            storedName,
            filePath,
            file.getSize(),
            file.getContentType(),
            uploadedBy
        );
        
        Photo savedPhoto = photoRepository.save(photo);
        return new PhotoDto(savedPhoto);
    }

    /**
     * 다중 파일 업로드
     */
    public List<PhotoDto> uploadFiles(MultipartFile[] files, Long uploadedBy) throws IOException {
        List<PhotoDto> uploadedPhotos = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                PhotoDto photoDto = uploadFile(file, uploadedBy);
                uploadedPhotos.add(photoDto);
            }
        }
        
        return uploadedPhotos;
    }

    /**
     * 장소 사진 업로드
     */
    public List<PhotoDto> uploadPlacePhotos(MultipartFile[] files, Long placeId, Long uploadedBy) throws IOException {
        List<PhotoDto> uploadedPhotos = uploadFiles(files, uploadedBy);
        
        // 장소 ID 설정 및 정렬 순서 설정
        for (int i = 0; i < uploadedPhotos.size(); i++) {
            PhotoDto photoDto = uploadedPhotos.get(i);
            Photo photo = photoRepository.findById(photoDto.getId()).orElseThrow();
            
            // 첫 번째 사진을 메인 사진으로 설정 (기존 메인 사진이 없는 경우)
            if (i == 0 && !photoRepository.findByPlaceIdAndIsMainTrue(placeId).isPresent()) {
                photo.setIsMain(true);
            }
            
            Place place = placeRepository.findById(placeId).orElseThrow(() -> 
                new RuntimeException("장소를 찾을 수 없습니다: " + placeId));
            photo.setPlace(place);
            photo.setSortOrder(photoRepository.findNextSortOrderByPlaceId(placeId));
            
            photoRepository.save(photo);
            uploadedPhotos.set(i, new PhotoDto(photo));
        }
        
        return uploadedPhotos;
    }

    /**
     * 리뷰 사진 업로드
     */
    public List<PhotoDto> uploadReviewPhotos(MultipartFile[] files, Long reviewId, Long uploadedBy) throws IOException {
        List<PhotoDto> uploadedPhotos = uploadFiles(files, uploadedBy);
        
        // 리뷰 ID 설정 및 정렬 순서 설정
        for (int i = 0; i < uploadedPhotos.size(); i++) {
            PhotoDto photoDto = uploadedPhotos.get(i);
            Photo photo = photoRepository.findById(photoDto.getId()).orElseThrow();
            
            Review review = reviewRepository.findById(reviewId).orElseThrow(() -> 
                new RuntimeException("리뷰를 찾을 수 없습니다: " + reviewId));
            photo.setReview(review);
            photo.setSortOrder(photoRepository.findNextSortOrderByReviewId(reviewId));
            
            photoRepository.save(photo);
            uploadedPhotos.set(i, new PhotoDto(photo));
        }
        
        return uploadedPhotos;
    }

    /**
     * 파일 삭제
     */
    public boolean deleteFile(Long photoId) {
        try {
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo == null) {
                return false;
            }
            
            // 물리적 파일 삭제
            Path filePath = Paths.get(uploadDir, photo.getFilePath());
            Files.deleteIfExists(filePath);
            
            // 데이터베이스에서 삭제
            photoRepository.delete(photo);
            
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 메인 사진 설정
     */
    public boolean setMainPhoto(Long photoId, Long placeId) {
        try {
            // 기존 메인 사진 해제
            photoRepository.findByPlaceIdAndIsMainTrue(placeId)
                .ifPresent(existingMain -> {
                    existingMain.setIsMain(false);
                    photoRepository.save(existingMain);
                });
            
            // 새 메인 사진 설정
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo != null && photo.getPlace() != null && photo.getPlace().getId().equals(placeId)) {
                photo.setIsMain(true);
                photoRepository.save(photo);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 " + (maxFileSize / 1024 / 1024) + "MB까지 업로드 가능합니다.");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(allowedTypes.split(",")).contains(contentType)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. 지원 형식: " + allowedTypes);
        }
        
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("지원하지 않는 파일 확장자입니다. 지원 확장자: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    /**
     * 저장될 파일명 생성
     */
    private String generateStoredName(String originalFilename) {
        String extension = FilenameUtils.getExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + extension;
    }

    /**
     * 파일 경로 생성 (년/월/일 구조)
     */
    private String createFilePath(String storedName) {
        LocalDateTime now = LocalDateTime.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return datePath + "/" + storedName;
    }

    /**
     * 디렉토리 생성
     */
    private void createDirectories(String filePath) throws IOException {
        Path directoryPath = Paths.get(uploadDir, filePath).getParent();
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
    }

    /**
     * 파일 URL 생성
     */
    public String getFileUrl(String filePath, String baseUrl) {
        if (filePath == null || baseUrl == null) {
            return null;
        }
        return baseUrl + "/uploads/" + filePath;
    }

    /**
     * 업로드 디렉토리 정보 조회
     */
    public String getUploadDir() {
        return uploadDir;
    }

    /**
     * 허용된 파일 타입 조회
     */
    public String getAllowedTypes() {
        return allowedTypes;
    }

    /**
     * 최대 파일 크기 조회
     */
    public long getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean fileExists(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath);
            return Files.exists(path);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 파일 크기 조회
     */
    public long getFileSize(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath);
            return Files.size(path);
        } catch (IOException e) {
            return 0;
        }
    }
}
