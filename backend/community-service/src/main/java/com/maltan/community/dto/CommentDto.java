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
    private String userNickname;
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

