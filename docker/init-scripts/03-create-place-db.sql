-- Place Service용 MySQL 데이터베이스 초기화 스크립트

-- placedb 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS placedb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- placedb 사용
USE placedb;

-- 카테고리 테이블 생성 및 초기 데이터
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    icon_url VARCHAR(500),
    color VARCHAR(7) DEFAULT '#007bff',
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 장소 테이블 생성
CREATE TABLE IF NOT EXISTS places (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(500) NOT NULL,
    phone VARCHAR(20),
    website VARCHAR(500),
    email VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    category_id BIGINT,
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    review_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    business_hours JSON,
    amenities JSON,
    price_range VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_category_id (category_id),
    INDEX idx_location (latitude, longitude),
    INDEX idx_rating (average_rating),
    INDEX idx_active (is_active),
    INDEX idx_name (name)
);

-- 리뷰 테이블 생성
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    place_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating DECIMAL(2, 1) NOT NULL CHECK (rating >= 1.0 AND rating <= 5.0),
    title VARCHAR(255),
    content TEXT,
    like_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE,
    INDEX idx_place_id (place_id),
    INDEX idx_user_id (user_id),
    INDEX idx_rating (rating),
    INDEX idx_active (is_active),
    INDEX idx_created_at (created_at),
    INDEX idx_like_count (like_count),
    UNIQUE KEY unique_user_place_review (user_id, place_id)
);

-- 사진 테이블 생성
CREATE TABLE IF NOT EXISTS photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    place_id BIGINT,
    review_id BIGINT,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    width INT,
    height INT,
    is_thumbnail BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    uploaded_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE,
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
    INDEX idx_place_id (place_id),
    INDEX idx_review_id (review_id),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_display_order (display_order)
);

-- 카테고리 초기 데이터 삽입
INSERT INTO categories (name, description, icon_url, color, display_order) VALUES
('음식점', '레스토랑, 카페, 바 등 음식 관련 장소', '/icons/restaurant.svg', '#ff6b6b', 1),
('관광지', '명소, 박물관, 공원 등 관광 명소', '/icons/attraction.svg', '#4ecdc4', 2),
('숙박', '호텔, 펜션, 게스트하우스 등 숙박 시설', '/icons/hotel.svg', '#45b7d1', 3),
('쇼핑', '쇼핑몰, 시장, 상점 등 쇼핑 관련 장소', '/icons/shopping.svg', '#96ceb4', 4),
('문화', '극장, 갤러리, 문화센터 등 문화 시설', '/icons/culture.svg', '#feca57', 5),
('스포츠', '체육관, 수영장, 골프장 등 스포츠 시설', '/icons/sports.svg', '#ff9ff3', 6),
('의료', '병원, 약국, 클리닉 등 의료 시설', '/icons/medical.svg', '#54a0ff', 7),
('교육', '학교, 학원, 도서관 등 교육 시설', '/icons/education.svg', '#5f27cd', 8),
('교통', '지하철역, 버스터미널, 공항 등 교통 시설', '/icons/transport.svg', '#00d2d3', 9),
('기타', '기타 분류되지 않은 장소', '/icons/other.svg', '#a4b0be', 10)
ON DUPLICATE KEY UPDATE 
    description = VALUES(description),
    icon_url = VALUES(icon_url),
    color = VALUES(color),
    display_order = VALUES(display_order);

-- 샘플 장소 데이터 삽입 (서울 지역)
INSERT INTO places (name, description, address, phone, latitude, longitude, category_id, business_hours, price_range) VALUES
('경복궁', '조선왕조의 정궁으로 한국의 대표적인 궁궐', '서울특별시 종로구 사직로 161', '02-3700-3900', 37.5796, 126.9770, 2, '{"monday": "09:00-18:00", "tuesday": "09:00-18:00", "wednesday": "09:00-18:00", "thursday": "09:00-18:00", "friday": "09:00-18:00", "saturday": "09:00-18:00", "sunday": "09:00-18:00"}', '저렴'),
('명동성당', '한국 천주교의 상징적인 성당', '서울특별시 중구 명동길 74', '02-774-1784', 37.5633, 126.9870, 2, '{"monday": "06:00-21:00", "tuesday": "06:00-21:00", "wednesday": "06:00-21:00", "thursday": "06:00-21:00", "friday": "06:00-21:00", "saturday": "06:00-21:00", "sunday": "06:00-21:00"}', '무료'),
('롯데월드타워', '서울의 랜드마크 초고층 빌딩', '서울특별시 송파구 올림픽로 300', '1661-2000', 37.5125, 127.1025, 2, '{"monday": "10:00-22:00", "tuesday": "10:00-22:00", "wednesday": "10:00-22:00", "thursday": "10:00-22:00", "friday": "10:00-23:00", "saturday": "10:00-23:00", "sunday": "10:00-22:00"}', '보통'),
('광장시장', '전통 한국 음식을 맛볼 수 있는 재래시장', '서울특별시 종로구 창경궁로 88', '02-2267-4077', 37.5707, 126.9996, 1, '{"monday": "09:00-18:00", "tuesday": "09:00-18:00", "wednesday": "09:00-18:00", "thursday": "09:00-18:00", "friday": "09:00-18:00", "saturday": "09:00-18:00", "sunday": "10:00-17:00"}', '저렴'),
('동대문디자인플라자', '현대적인 디자인의 복합문화공간', '서울특별시 중구 을지로 281', '02-2153-0000', 37.5665, 127.0092, 5, '{"monday": "10:00-22:00", "tuesday": "10:00-22:00", "wednesday": "10:00-22:00", "thursday": "10:00-22:00", "friday": "10:00-22:00", "saturday": "10:00-22:00", "sunday": "10:00-22:00"}', '보통')
ON DUPLICATE KEY UPDATE 
    description = VALUES(description),
    phone = VALUES(phone),
    business_hours = VALUES(business_hours);

-- 샘플 리뷰 데이터 삽입
INSERT INTO reviews (place_id, user_id, rating, title, content) VALUES
(1, 1, 4.5, '한국의 아름다운 궁궐', '경복궁은 정말 아름다운 곳입니다. 특히 가을에 방문하면 단풍과 어우러진 모습이 장관입니다.'),
(1, 2, 5.0, '역사를 느낄 수 있는 곳', '수문장 교대식도 볼 수 있고, 한복을 입고 가면 무료입장이라 더욱 좋았습니다.'),
(2, 1, 4.0, '도심 속 성당', '명동 한복판에 있어 접근성이 좋고, 건축물 자체도 아름답습니다.'),
(3, 3, 4.8, '서울의 랜드마크', '전망대에서 보는 서울 야경이 정말 멋집니다. 다만 가격이 조금 비싸요.'),
(4, 2, 4.2, '전통 음식의 천국', '빈대떡과 마약김밥이 정말 맛있어요. 다만 사람이 너무 많아서 줄을 서야 합니다.')
ON DUPLICATE KEY UPDATE 
    rating = VALUES(rating),
    title = VALUES(title),
    content = VALUES(content);

-- 장소별 평균 평점 및 리뷰 수 업데이트
UPDATE places p 
SET average_rating = (
    SELECT COALESCE(AVG(r.rating), 0) 
    FROM reviews r 
    WHERE r.place_id = p.id AND r.is_active = TRUE
),
review_count = (
    SELECT COUNT(*) 
    FROM reviews r 
    WHERE r.place_id = p.id AND r.is_active = TRUE
);

-- 인덱스 최적화를 위한 통계 업데이트
ANALYZE TABLE categories;
ANALYZE TABLE places;
ANALYZE TABLE reviews;
ANALYZE TABLE photos;

-- 완료 메시지
SELECT 'Place Service 데이터베이스 초기화가 완료되었습니다.' AS message;
