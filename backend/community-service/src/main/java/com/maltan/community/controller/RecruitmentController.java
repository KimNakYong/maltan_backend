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
            @RequestAttribute(value = "userId", required = false) Long userIdFromAttribute,
            @RequestParam(required = false) Long userId
    ) {
        // 임시: 인증 시스템 구현 전까지 쿼리 파라미터에서 userId 사용
        Long finalUserId = userIdFromAttribute != null ? userIdFromAttribute : userId;
        if (finalUserId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        ParticipateResponse response = recruitmentService.toggleParticipation(postId, finalUserId);
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

