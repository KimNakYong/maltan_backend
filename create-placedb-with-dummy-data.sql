-- placedb 완전한 스키마 및 더미 데이터 생성 스크립트

-- 데이터베이스 생성 (이미 존재하면 스킵)
CREATE DATABASE IF NOT EXISTS placedb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE placedb;

-- 기존 테이블 삭제 (순서 중요 - 외래키 관계 고려)
DROP TABLE IF EXISTS photos;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS places;
DROP TABLE IF EXISTS categories;

-- 1. 카테고리 테이블 생성
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    icon_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 장소 테이블 생성
CREATE TABLE places (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    address VARCHAR(300) NOT NULL,
    detailed_address VARCHAR(200),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    phone_number VARCHAR(20),
    website VARCHAR(500),
    opening_time TIME,
    closing_time TIME,
    is_open_24h BOOLEAN DEFAULT FALSE,
    closed_days VARCHAR(100),
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    review_count INT DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_category_id (category_id),
    INDEX idx_location (latitude, longitude),
    INDEX idx_rating (average_rating),
    INDEX idx_active (is_active)
);

-- 3. 리뷰 테이블 생성
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    place_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating DECIMAL(2, 1) NOT NULL CHECK (rating >= 1.0 AND rating <= 5.0),
    title VARCHAR(100),
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
    UNIQUE KEY unique_user_place_review (user_id, place_id)
);

-- 4. 사진 테이블 생성
CREATE TABLE photos (
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
    INDEX idx_review_id (review_id)
);

-- ================================
-- 더미 데이터 삽입
-- ================================

-- 1. 카테고리 더미 데이터
INSERT INTO categories (name, description, icon_url, sort_order) VALUES
('음식점', '레스토랑, 카페, 바 등 음식 관련 장소', '/icons/restaurant.svg', 1),
('관광지', '명소, 박물관, 공원 등 관광 명소', '/icons/attraction.svg', 2),
('숙박', '호텔, 펜션, 게스트하우스 등 숙박 시설', '/icons/hotel.svg', 3),
('쇼핑', '쇼핑몰, 시장, 상점 등 쇼핑 관련 장소', '/icons/shopping.svg', 4),
('문화', '극장, 갤러리, 문화센터 등 문화 시설', '/icons/culture.svg', 5),
('스포츠', '체육관, 수영장, 골프장 등 스포츠 시설', '/icons/sports.svg', 6),
('의료', '병원, 약국, 클리닉 등 의료 시설', '/icons/medical.svg', 7),
('교통', '지하철역, 버스터미널, 공항 등 교통 시설', '/icons/transport.svg', 8);

-- 2. 장소 더미 데이터 (서울 지역)
INSERT INTO places (name, description, address, latitude, longitude, phone_number, website, opening_time, closing_time, category_id, average_rating, review_count, view_count) VALUES
-- 음식점 (category_id: 1)
('강남 맛집 한우', '프리미엄 한우 전문점으로 최고급 한우를 제공합니다', '서울특별시 강남구 테헤란로 123', 37.5665, 127.0780, '02-1234-5678', 'https://hanwoo-gangnam.com', '11:00:00', '22:00:00', 1, 4.5, 127, 1520),
('홍대 피자하우스', '수제 피자와 파스타 전문점', '서울특별시 마포구 홍익로 45', 37.5563, 126.9236, '02-2345-6789', 'https://hongdae-pizza.com', '12:00:00', '23:00:00', 1, 4.2, 89, 890),
('명동 전통 칼국수', '50년 전통의 손칼국수 맛집', '서울특별시 중구 명동길 67', 37.5636, 126.9834, '02-3456-7890', NULL, '10:00:00', '21:00:00', 1, 4.7, 203, 2340),
('이태원 브런치 카페', '분위기 좋은 브런치 전문 카페', '서울특별시 용산구 이태원로 89', 37.5344, 126.9947, '02-4567-8901', 'https://itaewon-brunch.com', '08:00:00', '18:00:00', 1, 4.3, 156, 1120),
('잠실 BBQ 하우스', '고급 바비큐 전문점', '서울특별시 송파구 올림픽로 234', 37.5133, 127.1028, '02-5678-9012', NULL, '17:00:00', '24:00:00', 1, 4.4, 98, 760),

-- 관광지 (category_id: 2)
('경복궁', '조선왕조의 정궁으로 한국의 대표적인 궁궐', '서울특별시 종로구 사직로 161', 37.5796, 126.9770, '02-3700-3900', 'https://www.royalpalace.go.kr', '09:00:00', '18:00:00', 2, 4.6, 1520, 15200),
('남산타워', '서울의 랜드마크 타워', '서울특별시 용산구 남산공원길 105', 37.5512, 126.9882, '02-3455-9277', 'https://www.seoultower.co.kr', '10:00:00', '23:00:00', 2, 4.4, 2340, 23400),
('북촌 한옥마을', '전통 한옥이 보존된 역사적 마을', '서울특별시 종로구 계동길 37', 37.5816, 126.9850, NULL, NULL, '00:00:00', '23:59:59', 2, 4.3, 890, 8900),
('동대문디자인플라자', '현대적인 디자인의 복합문화공간', '서울특별시 중구 을지로 281', 37.5665, 127.0092, '02-2153-0000', 'https://www.ddp.or.kr', '10:00:00', '22:00:00', 2, 4.2, 567, 5670),
('한강공원', '서울 시민들의 휴식 공간', '서울특별시 영등포구 여의동로 330', 37.5219, 126.9316, NULL, NULL, '00:00:00', '23:59:59', 2, 4.5, 1234, 12340),

-- 숙박 (category_id: 3)
('강남 럭셔리 호텔', '5성급 럭셔리 호텔', '서울특별시 강남구 테헤란로 456', 37.5048, 127.0280, '02-6789-0123', 'https://gangnam-luxury.com', '00:00:00', '23:59:59', 3, 4.8, 345, 3450),
('명동 비즈니스 호텔', '출장객을 위한 비즈니스 호텔', '서울특별시 중구 명동8길 23', 37.5627, 126.9861, '02-7890-1234', NULL, '00:00:00', '23:59:59', 3, 4.1, 178, 1780),
('홍대 게스트하우스', '젊은 여행객을 위한 게스트하우스', '서울특별시 마포구 와우산로 12', 37.5520, 126.9229, '02-8901-2345', 'https://hongdae-guest.com', '00:00:00', '23:59:59', 3, 4.0, 89, 890),

-- 쇼핑 (category_id: 4)
('강남 쇼핑센터', '대형 쇼핑몰', '서울특별시 강남구 강남대로 789', 37.4979, 127.0276, '02-9012-3456', 'https://gangnam-shopping.com', '10:00:00', '22:00:00', 4, 4.3, 456, 4560),
('명동 쇼핑거리', '관광객들이 즐겨 찾는 쇼핑 명소', '서울특별시 중구 명동2가', 37.5636, 126.9834, NULL, NULL, '10:00:00', '23:00:00', 4, 4.2, 789, 7890),
('동대문 시장', '24시간 운영하는 패션 시장', '서울특별시 중구 장충단로 247', 37.5663, 127.0090, '02-2233-4455', NULL, '00:00:00', '23:59:59', 4, 4.4, 234, 2340),

-- 문화 (category_id: 5)
('국립중앙박물관', '한국의 대표적인 박물관', '서울특별시 용산구 서빙고로 137', 37.5240, 126.9803, '02-2077-9000', 'https://www.museum.go.kr', '10:00:00', '18:00:00', 5, 4.7, 678, 6780),
('세종문화회관', '서울의 대표적인 공연장', '서울특별시 종로구 세종대로 175', 37.5720, 126.9762, '02-399-1000', 'https://www.sejongpac.or.kr', '09:00:00', '22:00:00', 5, 4.5, 345, 3450),

-- 스포츠 (category_id: 6)
('잠실종합운동장', '서울의 대표적인 스포츠 시설', '서울특별시 송파구 올림픽로 25', 37.5133, 127.0719, '02-2240-8800', 'https://www.jamsil.or.kr', '06:00:00', '22:00:00', 6, 4.4, 567, 5670),
('강남 피트니스센터', '최신 시설을 갖춘 헬스장', '서울특별시 강남구 역삼로 123', 37.4996, 127.0354, '02-1111-2222', NULL, '06:00:00', '23:00:00', 6, 4.2, 123, 1230);

-- 3. 리뷰 더미 데이터
INSERT INTO reviews (place_id, user_id, rating, title, content, like_count) VALUES
-- 강남 맛집 한우 리뷰
(1, 1, 5.0, '최고의 한우 맛집!', '정말 맛있는 한우를 먹을 수 있는 곳입니다. 가격은 비싸지만 그만한 가치가 있어요.', 15),
(1, 2, 4.5, '고급스러운 분위기', '분위기도 좋고 서비스도 훌륭합니다. 특별한 날에 가기 좋은 곳이에요.', 8),
(1, 3, 4.0, '가격 대비 괜찮음', '비싸긴 하지만 품질이 좋아서 만족스럽습니다.', 3),

-- 홍대 피자하우스 리뷰
(2, 1, 4.5, '수제 피자 맛집', '직접 만든 도우가 정말 맛있어요. 홍대에서 피자 먹을 때 추천!', 12),
(2, 4, 4.0, '분위기 좋은 곳', '친구들과 가기 좋은 분위기입니다.', 5),

-- 경복궁 리뷰
(6, 2, 5.0, '한국의 아름다운 궁궐', '경복궁은 정말 아름다운 곳입니다. 특히 가을에 방문하면 단풍과 어우러진 모습이 장관입니다.', 45),
(6, 3, 4.5, '역사를 느낄 수 있는 곳', '수문장 교대식도 볼 수 있고, 한복을 입고 가면 무료입장이라 더욱 좋았습니다.', 32),
(6, 5, 4.8, '서울 필수 관광지', '외국인 친구들과 함께 갔는데 모두 만족했습니다.', 28),

-- 남산타워 리뷰
(7, 1, 4.0, '서울 야경 명소', '밤에 가면 서울 야경이 정말 아름답습니다. 다만 사람이 많아서 조금 복잡해요.', 23),
(7, 4, 4.5, '로맨틱한 데이트 코스', '연인과 함께 가기 좋은 곳입니다. 사랑의 자물쇠도 걸 수 있어요.', 18),

-- 강남 럭셔리 호텔 리뷰
(11, 3, 5.0, '최고급 서비스', '정말 완벽한 서비스와 시설입니다. 비싸지만 그만한 가치가 있어요.', 25),
(11, 5, 4.5, '비즈니스 출장 최적', '출장으로 이용했는데 모든 면에서 만족스러웠습니다.', 12);

-- 4. 사진 더미 데이터 (예시)
INSERT INTO photos (place_id, file_name, original_name, file_path, file_size, mime_type, width, height, is_thumbnail, display_order, uploaded_by) VALUES
(1, 'hanwoo_001.jpg', '한우_메인.jpg', '/uploads/places/1/hanwoo_001.jpg', 2048576, 'image/jpeg', 1920, 1080, TRUE, 1, 1),
(1, 'hanwoo_002.jpg', '한우_내부.jpg', '/uploads/places/1/hanwoo_002.jpg', 1536000, 'image/jpeg', 1600, 900, FALSE, 2, 1),
(6, 'gyeongbok_001.jpg', '경복궁_정문.jpg', '/uploads/places/6/gyeongbok_001.jpg', 3072000, 'image/jpeg', 2048, 1536, TRUE, 1, 2),
(6, 'gyeongbok_002.jpg', '경복궁_내부.jpg', '/uploads/places/6/gyeongbok_002.jpg', 2560000, 'image/jpeg', 1920, 1440, FALSE, 2, 2),
(7, 'namsan_001.jpg', '남산타워_야경.jpg', '/uploads/places/7/namsan_001.jpg', 4096000, 'image/jpeg', 2560, 1440, TRUE, 1, 3);

-- 5. 장소별 평균 평점 및 리뷰 수 업데이트
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

-- 6. 인덱스 최적화
ANALYZE TABLE categories;
ANALYZE TABLE places;
ANALYZE TABLE reviews;
ANALYZE TABLE photos;

-- 완료 메시지
SELECT 'placedb 스키마 생성 및 더미 데이터 삽입이 완료되었습니다!' AS message;
SELECT 
    (SELECT COUNT(*) FROM categories) AS 카테고리수,
    (SELECT COUNT(*) FROM places) AS 장소수,
    (SELECT COUNT(*) FROM reviews) AS 리뷰수,
    (SELECT COUNT(*) FROM photos) AS 사진수;

