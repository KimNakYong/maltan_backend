package com.maltan.community.repository;

import com.maltan.community.model.CommentVote;
import com.maltan.community.model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    
    // 사용자의 특정 댓글 투표 조회
    Optional<CommentVote> findByCommentIdAndUserId(Long commentId, Long userId);
    
    // 사용자가 특정 댓글에 투표했는지 확인
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
    
    // 특정 댓글의 특정 타입 투표 수 카운트
    long countByCommentIdAndVoteType(Long commentId, VoteType voteType);
    
    // 사용자의 투표 삭제
    void deleteByCommentIdAndUserId(Long commentId, Long userId);
}

