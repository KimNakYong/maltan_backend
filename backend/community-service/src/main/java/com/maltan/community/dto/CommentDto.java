package com.maltan.community.dto;

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
public class CommentDto {
    private Long id;
    private Long postId;
    private Long userId;
    private String userName;  // User Service에서 조회 (회원가입 시 입력한 이름)
    private String userNickname;  // 호환성을 위해 유지
    private String userProfileImage;
    
    private Long parentCommentId;
    private String content;
    
    private Integer likeCount;
    private Integer dislikeCount;
    
    private Boolean isLiked;
    private Boolean isDisliked;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<CommentDto> replies;
}

