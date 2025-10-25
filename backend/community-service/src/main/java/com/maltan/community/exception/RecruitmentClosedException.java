package com.maltan.community.exception;

public class RecruitmentClosedException extends RuntimeException {
    public RecruitmentClosedException() {
        super("모집이 마감되었습니다.");
    }
    
    public RecruitmentClosedException(String message) {
        super(message);
    }
}

