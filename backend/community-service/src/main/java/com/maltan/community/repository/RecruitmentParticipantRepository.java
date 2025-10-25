package com.maltan.community.repository;

import com.maltan.community.model.ParticipantStatus;
import com.maltan.community.model.RecruitmentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentParticipantRepository extends JpaRepository<RecruitmentParticipant, Long> {
    
    // 특정 게시글의 특정 사용자 참여 조회
    Optional<RecruitmentParticipant> findByPostIdAndUserId(Long postId, Long userId);
    
    // 특정 게시글의 참여자 목록 (참여 중인 사람만)
    List<RecruitmentParticipant> findByPostIdAndStatus(Long postId, ParticipantStatus status);
    
    // 특정 게시글의 참여 중인 인원 수
    @Query("SELECT COUNT(rp) FROM RecruitmentParticipant rp " +
           "WHERE rp.postId = :postId AND rp.status = :status")
    Long countByPostIdAndStatus(@Param("postId") Long postId, @Param("status") ParticipantStatus status);
    
    // 사용자가 참여한 모집 게시글 목록
    List<RecruitmentParticipant> findByUserIdAndStatus(Long userId, ParticipantStatus status);
    
    // 사용자가 특정 게시글에 참여 중인지 확인
    @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END " +
           "FROM RecruitmentParticipant rp " +
           "WHERE rp.postId = :postId AND rp.userId = :userId AND rp.status = :status")
    boolean existsByPostIdAndUserIdAndStatus(
        @Param("postId") Long postId, 
        @Param("userId") Long userId,
        @Param("status") ParticipantStatus status
    );
}

