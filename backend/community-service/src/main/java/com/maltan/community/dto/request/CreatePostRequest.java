package com.maltan.community.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    
    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;
    
    @NotBlank(message = "지역(시/도)은 필수입니다.")
    private String regionSi;
    
    private String regionGu;
    private String regionDong;
    
    // 모집 정보 (선택)
    private Boolean isRecruitment;
    
    @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다.")
    @Max(value = 100, message = "모집 인원은 최대 100명을 초과할 수 없습니다.")
    private Integer recruitmentMax;
    
    private LocalDateTime recruitmentDeadline;
    private LocalDateTime eventDate;
    
    @Size(max = 200, message = "활동 장소는 200자를 초과할 수 없습니다.")
    private String eventLocation;
    
    // 이미지 URL 목록
    @Size(max = 5, message = "이미지는 최대 5개까지 업로드 가능합니다.")
    private List<String> images;
}

