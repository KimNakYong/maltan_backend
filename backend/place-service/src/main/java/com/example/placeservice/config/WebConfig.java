package com.example.placeservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;

/**
 * Web MVC 설정
 */
@Configuration
public class WebConfig {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        File uploadDirFile = new File(uploadDir);
        log.info("=== Upload Directory Configuration ===");
        log.info("Upload Dir: {}", uploadDir);
        log.info("Absolute Path: {}", uploadDirFile.getAbsolutePath());
        log.info("Exists: {}", uploadDirFile.exists());
        log.info("Is Directory: {}", uploadDirFile.isDirectory());
        log.info("Can Read: {}", uploadDirFile.canRead());
        log.info("=====================================");
        
        // 디렉토리가 없으면 생성
        if (!uploadDirFile.exists()) {
            boolean created = uploadDirFile.mkdirs();
            log.info("Created upload directory: {}", created);
        }
    }
}
