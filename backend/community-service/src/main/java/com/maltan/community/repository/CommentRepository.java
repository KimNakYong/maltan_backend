package com.maltan.community.repository;

import com.maltan.community.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 게시글의 댓글 조회 (대댓글 제외)
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId " +
           "AND c.parentComment IS NULL " +
           "AND c.isDeleted = false " +
           "ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdAndParentCommentIsNull(@Param("postId") Long postId);
    
    // 대댓글 조회
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId " +
           "AND c.isDeleted = false " +
           "ORDER BY c.createdAt ASC")
    List<Comment> findByParentCommentId(@Param("parentCommentId") Long parentCommentId);
    
    // 사용자별 댓글 조회
    Page<Comment> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
    
    // ID로 조회 (삭제되지 않은 것만)
    Optional<Comment> findByIdAndIsDeletedFalse(Long id);
    
    // 게시글의 댓글 수 카운트
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.isDeleted = false")
    Long countByPostId(@Param("postId") Long postId);
    
    // 통계용 메서드
    Long countByIsDeletedFalse();
}

