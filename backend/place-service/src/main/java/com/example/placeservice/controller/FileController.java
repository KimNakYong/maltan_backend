package com.example.placeservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 파일 서빙 컨트롤러
 * 업로드된 이미지 파일을 웹에서 접근 가능하도록 제공
 */
@RestController
@RequestMapping("/uploads")
@CrossOrigin(origins = "*")
public class FileController {

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    /**
     * 업로드된 파일 서빙
     * 
     * @param filePath 파일 경로 (예: 2025/10/26/filename.jpg)
     * @return 파일 리소스
     */
    @GetMapping("/**")
    public ResponseEntity<Resource> serveFile(@RequestParam(required = false) String filePath) {
        try {
            // Request URI에서 /uploads 이후의 경로 추출
            String requestPath = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .build()
                    .getPath();
            
            // /uploads/ 제거
            String relativePath = requestPath.replaceFirst("^/uploads/", "");
            
            // 실제 파일 경로 생성
            Path file = Paths.get(uploadDir).resolve(relativePath).normalize();
            File fileResource = file.toFile();
            
            // 파일 존재 확인
            if (!fileResource.exists() || !fileResource.isFile()) {
                return ResponseEntity.notFound().build();
            }
            
            // 파일이 uploadDir 하위에 있는지 보안 검사
            if (!file.startsWith(Paths.get(uploadDir).normalize())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Content-Type 감지
            String contentType = Files.probeContentType(file);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // 파일 리소스 생성
            Resource resource = new FileSystemResource(fileResource);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileResource.getName() + "\"")
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 파일 존재 여부 확인 (디버깅용)
     */
    @GetMapping("/check/**")
    public ResponseEntity<String> checkFile() {
        try {
            String requestPath = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .build()
                    .getPath();
            
            String relativePath = requestPath.replaceFirst("^/uploads/check/", "");
            Path file = Paths.get(uploadDir).resolve(relativePath).normalize();
            
            boolean exists = Files.exists(file);
            boolean isFile = Files.isRegularFile(file);
            boolean readable = Files.isReadable(file);
            
            String info = String.format(
                "Upload Dir: %s\n" +
                "Relative Path: %s\n" +
                "Full Path: %s\n" +
                "Exists: %s\n" +
                "Is File: %s\n" +
                "Readable: %s",
                uploadDir, relativePath, file.toString(), exists, isFile, readable
            );
            
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }
}

