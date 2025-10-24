package com.maltan.community.service;

import com.maltan.community.dto.ParticipantDto;
import com.maltan.community.dto.response.ParticipantListResponse;
import com.maltan.community.dto.response.ParticipateResponse;
import com.maltan.community.exception.NotRecruitmentPostException;
import com.maltan.community.exception.PostNotFoundException;
import com.maltan.community.exception.RecruitmentClosedException;
import com.maltan.community.exception.RecruitmentFullException;
import com.maltan.community.model.*;
import com.maltan.community.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentService {
    
    private final PostRepository postRepository;
    private final RecruitmentParticipantRepository participantRepository;
    
    /**
     * 모집 참여/취소 토글
     */
    @Transactional
    public ParticipateResponse toggleParticipation(Long postId, Long userId) {
        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
        
        // 모집 게시글인지 확인
        if (!post.getIsRecruitment()) {
            throw new NotRecruitmentPostException();
        }
        
        // 마감 시간 확인
        if (post.isExpired()) {
            throw new RecruitmentClosedException();
        }
        
        // 기존 참여 여부 확인
        Optional<RecruitmentParticipant> existingParticipant = 
            participantRepository.findByPostIdAndUserId(postId, userId);
        
        if (existingParticipant.isPresent()) {
            RecruitmentParticipant participant = existingParticipant.get();
            
            if (participant.getStatus() == ParticipantStatus.JOINED) {
                // 참여 중 → 취소
                participant.cancel();
                post.decrementCurrentCount();
                
                log.info("모집 참여 취소: postId={}, userId={}", postId, userId);
                
                return ParticipateResponse.builder()
                    .isJoined(false)
                    .currentCount(post.getRecruitmentCurrent())
                    .maxCount(post.getRecruitmentMax())
                    .message("참여가 취소되었습니다.")
                    .build();
            } else {
                // 취소 상태 → 다시 참여
                if (post.isFull()) {
                    throw new RecruitmentFullException();
                }
                
                participant.rejoin();
                post.incrementCurrentCount();
                
                log.info("모집 재참여: postId={}, userId={}", postId, userId);
                
                return ParticipateResponse.builder()
                    .isJoined(true)
                    .currentCount(post.getRecruitmentCurrent())
                    .maxCount(post.getRecruitmentMax())
                    .message("모집에 참여했습니다.")
                    .build();
            }
        } else {
            // 신규 참여
            if (post.isFull()) {
                throw new RecruitmentFullException();
            }
            
            RecruitmentParticipant participant = RecruitmentParticipant.builder()
                .postId(postId)
                .userId(userId)
                .status(ParticipantStatus.JOINED)
                .build();
            
            participantRepository.save(participant);
            post.incrementCurrentCount();
            
            log.info("모집 참여: postId={}, userId={}", postId, userId);
            
            return ParticipateResponse.builder()
                .isJoined(true)
                .currentCount(post.getRecruitmentCurrent())
                .maxCount(post.getRecruitmentMax())
                .message("모집에 참여했습니다.")
                .build();
        }
    }
    
    /**
     * 참여자 목록 조회
     */
    @Transactional(readOnly = true)
    public ParticipantListResponse getParticipants(Long postId) {
        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
        
        if (!post.getIsRecruitment()) {
            throw new NotRecruitmentPostException();
        }
        
        List<RecruitmentParticipant> participants = participantRepository
            .findByPostIdAndStatus(postId, ParticipantStatus.JOINED);
        
        List<ParticipantDto> participantDtos = participants.stream()
            .map(p -> ParticipantDto.builder()
                .userId(p.getUserId())
                // TODO: User Service에서 사용자 정보 조회
                .nickname("사용자" + p.getUserId())
                .profileImage(null)
                .joinedAt(p.getJoinedAt())
                .build())
            .collect(Collectors.toList());
        
        return ParticipantListResponse.builder()
            .participants(participantDtos)
            .currentCount(post.getRecruitmentCurrent())
            .maxCount(post.getRecruitmentMax())
            .isFull(post.isFull())
            .build();
    }
    
    /**
     * 만료된 모집 게시글 자동 마감
     */
    @Transactional
    public void closeExpiredRecruitments() {
        List<Post> expiredPosts = postRepository.findExpiredRecruitmentPosts(
            PostStatus.ACTIVE, 
            LocalDateTime.now()
        );
        
        expiredPosts.forEach(post -> {
            post.close();
            log.info("모집 자동 마감: postId={}, title={}", post.getId(), post.getTitle());
        });
        
        if (!expiredPosts.isEmpty()) {
            log.info("만료된 모집 게시글 {}개 마감 처리", expiredPosts.size());
        }
    }
}

