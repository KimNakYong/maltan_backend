package com.maltan.community.scheduler;

import com.maltan.community.service.RecruitmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecruitmentScheduler {
    
    private final RecruitmentService recruitmentService;
    
    /**
     * 만료된 모집 게시글 자동 마감
     * 10분마다 실행
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void closeExpiredRecruitments() {
        log.debug("만료된 모집 게시글 체크 시작");
        recruitmentService.closeExpiredRecruitments();
    }
}

