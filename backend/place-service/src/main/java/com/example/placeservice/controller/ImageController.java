package com.example.placeservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 이미지 파일 서빙 컨트롤러
 */
@RestController
@CrossOrigin(origins = "*")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    @GetMapping("/uploads/**")
    public ResponseEntity<Resource> serveImage(HttpServletRequest request) {
        try {
            String requestURI = request.getRequestURI();
            String imagePath = requestURI.replace("/uploads/", "");
            
            log.info("=== Image Request ===");
            log.info("Request URI: {}", requestURI);
            log.info("Image Path: {}", imagePath);
            log.info("Upload Dir: {}", uploadDir);
            
            // 파일 경로
            Path filePath = Paths.get(uploadDir, imagePath);
            File file = filePath.toFile();
            
            log.info("Full File Path: {}", filePath.toAbsolutePath());
            log.info("File Exists: {}", file.exists());
            log.info("Is File: {}", file.isFile());
            
            if (file.exists()) {
                log.info("File Size: {} bytes", file.length());
                log.info("Can Read: {}", file.canRead());
            }
            
            if (!file.exists() || !file.isFile()) {
                log.error("File not found or not a file: {}", filePath.toAbsolutePath());
                
                // 디렉토리 내용 확인
                File parentDir = file.getParentFile();
                if (parentDir != null && parentDir.exists()) {
                    log.info("Parent directory exists: {}", parentDir.getAbsolutePath());
                    File[] files = parentDir.listFiles();
                    if (files != null && files.length > 0) {
                        log.info("Files in directory:");
                        for (File f : files) {
                            log.info("  - {}", f.getName());
                        }
                    } else {
                        log.info("Directory is empty");
                    }
                } else {
                    log.error("Parent directory does not exist: {}", parentDir != null ? parentDir.getAbsolutePath() : "null");
                }
                
                return ResponseEntity.notFound().build();
            }
            
            // MIME 타입 결정
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            log.info("Content Type: {}", contentType);
            
            Resource resource = new FileSystemResource(file);
            
            log.info("Successfully serving image: {}", imagePath);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error serving image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

