package com.example.placeservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * 정적 리소스 핸들러 설정
     * 업로드된 파일에 대한 URL 매핑
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드 디렉토리 경로 정규화
        String normalizedUploadDir = uploadDir;
        if (!normalizedUploadDir.endsWith("/")) {
            normalizedUploadDir += "/";
        }
        
        // 절대 경로로 변환
        String resourceLocation;
        if (normalizedUploadDir.startsWith("/")) {
            // 이미 절대 경로인 경우
            resourceLocation = "file:" + normalizedUploadDir;
        } else {
            // 상대 경로인 경우 현재 디렉토리 기준으로 변환
            resourceLocation = "file:" + System.getProperty("user.dir") + "/" + normalizedUploadDir;
        }
        
        // 업로드된 파일 접근을 위한 리소스 핸들러
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600); // 1시간 캐시

        // 기본 정적 리소스 핸들러
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }
}
