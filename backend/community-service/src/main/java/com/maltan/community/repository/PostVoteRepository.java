package com.maltan.community.repository;

import com.maltan.community.model.PostVote;
import com.maltan.community.model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Long> {
    
    // 사용자의 특정 게시글 투표 조회
    Optional<PostVote> findByPostIdAndUserId(Long postId, Long userId);
    
    // 사용자가 특정 게시글에 투표했는지 확인
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    
    // 특정 게시글의 특정 타입 투표 수 카운트
    long countByPostIdAndVoteType(Long postId, VoteType voteType);
    
    // 사용자의 투표 삭제
    void deleteByPostIdAndUserId(Long postId, Long userId);
}

