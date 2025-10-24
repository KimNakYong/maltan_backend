package com.maltan.community.exception;

public class RecruitmentFullException extends RuntimeException {
    public RecruitmentFullException() {
        super("모집 인원이 마감되었습니다.");
    }
    
    public RecruitmentFullException(String message) {
        super(message);
    }
}

