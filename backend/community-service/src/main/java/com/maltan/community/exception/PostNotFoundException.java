package com.maltan.community.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException() {
        super("게시글을 찾을 수 없습니다.");
    }
    
    public PostNotFoundException(Long postId) {
        super("게시글을 찾을 수 없습니다. ID: " + postId);
    }
    
    public PostNotFoundException(String message) {
        super(message);
    }
}

