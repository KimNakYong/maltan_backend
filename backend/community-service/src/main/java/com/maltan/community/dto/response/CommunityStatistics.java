package com.maltan.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityStatistics {
    private Long totalPosts;
    private Long totalComments;
    private Long totalRecruitmentPosts;
    private Long activeRecruitmentPosts;
    private Long totalVotes;
    private Long totalParticipants;
}

