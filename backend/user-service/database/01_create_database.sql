-- User Service Database Schema
-- MySQL 8.0+ 버전용

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS user_service 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE user_service;

-- Users 테이블 생성
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '사용자명',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일',
    password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    name VARCHAR(100) NOT NULL COMMENT '실제 이름',
    phone_number VARCHAR(20) COMMENT '전화번호',
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER' COMMENT '사용자 역할',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '계정 활성화 상태',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_created_at (created_at),
    INDEX idx_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 정보 테이블';

-- Preferred Regions 테이블 생성
CREATE TABLE preferred_regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    city VARCHAR(50) NOT NULL COMMENT '도시 코드',
    city_name VARCHAR(100) NOT NULL COMMENT '도시명',
    district VARCHAR(50) NOT NULL COMMENT '구/군 코드',
    district_name VARCHAR(100) NOT NULL COMMENT '구/군명',
    priority INT NOT NULL COMMENT '우선순위 (1-3)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_city (city),
    INDEX idx_priority (priority),
    UNIQUE KEY uk_user_priority (user_id, priority) COMMENT '사용자별 우선순위는 유일해야 함',
    CONSTRAINT chk_priority CHECK (priority BETWEEN 1 AND 3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 선호 지역 테이블';

-- 사용자 세션 테이블 (선택사항)
CREATE TABLE user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    session_token VARCHAR(255) NOT NULL UNIQUE COMMENT '세션 토큰',
    expires_at TIMESTAMP NOT NULL COMMENT '만료일시',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_session_token (session_token),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 세션 테이블';

-- 로그인 이력 테이블 (선택사항)
CREATE TABLE login_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    login_ip VARCHAR(45) COMMENT '로그인 IP',
    user_agent TEXT COMMENT '사용자 에이전트',
    login_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '로그인 일시',
    success BOOLEAN NOT NULL DEFAULT TRUE COMMENT '로그인 성공 여부',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_login_at (login_at),
    INDEX idx_success (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='로그인 이력 테이블';
