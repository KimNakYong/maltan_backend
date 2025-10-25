package com.maltan.community.dto.response;

import com.maltan.community.dto.ParticipantDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantListResponse {
    private List<ParticipantDto> participants;
    private Integer currentCount;
    private Integer maxCount;
    private Boolean isFull;
}

