package com.example.placeservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 디버깅용 컨트롤러
 * 파일 시스템 상태 확인
 */
@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DebugController {

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    /**
     * 업로드 디렉토리 정보 확인
     */
    @GetMapping("/upload-info")
    public ResponseEntity<Map<String, Object>> getUploadInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            File uploadDirFile = new File(uploadDir);
            
            info.put("uploadDir", uploadDir);
            info.put("exists", uploadDirFile.exists());
            info.put("isDirectory", uploadDirFile.isDirectory());
            info.put("canRead", uploadDirFile.canRead());
            info.put("canWrite", uploadDirFile.canWrite());
            info.put("absolutePath", uploadDirFile.getAbsolutePath());
            
            if (uploadDirFile.exists() && uploadDirFile.isDirectory()) {
                // 최근 파일 목록 (최대 20개)
                try (Stream<Path> paths = Files.walk(Paths.get(uploadDir))) {
                    List<String> recentFiles = paths
                            .filter(Files::isRegularFile)
                            .sorted((p1, p2) -> {
                                try {
                                    return Files.getLastModifiedTime(p2)
                                            .compareTo(Files.getLastModifiedTime(p1));
                                } catch (Exception e) {
                                    return 0;
                                }
                            })
                            .limit(20)
                            .map(p -> p.toString().replace(uploadDir, ""))
                            .collect(Collectors.toList());
                    
                    info.put("recentFiles", recentFiles);
                    info.put("totalFiles", recentFiles.size());
                } catch (Exception e) {
                    info.put("filesError", e.getMessage());
                }
            }
            
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            info.put("error", e.getMessage());
            return ResponseEntity.ok(info);
        }
    }

    /**
     * 특정 파일 확인
     */
    @GetMapping("/check-file")
    public ResponseEntity<Map<String, Object>> checkFile(
            @RequestParam String filePath) {
        Map<String, Object> info = new HashMap<>();
        
        try {
            Path fullPath = Paths.get(uploadDir).resolve(filePath).normalize();
            File file = fullPath.toFile();
            
            info.put("uploadDir", uploadDir);
            info.put("requestedPath", filePath);
            info.put("fullPath", fullPath.toString());
            info.put("exists", file.exists());
            info.put("isFile", file.isFile());
            info.put("canRead", file.canRead());
            
            if (file.exists()) {
                info.put("size", file.length());
                info.put("lastModified", file.lastModified());
            }
            
            // 부모 디렉토리 확인
            File parent = file.getParentFile();
            if (parent != null) {
                info.put("parentExists", parent.exists());
                info.put("parentIsDirectory", parent.isDirectory());
                
                if (parent.exists() && parent.isDirectory()) {
                    String[] siblings = parent.list();
                    info.put("siblingsCount", siblings != null ? siblings.length : 0);
                    if (siblings != null && siblings.length > 0) {
                        List<String> siblingList = new ArrayList<>();
                        for (int i = 0; i < Math.min(10, siblings.length); i++) {
                            siblingList.add(siblings[i]);
                        }
                        info.put("someSiblings", siblingList);
                    }
                }
            }
            
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            info.put("error", e.getMessage());
            info.put("stackTrace", e.getStackTrace()[0].toString());
            return ResponseEntity.ok(info);
        }
    }
}

