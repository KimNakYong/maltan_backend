package com.maltan.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
    
    private Long parentCommentId;  // 대댓글인 경우
    
    private Long userId;  // 임시: 인증 시스템 구현 전까지 요청 본문에서 받음
}

