package com.maltan.community.scheduler;

import com.maltan.community.model.Post;
import com.maltan.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularPostScheduler {
    
    private final PostRepository postRepository;
    
    /**
     * 매일 자정에 인기 게시글 선정 및 고정
     * 지난 24시간 동안 가장 많은 추천을 받은 게시글을 선정
     */
    @Scheduled(cron = "0 0 0 * * *")  // 매일 00:00:00에 실행
    @Transactional
    public void updatePopularPost() {
        log.info("인기 게시글 선정 스케줄러 시작");
        
        try {
            // 기존 고정 게시글 해제
            unpinExpiredPosts();
            
            // 지난 24시간 동안 가장 많은 추천을 받은 게시글 찾기
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            List<Post> topPosts = postRepository.findTopByLikeCountSince(yesterday, 1);
            
            if (!topPosts.isEmpty()) {
                Post topPost = topPosts.get(0);
                
                // 최소 추천 수 조건 (예: 10개 이상)
                if (topPost.getLikeCount() >= 10) {
                    // 24시간 동안 고정
                    LocalDateTime pinnedUntil = LocalDateTime.now().plusDays(1);
                    topPost.pin(pinnedUntil);
                    postRepository.save(topPost);
                    
                    log.info("인기 게시글 선정 완료: postId={}, title={}, likeCount={}", 
                        topPost.getId(), topPost.getTitle(), topPost.getLikeCount());
                } else {
                    log.info("추천 수가 부족하여 인기 게시글 미선정 (최소 10개 필요, 현재: {})", 
                        topPost.getLikeCount());
                }
            } else {
                log.info("지난 24시간 동안 게시글이 없어 인기 게시글 미선정");
            }
        } catch (Exception e) {
            log.error("인기 게시글 선정 중 오류 발생", e);
        }
    }
    
    /**
     * 매 시간마다 만료된 고정 게시글 해제
     */
    @Scheduled(cron = "0 0 * * * *")  // 매 시간 정각에 실행
    @Transactional
    public void unpinExpiredPosts() {
        log.info("만료된 고정 게시글 해제 스케줄러 시작");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Post> expiredPosts = postRepository.findByIsPinnedTrueAndPinnedUntilBefore(now);
            
            for (Post post : expiredPosts) {
                post.unpin();
                postRepository.save(post);
                log.info("고정 해제: postId={}, title={}", post.getId(), post.getTitle());
            }
            
            if (!expiredPosts.isEmpty()) {
                log.info("총 {}개의 고정 게시글 해제 완료", expiredPosts.size());
            }
        } catch (Exception e) {
            log.error("고정 게시글 해제 중 오류 발생", e);
        }
    }
}

