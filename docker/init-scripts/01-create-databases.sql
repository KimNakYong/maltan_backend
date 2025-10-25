-- 데이터베이스 생성 (존재하지 않으면)
CREATE DATABASE IF NOT EXISTS userdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- userdb 데이터베이스 사용
USE userdb;

-- 기존 테이블이 있다면 삭제
DROP TABLE IF EXISTS users;

-- users 테이블 생성 (선호 지역을 JSON 타입으로 통합)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '사용자명',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '이메일',
    password VARCHAR(255) NOT NULL COMMENT '비밀번호',
    name VARCHAR(100) NOT NULL COMMENT '이름',
    phone_number VARCHAR(20) COMMENT '전화번호',
    preferred_regions JSON COMMENT '선호 지역 (JSON 배열)',
    role ENUM('USER', 'ADMIN') DEFAULT 'USER' COMMENT '사용자 역할',
    is_enabled BOOLEAN DEFAULT TRUE COMMENT '계정 활성화 상태',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_created_at (created_at),
    INDEX idx_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 정보 테이블';

-- 샘플 데이터 삽입
INSERT INTO users (username, email, password, name, phone_number, role, is_enabled, preferred_regions) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '관리자', '010-0000-0000', 'ADMIN', TRUE, '[{"city": "seoul", "cityName": "서울특별시", "district": "gangnam", "districtName": "강남구", "priority": 1}]'),
('testuser', 'test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '테스트사용자', '010-1234-5678', 'USER', TRUE, '[{"city": "seoul", "cityName": "서울특별시", "district": "gangnam", "districtName": "강남구", "priority": 1}]');
