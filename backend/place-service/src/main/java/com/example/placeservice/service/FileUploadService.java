package com.example.placeservice.service;

import com.example.placeservice.dto.PhotoDto;
import com.example.placeservice.entity.Photo;
import com.example.placeservice.entity.Place;
import com.example.placeservice.entity.Review;
import com.example.placeservice.repository.PhotoRepository;
import com.example.placeservice.repository.PlaceRepository;
import com.example.placeservice.repository.ReviewRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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

    @Value("${app.upload.max-photos-per-place:10}") // 장소당 최대 사진 개수
    private int maxPhotosPerPlace;

    @Value("${app.upload.image.max-width:1920}") // 이미지 최대 너비
    private int maxImageWidth;

    @Value("${app.upload.image.max-height:1080}") // 이미지 최대 높이
    private int maxImageHeight;

    @Value("${app.upload.image.quality:0.8}") // 이미지 품질 (0.0 ~ 1.0)
    private double imageQuality;

    @Value("${app.upload.thumbnail.size:300}") // 썸네일 크기
    private int thumbnailSize;

    @Value("${app.upload.enable-compression:true}") // 압축 활성화 여부
    private boolean enableCompression;

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
        
        Path targetPath = Paths.get(uploadDir, filePath);
        long finalFileSize;
        String finalContentType = file.getContentType();
        
        try {
            if (enableCompression && isImageFile(file)) {
                // 이미지 압축 및 리사이징
                BufferedImage originalImage = ImageIO.read(file.getInputStream());
                
                if (originalImage != null) {
                    // 압축된 이미지 저장
                    Thumbnails.of(originalImage)
                        .size(maxImageWidth, maxImageHeight)
                        .outputQuality(imageQuality)
                        .outputFormat("jpg")
                        .toFile(targetPath.toFile());
                    
                    // 실제 저장된 파일 크기
                    finalFileSize = Files.size(targetPath);
                    finalContentType = "image/jpeg";
                    
                    // 썸네일 생성
                    try {
                        createThumbnail(originalImage, filePath);
                    } catch (Exception e) {
                        // 썸네일 생성 실패해도 계속 진행
                        System.err.println("썸네일 생성 실패: " + e.getMessage());
                    }
                } else {
                    // 이미지 읽기 실패 시 원본 저장
                    byte[] fileBytes = file.getBytes();
                    Files.write(targetPath, fileBytes);
                    finalFileSize = file.getSize();
                }
            } else {
                // 압축 비활성화 또는 이미지가 아닌 경우 원본 저장
                byte[] fileBytes = file.getBytes();
                Files.write(targetPath, fileBytes);
                finalFileSize = file.getSize();
            }
        } catch (Exception e) {
            // 압축 실패 시 원본 저장
            System.err.println("이미지 압축 실패, 원본 저장: " + e.getMessage());
            byte[] fileBytes = file.getBytes();
            Files.write(targetPath, fileBytes);
            finalFileSize = file.getSize();
            finalContentType = file.getContentType();
        }
        
        // Photo 엔티티 생성 및 저장
        Photo photo = new Photo(
            file.getOriginalFilename(),
            storedName,
            filePath,
            finalFileSize,
            finalContentType,
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
     * 장소 사진 업로드 (기존 사진 모두 삭제 후 최신 1개만 유지)
     */
    public List<PhotoDto> uploadPlacePhotos(MultipartFile[] files, Long placeId, Long uploadedBy) throws IOException {
        // 장소 조회
        Place place = placeRepository.findById(placeId).orElseThrow(() -> 
            new RuntimeException("장소를 찾을 수 없습니다: " + placeId));
        
        // 기존 사진 모두 삭제
        List<Photo> existingPhotos = photoRepository.findByPlaceIdOrderBySortOrderAscCreatedAtDesc(placeId);
        for (Photo existingPhoto : existingPhotos) {
            // 물리적 파일 삭제
            try {
                Path filePath = Paths.get(uploadDir, existingPhoto.getFilePath());
                Files.deleteIfExists(filePath);
                
                // 썸네일도 삭제
                String thumbPath = existingPhoto.getFilePath().replace(".jpg", "_thumb.jpg")
                        .replace(".png", "_thumb.jpg")
                        .replace(".jpeg", "_thumb.jpg");
                Path thumbFilePath = Paths.get(uploadDir, thumbPath);
                Files.deleteIfExists(thumbFilePath);
            } catch (Exception e) {
                System.err.println("기존 파일 삭제 실패: " + e.getMessage());
            }
        }
        // DB에서 삭제
        photoRepository.deleteAll(existingPhotos);
        
        List<PhotoDto> uploadedPhotos = new ArrayList<>();
        
        // 첫 번째 파일만 처리 (최신 1개만 유지)
        MultipartFile file = files.length > 0 ? files[0] : null;
        if (file != null && !file.isEmpty()) {
            
            validateFile(file);
            
            String storedName = generateStoredName(file.getOriginalFilename());
            String filePath = createFilePath(storedName);
            
            // 디렉토리 생성
            createDirectories(filePath);
            
            Path targetPath = Paths.get(uploadDir, filePath);
            long finalFileSize;
            String finalContentType = file.getContentType();
            String finalFilePath = filePath; // 최종 저장 경로
            
            try {
                if (enableCompression && isImageFile(file)) {
                    // 이미지 압축 및 리사이징
                    BufferedImage originalImage = ImageIO.read(file.getInputStream());
                    
                    if (originalImage != null) {
                        // JPG 확장자로 변경
                        String jpgFilePath = filePath.replaceAll("\\.(png|jpeg|gif|webp)$", ".jpg");
                        Path jpgTargetPath = Paths.get(uploadDir, jpgFilePath);
                        
                        // 압축된 이미지 저장
                        Thumbnails.of(originalImage)
                            .size(maxImageWidth, maxImageHeight)
                            .outputQuality(imageQuality)
                            .outputFormat("jpg")
                            .toFile(jpgTargetPath.toFile());
                        
                        finalFileSize = Files.size(jpgTargetPath);
                        finalContentType = "image/jpeg";
                        finalFilePath = jpgFilePath;
                        
                        // 썸네일 생성
                        try {
                            createThumbnail(originalImage, jpgFilePath);
                        } catch (Exception e) {
                            System.err.println("썸네일 생성 실패: " + e.getMessage());
                        }
                    } else {
                        byte[] fileBytes = file.getBytes();
                        Files.write(targetPath, fileBytes);
                        finalFileSize = file.getSize();
                    }
                } else {
                    byte[] fileBytes = file.getBytes();
                    Files.write(targetPath, fileBytes);
                    finalFileSize = file.getSize();
                }
            } catch (Exception e) {
                System.err.println("이미지 압축 실패, 원본 저장: " + e.getMessage());
                byte[] fileBytes = file.getBytes();
                Files.write(targetPath, fileBytes);
                finalFileSize = file.getSize();
                finalContentType = file.getContentType();
            }
            
            // Photo 엔티티 생성 (place 설정 포함)
            Photo photo = new Photo(
                file.getOriginalFilename(),
                storedName,
                finalFilePath, // 압축 시 .jpg 확장자로 변경된 경로 사용
                finalFileSize,
                finalContentType,
                uploadedBy
            );
            
            // 장소 설정
            photo.setPlace(place);
            
            // 메인 사진으로 설정
            photo.setIsMain(true);
            
            // 정렬 순서 1로 설정 (첫 번째이자 유일한 사진)
            photo.setSortOrder(1);
            
            // DB 저장
            Photo savedPhoto = photoRepository.save(photo);
            uploadedPhotos.add(new PhotoDto(savedPhoto));
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

    /**
     * 이미지 파일인지 확인
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * 썸네일 생성
     */
    private void createThumbnail(BufferedImage originalImage, String originalPath) {
        try {
            // 썸네일 경로 생성 (원본 파일명에 _thumb 추가)
            String extension = FilenameUtils.getExtension(originalPath);
            String pathWithoutExtension = originalPath.substring(0, originalPath.lastIndexOf('.'));
            String thumbPath = pathWithoutExtension + "_thumb." + extension;
            
            Path thumbTargetPath = Paths.get(uploadDir, thumbPath);
            
            // 썸네일 생성 및 저장
            Thumbnails.of(originalImage)
                .size(thumbnailSize, thumbnailSize)
                .outputQuality(imageQuality)
                .outputFormat("jpg")
                .toFile(thumbTargetPath.toFile());
            
        } catch (IOException e) {
            // 썸네일 생성 실패 시 로그만 남기고 계속 진행
            System.err.println("썸네일 생성 실패: " + e.getMessage());
        }
    }
}
