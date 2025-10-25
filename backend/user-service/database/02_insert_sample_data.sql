-- User Service Sample Data
-- 샘플 데이터 삽입 스크립트

USE user_service;

-- 샘플 사용자 데이터 삽입
INSERT INTO users (username, email, password, name, phone_number, role, is_enabled) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '관리자', '010-0000-0000', 'ADMIN', TRUE),
('honggildong', 'hong@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '홍길동', '010-1111-1111', 'USER', TRUE),
('kimcheolsu', 'kim@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '김철수', '010-2222-2222', 'USER', TRUE),
('leeyounghee', 'lee@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '이영희', '010-3333-3333', 'USER', TRUE),
('parkminsu', 'park@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '박민수', '010-4444-4444', 'USER', TRUE);

-- 샘플 선호 지역 데이터 삽입
INSERT INTO preferred_regions (user_id, city, city_name, district, district_name, priority) VALUES
-- 관리자 (1순위: 강남구)
(1, 'seoul', '서울특별시', 'gangnam', '강남구', 1),

-- 홍길동 (1순위: 강남구, 2순위: 마포구)
(2, 'seoul', '서울특별시', 'gangnam', '강남구', 1),
(2, 'seoul', '서울특별시', 'mapo', '마포구', 2),

-- 김철수 (1순위: 해운대구)
(3, 'busan', '부산광역시', 'haeundae', '해운대구', 1),

-- 이영희 (1순위: 종로구, 2순위: 중구, 3순위: 용산구)
(4, 'seoul', '서울특별시', 'jongno', '종로구', 1),
(4, 'seoul', '서울특별시', 'jung', '중구', 2),
(4, 'seoul', '서울특별시', 'yongsan', '용산구', 3),

-- 박민수 (1순위: 수원시, 2순위: 성남시)
(5, 'gyeonggi', '경기도', 'suwon', '수원시', 1),
(5, 'gyeonggi', '경기도', 'seongnam', '성남시', 2);

-- 샘플 로그인 이력 데이터 삽입
INSERT INTO login_history (user_id, login_ip, user_agent, success) VALUES
(1, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', TRUE),
(2, '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', TRUE),
(3, '192.168.1.102', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15', TRUE),
(4, '192.168.1.103', 'Mozilla/5.0 (Android 10; Mobile; rv:81.0) Gecko/81.0 Firefox/81.0', TRUE),
(5, '192.168.1.104', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', FALSE);

-- 데이터 확인 쿼리
SELECT 
    u.id,
    u.username,
    u.email,
    u.name,
    u.phone_number,
    u.role,
    u.is_enabled,
    COUNT(pr.id) as region_count
FROM users u
LEFT JOIN preferred_regions pr ON u.id = pr.user_id
GROUP BY u.id, u.username, u.email, u.name, u.phone_number, u.role, u.is_enabled
ORDER BY u.id;

-- 선호 지역 상세 조회
SELECT 
    u.username,
    u.name,
    pr.city_name,
    pr.district_name,
    pr.priority
FROM users u
JOIN preferred_regions pr ON u.id = pr.user_id
ORDER BY u.username, pr.priority;
