package com.maltan.community.service;

import com.maltan.community.dto.CommentDto;
import com.maltan.community.dto.request.CreateCommentRequest;
import com.maltan.community.dto.request.UpdateCommentRequest;
import com.maltan.community.exception.CommentNotFoundException;
import com.maltan.community.exception.PostNotFoundException;
import com.maltan.community.exception.UnauthorizedException;
import com.maltan.community.model.*;
import com.maltan.community.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentVoteRepository commentVoteRepository;
    
    /**
     * 게시글의 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long postId, Long currentUserId) {
        // 부모 댓글만 조회
        List<Comment> parentComments = commentRepository.findByPostIdAndParentCommentIsNull(postId);
        
        return parentComments.stream()
            .map(comment -> convertToDto(comment, currentUserId))
            .collect(Collectors.toList());
    }
    
    /**
     * 댓글 작성
     */
    @Transactional
    public CommentDto createComment(Long postId, CreateCommentRequest request, Long userId) {
        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
        
        Comment comment = Comment.builder()
            .post(post)
            .userId(userId)
            .content(request.getContent())
            .build();
        
        // 대댓글인 경우
        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findByIdAndIsDeletedFalse(request.getParentCommentId())
                .orElseThrow(() -> new CommentNotFoundException(request.getParentCommentId()));
            comment.setParentComment(parentComment);
        }
        
        Comment savedComment = commentRepository.save(comment);
        
        // 게시글의 댓글 수 증가
        post.incrementCommentCount();
        
        log.info("댓글 작성 완료: commentId={}, postId={}, userId={}", savedComment.getId(), postId, userId);
        
        return convertToDto(savedComment, userId);
    }
    
    /**
     * 댓글 수정
     */
    @Transactional
    public CommentDto updateComment(Long commentId, UpdateCommentRequest request, Long userId) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));
        
        // 작성자 확인
        if (!comment.getUserId().equals(userId)) {
            throw new UnauthorizedException("댓글을 수정할 권한이 없습니다.");
        }
        
        comment.setContent(request.getContent());
        
        log.info("댓글 수정 완료: commentId={}, userId={}", commentId, userId);
        
        return convertToDto(comment, userId);
    }
    
    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));
        
        // 작성자 확인
        if (!comment.getUserId().equals(userId)) {
            throw new UnauthorizedException("댓글을 삭제할 권한이 없습니다.");
        }
        
        // Soft Delete
        comment.softDelete();
        
        // 게시글의 댓글 수 감소
        Post post = comment.getPost();
        post.decrementCommentCount();
        
        log.info("댓글 삭제 완료: commentId={}, userId={}", commentId, userId);
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private CommentDto convertToDto(Comment comment, Long currentUserId) {
        // 대댓글 목록
        List<CommentDto> replyDtos = new ArrayList<>();
        if (!comment.isReply()) {
            replyDtos = commentRepository.findByParentCommentId(comment.getId()).stream()
                .map(reply -> convertToDto(reply, currentUserId))
                .collect(Collectors.toList());
        }
        
        // 사용자의 투표 상태 확인
        Boolean isLiked = false;
        Boolean isDisliked = false;
        if (currentUserId != null) {
            commentVoteRepository.findByCommentIdAndUserId(comment.getId(), currentUserId)
                .ifPresent(vote -> {
                    // 투표 상태 설정 로직
                });
        }
        
        return CommentDto.builder()
            .id(comment.getId())
            .postId(comment.getPost().getId())
            .userId(comment.getUserId())
            .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
            .content(comment.getContent())
            .likeCount(comment.getLikeCount())
            .dislikeCount(comment.getDislikeCount())
            .isLiked(isLiked)
            .isDisliked(isDisliked)
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .replies(replyDtos)
            .build();
    }
}

