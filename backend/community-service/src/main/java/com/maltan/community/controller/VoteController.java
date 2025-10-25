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
            @RequestAttribute("userId") Long userId
    ) {
        VoteResponse response = voteService.votePost(postId, request, userId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/posts/{postId}/vote")
    public ResponseEntity<VoteResponse> cancelPostVote(
            @PathVariable Long postId,
            @RequestAttribute("userId") Long userId
    ) {
        VoteResponse response = voteService.cancelPostVote(postId, userId);
        return ResponseEntity.ok(response);
    }
    
    // ===== 댓글 투표 =====
    
    @PostMapping("/comments/{commentId}/vote")
    public ResponseEntity<VoteResponse> voteComment(
            @PathVariable Long commentId,
            @Valid @RequestBody VoteRequest request,
            @RequestAttribute("userId") Long userId
    ) {
        VoteResponse response = voteService.voteComment(commentId, request, userId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/comments/{commentId}/vote")
    public ResponseEntity<VoteResponse> cancelCommentVote(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long userId
    ) {
        VoteResponse response = voteService.cancelCommentVote(commentId, userId);
        return ResponseEntity.ok(response);
    }
}

