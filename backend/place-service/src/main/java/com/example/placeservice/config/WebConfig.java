package com.example.placeservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;

/**
 * Web MVC 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

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

    /**
     * 정적 리소스 핸들러 설정
     * 업로드된 파일에 대한 URL 매핑
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드 디렉토리의 절대 경로 사용
        File uploadDirFile = new File(uploadDir);
        String absolutePath = uploadDirFile.getAbsolutePath();
        
        // 끝에 슬래시 추가
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }
        
        String resourceLocation = "file:" + absolutePath;
        
        log.info("Registering resource handler: /uploads/** -> {}", resourceLocation);
        
        // 업로드된 파일 접근을 위한 리소스 핸들러
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(0); // 캐시 비활성화 (개발 중)
                
        log.info("Resource handler registered successfully");
    }
}
