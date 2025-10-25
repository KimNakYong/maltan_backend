package com.maltan.community.controller;

import com.maltan.community.dto.response.CommunityStatistics;
import com.maltan.community.model.PostStatus;
import com.maltan.community.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final RecruitmentParticipantRepository participantRepository;
    
    @GetMapping
    public ResponseEntity<CommunityStatistics> getStatistics() {
        // 전체 게시글 수 (삭제되지 않은 것만)
        Long totalPosts = postRepository.countByIsDeletedFalse();
        
        // 전체 댓글 수 (삭제되지 않은 것만)
        Long totalComments = commentRepository.countByIsDeletedFalse();
        
        // 모집 게시글 수
        Long totalRecruitmentPosts = postRepository.countByIsRecruitmentTrueAndIsDeletedFalse();
        
        // 활성 모집 게시글 수 (OPEN 상태)
        Long activeRecruitmentPosts = postRepository.countByIsRecruitmentTrueAndStatusAndIsDeletedFalse(PostStatus.OPEN);
        
        // 전체 투표 수 (게시글 + 댓글)
        Long totalVotes = postVoteRepository.count() + commentVoteRepository.count();
        
        // 전체 참여자 수
        Long totalParticipants = participantRepository.count();
        
        CommunityStatistics statistics = CommunityStatistics.builder()
            .totalPosts(totalPosts)
            .totalComments(totalComments)
            .totalRecruitmentPosts(totalRecruitmentPosts)
            .activeRecruitmentPosts(activeRecruitmentPosts)
            .totalVotes(totalVotes)
            .totalParticipants(totalParticipants)
            .build();
        
        return ResponseEntity.ok(statistics);
    }
}

