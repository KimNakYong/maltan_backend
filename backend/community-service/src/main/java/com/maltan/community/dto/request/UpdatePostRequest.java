package com.maltan.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class UpdatePostRequest {
    
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    
    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;
    
    // 모집 정보 (모집 게시글인 경우)
    private LocalDateTime recruitmentDeadline;
    private LocalDateTime eventDate;
    
    @Size(max = 200, message = "활동 장소는 200자를 초과할 수 없습니다.")
    private String eventLocation;
    
    // 이미지 URL 목록
    @Size(max = 5, message = "이미지는 최대 5개까지 업로드 가능합니다.")
    private List<String> images;
}

