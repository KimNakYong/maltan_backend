package com.maltan.community.service;

import com.maltan.community.dto.request.VoteRequest;
import com.maltan.community.dto.response.VoteResponse;
import com.maltan.community.exception.CommentNotFoundException;
import com.maltan.community.exception.PostNotFoundException;
import com.maltan.community.model.*;
import com.maltan.community.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteService {
    
    private final PostRepository postRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    
    /**
     * 게시글 추천/비추천
     */
    @Transactional
    public VoteResponse votePost(Long postId, VoteRequest request, Long userId) {
        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
        
        // 기존 투표 확인
        PostVote existingVote = postVoteRepository.findByPostIdAndUserId(postId, userId)
            .orElse(null);
        
        if (existingVote != null) {
            // 같은 타입으로 재투표 → 투표 취소
            if (existingVote.getVoteType() == request.getVoteType()) {
                postVoteRepository.delete(existingVote);
                updatePostVoteCount(post, request.getVoteType(), false);
                log.info("게시글 투표 취소: postId={}, userId={}, voteType={}", postId, userId, request.getVoteType());
                
                return VoteResponse.builder()
                    .likeCount(post.getLikeCount())
                    .dislikeCount(post.getDislikeCount())
                    .userVoteType(null)
                    .build();
            } else {
                // 다른 타입으로 변경 → 기존 투표 타입 카운트 감소, 새 타입 카운트 증가
                updatePostVoteCount(post, existingVote.getVoteType(), false);
                updatePostVoteCount(post, request.getVoteType(), true);
                existingVote.setVoteType(request.getVoteType());
                log.info("게시글 투표 변경: postId={}, userId={}, voteType={}", postId, userId, request.getVoteType());
            }
        } else {
            // 신규 투표
            PostVote newVote = PostVote.builder()
                .postId(postId)
                .userId(userId)
                .voteType(request.getVoteType())
                .build();
            postVoteRepository.save(newVote);
            updatePostVoteCount(post, request.getVoteType(), true);
            log.info("게시글 투표 등록: postId={}, userId={}, voteType={}", postId, userId, request.getVoteType());
        }
        
        return VoteResponse.builder()
            .likeCount(post.getLikeCount())
            .dislikeCount(post.getDislikeCount())
            .userVoteType(request.getVoteType())
            .build();
    }
    
    /**
     * 게시글 투표 취소
     */
    @Transactional
    public VoteResponse cancelPostVote(Long postId, Long userId) {
        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
        
        PostVote vote = postVoteRepository.findByPostIdAndUserId(postId, userId)
            .orElse(null);
        
        if (vote != null) {
            updatePostVoteCount(post, vote.getVoteType(), false);
            postVoteRepository.delete(vote);
            log.info("게시글 투표 취소: postId={}, userId={}", postId, userId);
        }
        
        return VoteResponse.builder()
            .likeCount(post.getLikeCount())
            .dislikeCount(post.getDislikeCount())
            .userVoteType(null)
            .build();
    }
    
    /**
     * 댓글 추천/비추천
     */
    @Transactional
    public VoteResponse voteComment(Long commentId, VoteRequest request, Long userId) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));
        
        // 기존 투표 확인
        CommentVote existingVote = commentVoteRepository.findByCommentIdAndUserId(commentId, userId)
            .orElse(null);
        
        if (existingVote != null) {
            // 같은 타입으로 재투표 → 투표 취소
            if (existingVote.getVoteType() == request.getVoteType()) {
                commentVoteRepository.delete(existingVote);
                updateCommentVoteCount(comment, request.getVoteType(), false);
                log.info("댓글 투표 취소: commentId={}, userId={}, voteType={}", commentId, userId, request.getVoteType());
                
                return VoteResponse.builder()
                    .likeCount(comment.getLikeCount())
                    .dislikeCount(comment.getDislikeCount())
                    .userVoteType(null)
                    .build();
            } else {
                // 다른 타입으로 변경
                updateCommentVoteCount(comment, existingVote.getVoteType(), false);
                updateCommentVoteCount(comment, request.getVoteType(), true);
                existingVote.setVoteType(request.getVoteType());
                log.info("댓글 투표 변경: commentId={}, userId={}, voteType={}", commentId, userId, request.getVoteType());
            }
        } else {
            // 신규 투표
            CommentVote newVote = CommentVote.builder()
                .commentId(commentId)
                .userId(userId)
                .voteType(request.getVoteType())
                .build();
            commentVoteRepository.save(newVote);
            updateCommentVoteCount(comment, request.getVoteType(), true);
            log.info("댓글 투표 등록: commentId={}, userId={}, voteType={}", commentId, userId, request.getVoteType());
        }
        
        return VoteResponse.builder()
            .likeCount(comment.getLikeCount())
            .dislikeCount(comment.getDislikeCount())
            .userVoteType(request.getVoteType())
            .build();
    }
    
    /**
     * 댓글 투표 취소
     */
    @Transactional
    public VoteResponse cancelCommentVote(Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));
        
        CommentVote vote = commentVoteRepository.findByCommentIdAndUserId(commentId, userId)
            .orElse(null);
        
        if (vote != null) {
            updateCommentVoteCount(comment, vote.getVoteType(), false);
            commentVoteRepository.delete(vote);
            log.info("댓글 투표 취소: commentId={}, userId={}", commentId, userId);
        }
        
        return VoteResponse.builder()
            .likeCount(comment.getLikeCount())
            .dislikeCount(comment.getDislikeCount())
            .userVoteType(null)
            .build();
    }
    
    /**
     * 게시글 투표 카운트 업데이트
     */
    private void updatePostVoteCount(Post post, VoteType voteType, boolean increment) {
        if (voteType == VoteType.LIKE) {
            if (increment) {
                post.incrementLikeCount();
            } else {
                post.decrementLikeCount();
            }
        } else if (voteType == VoteType.DISLIKE) {
            if (increment) {
                post.incrementDislikeCount();
            } else {
                post.decrementDislikeCount();
            }
        }
    }
    
    /**
     * 댓글 투표 카운트 업데이트
     */
    private void updateCommentVoteCount(Comment comment, VoteType voteType, boolean increment) {
        if (voteType == VoteType.LIKE) {
            if (increment) {
                comment.incrementLikeCount();
            } else {
                comment.decrementLikeCount();
            }
        } else if (voteType == VoteType.DISLIKE) {
            if (increment) {
                comment.incrementDislikeCount();
            } else {
                comment.decrementDislikeCount();
            }
        }
    }
}

