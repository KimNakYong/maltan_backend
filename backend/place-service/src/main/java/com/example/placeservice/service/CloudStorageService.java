package com.example.placeservice.service;

import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Google Cloud Storage 파일 업로드/삭제 서비스
 */
@Service
public class CloudStorageService {

    @Value("${gcp.storage.bucket-name}")
    private String bucketName;

    @Value("${gcp.storage.project-id}")
    private String projectId;

    @Value("${gcp.storage.enabled:false}")
    private boolean storageEnabled;

    private Storage storage;

    /**
     * Storage 인스턴스 초기화 (Lazy Loading)
     */
    private Storage getStorage() {
        if (storage == null && storageEnabled) {
            storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .build()
                .getService();
        }
        return storage;
    }

    /**
     * 파일을 GCS에 업로드
     * 
     * @param file 업로드할 파일
     * @param folder 저장할 폴더 (예: "places", "reviews")
     * @return 업로드된 파일의 Public URL
     * @throws IOException
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (!storageEnabled) {
            throw new IllegalStateException("Google Cloud Storage가 비활성화되어 있습니다.");
        }

        // 고유한 파일명 생성
        String fileName = generateFileName(file.getOriginalFilename(), folder);

        // BlobInfo 생성
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(file.getContentType())
            .build();

        // 파일 업로드
        Blob blob = getStorage().create(blobInfo, file.getBytes());

        // Public URL 반환
        return getPublicUrl(fileName);
    }

    /**
     * GCS에서 파일 삭제
     * 
     * @param fileUrl 삭제할 파일의 Public URL
     * @return 삭제 성공 여부
     */
    public boolean deleteFile(String fileUrl) {
        if (!storageEnabled) {
            return false;
        }

        try {
            // URL에서 파일명 추출
            String fileName = extractFileNameFromUrl(fileUrl);
            
            if (fileName == null) {
                return false;
            }

            BlobId blobId = BlobId.of(bucketName, fileName);
            return getStorage().delete(blobId);
        } catch (Exception e) {
            System.err.println("파일 삭제 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 파일의 Signed URL 생성 (임시 접근 URL)
     * 
     * @param fileName 파일명
     * @param duration URL 유효 시간
     * @param unit 시간 단위
     * @return Signed URL
     */
    public String generateSignedUrl(String fileName, long duration, TimeUnit unit) {
        if (!storageEnabled) {
            return null;
        }

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        return getStorage().signUrl(
            blobInfo,
            duration,
            unit,
            Storage.SignUrlOption.withV4Signature()
        ).toString();
    }

    /**
     * 파일명 생성 (UUID + 원본 확장자)
     */
    private String generateFileName(String originalFilename, String folder) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uuid = UUID.randomUUID().toString();
        return folder + "/" + uuid + extension;
    }

    /**
     * Public URL 생성
     */
    private String getPublicUrl(String fileName) {
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    }

    /**
     * URL에서 파일명 추출
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(bucketName)) {
            return null;
        }
        
        String[] parts = fileUrl.split(bucketName + "/");
        if (parts.length > 1) {
            return parts[1];
        }
        return null;
    }

    /**
     * Storage 활성화 여부 확인
     */
    public boolean isEnabled() {
        return storageEnabled;
    }
}

