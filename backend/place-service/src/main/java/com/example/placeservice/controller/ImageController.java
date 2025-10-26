package com.example.placeservice.controller;

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
 * 이미지 파일 서빙 컨트롤러 (가장 간단하고 확실한 방법)
 */
@RestController
@CrossOrigin(origins = "*")
public class ImageController {

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    @GetMapping("/uploads/**")
    public ResponseEntity<Resource> serveImage(HttpServletRequest request) {
        try {
            String requestURI = request.getRequestURI();
            String imagePath = requestURI.replace("/uploads/", "");
            
            // 파일 경로
            Path filePath = Paths.get(uploadDir, imagePath);
            File file = filePath.toFile();
            
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }
            
            // MIME 타입 결정
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

