package com.maltan.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipateResponse {
    private Boolean isJoined;
    private Integer currentCount;
    private Integer maxCount;
    private String message;
}

