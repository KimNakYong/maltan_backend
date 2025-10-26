package com.example.placeservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;

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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 절대 경로로 변환
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().toString().replace("\\", "/");
        
        // 끝에 슬래시가 없으면 추가
        if (!absolutePath.endsWith("/")) {
            absolutePath += "/";
        }
        
        String resourceLocation = "file:" + absolutePath;
        
        log.info("=== Resource Handler Registration ===");
        log.info("URL Pattern: /uploads/**");
        log.info("Resource Location: {}", resourceLocation);
        log.info("=====================================");
        
        // 우선순위를 높이기 위해 order 설정
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(0) // 개발 환경: 캐시 비활성화
                .resourceChain(false); // 리소스 체인 비활성화 (더 빠른 응답)
        
        // 기본 정적 리소스 핸들러는 나중에 처리되도록 설정
        registry.setOrder(1);
    }
}
