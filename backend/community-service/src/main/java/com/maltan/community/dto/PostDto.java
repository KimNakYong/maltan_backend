package com.maltan.community.dto;

import com.maltan.community.model.PostStatus;
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
public class PostDto {
    private Long id;
    private Long userId;
    private String userName;  // User Service에서 조회 (회원가입 시 입력한 이름)
    private String userProfileImage;  // User Service에서 조회
    
    private String title;
    private String content;
    private String category;
    
    private String regionSi;
    private String regionGu;
    private String regionDong;
    
    private Boolean isRecruitment;
    private Integer recruitmentMax;
    private Integer recruitmentCurrent;
    private LocalDateTime recruitmentDeadline;
    private LocalDateTime eventDate;
    private String eventLocation;
    
    private Integer viewCount;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer commentCount;
    
    private PostStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<String> images;
    
    // 사용자 관련 상태 (로그인한 사용자)
    private Boolean isLiked;
    private Boolean isDisliked;
    private Boolean isJoined;  // 모집 참여 여부
}

