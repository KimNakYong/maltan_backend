package com.maltan.community.dto.request;

import com.maltan.community.model.VoteType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {
    
    @NotNull(message = "투표 타입은 필수입니다.")
    private VoteType voteType;  // LIKE 또는 DISLIKE
}

