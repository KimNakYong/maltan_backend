package com.maltan.community.controller;

import com.maltan.community.dto.request.VoteRequest;
import com.maltan.community.dto.response.VoteResponse;
import com.maltan.community.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class VoteController {
    
    private final VoteService voteService;
    
    // ===== 게시글 투표 =====
    
    @PostMapping("/posts/{postId}/vote")
    public ResponseEntity<VoteResponse> votePost(
            @PathVariable Long postId,
            @Valid @RequestBody VoteRequest request,
            @RequestAttribute(value = "userId", required = false) Long userIdFromAttribute
    ) {
        // 임시: 인증 시스템 구현 전까지 요청 본문에서 userId 사용
        Long userId = userIdFromAttribute != null ? userIdFromAttribute : request.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        VoteResponse response = voteService.votePost(postId, request, userId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/posts/{postId}/vote")
    public ResponseEntity<VoteResponse> cancelPostVote(
            @PathVariable Long postId,
            @RequestAttribute(value = "userId", required = false) Long userIdFromAttribute,
            @RequestParam(required = false) Long userId
    ) {
        // 임시: 인증 시스템 구현 전까지 쿼리 파라미터에서 userId 사용
        Long finalUserId = userIdFromAttribute != null ? userIdFromAttribute : userId;
        if (finalUserId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        VoteResponse response = voteService.cancelPostVote(postId, finalUserId);
        return ResponseEntity.ok(response);
    }
    
    // ===== 댓글 투표 =====
    
    @PostMapping("/comments/{commentId}/vote")
    public ResponseEntity<VoteResponse> voteComment(
            @PathVariable Long commentId,
            @Valid @RequestBody VoteRequest request,
            @RequestAttribute(value = "userId", required = false) Long userIdFromAttribute
    ) {
        // 임시: 인증 시스템 구현 전까지 요청 본문에서 userId 사용
        Long userId = userIdFromAttribute != null ? userIdFromAttribute : request.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        VoteResponse response = voteService.voteComment(commentId, request, userId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/comments/{commentId}/vote")
    public ResponseEntity<VoteResponse> cancelCommentVote(
            @PathVariable Long commentId,
            @RequestAttribute(value = "userId", required = false) Long userIdFromAttribute,
            @RequestParam(required = false) Long userId
    ) {
        // 임시: 인증 시스템 구현 전까지 쿼리 파라미터에서 userId 사용
        Long finalUserId = userIdFromAttribute != null ? userIdFromAttribute : userId;
        if (finalUserId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        VoteResponse response = voteService.cancelCommentVote(commentId, finalUserId);
        return ResponseEntity.ok(response);
    }
}

