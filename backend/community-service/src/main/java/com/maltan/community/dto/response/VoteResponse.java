package com.maltan.community.dto.response;

import com.maltan.community.model.VoteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {
    private Integer likeCount;
    private Integer dislikeCount;
    private VoteType userVoteType;  // 사용자의 현재 투표 상태
}

