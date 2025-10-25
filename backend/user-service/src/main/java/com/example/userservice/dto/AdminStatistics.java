package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatistics {
    private Long totalUsers;
    private Long totalPosts;
    private Long totalComments;
    
    // 추가 통계
    private Long adminUsers;
    private Long activeUsers;  // isEnabled = true
}

