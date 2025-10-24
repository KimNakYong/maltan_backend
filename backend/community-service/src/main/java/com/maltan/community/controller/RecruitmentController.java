package com.maltan.community.controller;

import com.maltan.community.dto.response.ParticipantListResponse;
import com.maltan.community.dto.response.ParticipateResponse;
import com.maltan.community.service.RecruitmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
public class RecruitmentController {
    
    private final RecruitmentService recruitmentService;
    
    /**
     * 모집 참여/취소 토글
     */
    @PostMapping("/{postId}/participate")
    public ResponseEntity<ParticipateResponse> toggleParticipation(
            @PathVariable Long postId,
            @RequestAttribute("userId") Long userId
    ) {
        ParticipateResponse response = recruitmentService.toggleParticipation(postId, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 참여자 목록 조회
     */
    @GetMapping("/{postId}/participants")
    public ResponseEntity<ParticipantListResponse> getParticipants(
            @PathVariable Long postId
    ) {
        ParticipantListResponse response = recruitmentService.getParticipants(postId);
        return ResponseEntity.ok(response);
    }
}

