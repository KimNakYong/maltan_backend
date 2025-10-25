-- Docker를 사용한 MySQL 설정
-- docker-compose.yml과 함께 사용

-- Docker 컨테이너에서 실행할 때 사용하는 스크립트
-- 초기 데이터베이스 설정

-- 데이터베이스 생성 (Docker 환경)
CREATE DATABASE IF NOT EXISTS user_service 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE user_service;

-- 기본 관리자 계정 생성
INSERT IGNORE INTO users (username, email, password, name, phone_number, role, is_enabled) VALUES
('admin', 'admin@user-service.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '시스템 관리자', '010-0000-0000', 'ADMIN', TRUE);

-- 관리자 선호 지역 설정
INSERT IGNORE INTO preferred_regions (user_id, city, city_name, district, district_name, priority) VALUES
(1, 'seoul', '서울특별시', 'gangnam', '강남구', 1);

-- 테스트용 사용자 계정들
INSERT IGNORE INTO users (username, email, password, name, phone_number, role, is_enabled) VALUES
('testuser1', 'test1@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '테스트사용자1', '010-1111-1111', 'USER', TRUE),
('testuser2', 'test2@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '테스트사용자2', '010-2222-2222', 'USER', TRUE);

-- 테스트 사용자 선호 지역
INSERT IGNORE INTO preferred_regions (user_id, city, city_name, district, district_name, priority) VALUES
(2, 'seoul', '서울특별시', 'gangnam', '강남구', 1),
(2, 'seoul', '서울특별시', 'mapo', '마포구', 2),
(3, 'busan', '부산광역시', 'haeundae', '해운대구', 1);

-- 데이터 확인
SELECT 'Database setup completed successfully!' as status;
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as region_count FROM preferred_regions;
