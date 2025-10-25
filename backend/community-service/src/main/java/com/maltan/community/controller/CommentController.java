package com.maltan.community.controller;

import com.maltan.community.dto.CommentDto;
import com.maltan.community.dto.request.CreateCommentRequest;
import com.maltan.community.dto.request.UpdateCommentRequest;
import com.maltan.community.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    /**
     * 게시글의 댓글 목록 조회
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(
            @PathVariable Long postId,
            @RequestAttribute(value = "userId", required = false) Long userId
    ) {
        List<CommentDto> comments = commentService.getComments(postId, userId);
        return ResponseEntity.ok(comments);
    }
    
    /**
     * 댓글 작성
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            @RequestAttribute("userId") Long userId
    ) {
        CommentDto comment = commentService.createComment(postId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
    
    /**
     * 댓글 수정
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @RequestAttribute("userId") Long userId
    ) {
        CommentDto comment = commentService.updateComment(commentId, request, userId);
        return ResponseEntity.ok(comment);
    }
    
    /**
     * 댓글 삭제
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long userId
    ) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}

