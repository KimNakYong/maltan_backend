package com.maltan.community.exception;

public class NotRecruitmentPostException extends RuntimeException {
    public NotRecruitmentPostException() {
        super("모집 게시글이 아닙니다.");
    }
    
    public NotRecruitmentPostException(String message) {
        super(message);
    }
}

