-- Place Service 데이터베이스 초기화 스크립트

-- place_db 데이터베이스 생성 (이미 존재하지 않는 경우)
SELECT 'CREATE DATABASE place_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'place_db')\gexec

-- place_db 연결
\c place_db;

-- 카테고리 테이블 생성
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(200),
    icon_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 장소 테이블 생성
CREATE TABLE IF NOT EXISTS places (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    address VARCHAR(300) NOT NULL,
    detailed_address VARCHAR(200),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    phone_number VARCHAR(20),
    website VARCHAR(500),
    opening_time TIME,
    closing_time TIME,
    is_open_24h BOOLEAN DEFAULT false,
    closed_days VARCHAR(100),
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    review_count INTEGER DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by BIGINT,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- 리뷰 테이블 생성
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    rating DECIMAL(2,1) NOT NULL,
    content TEXT,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    like_count INTEGER DEFAULT 0,
    place_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (place_id) REFERENCES places(id)
);

-- 사진 테이블 생성
CREATE TABLE IF NOT EXISTS photos (
    id BIGSERIAL PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    is_main BOOLEAN DEFAULT false,
    sort_order INTEGER DEFAULT 0,
    uploaded_by BIGINT,
    place_id BIGINT,
    review_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (place_id) REFERENCES places(id),
    FOREIGN KEY (review_id) REFERENCES reviews(id)
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_places_category_id ON places(category_id);
CREATE INDEX IF NOT EXISTS idx_places_is_active ON places(is_active);
CREATE INDEX IF NOT EXISTS idx_places_created_at ON places(created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_place_id ON reviews(place_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_is_active ON reviews(is_active);
CREATE INDEX IF NOT EXISTS idx_photos_place_id ON photos(place_id);
CREATE INDEX IF NOT EXISTS idx_photos_review_id ON photos(review_id);

-- 기본 카테고리 데이터 삽입
INSERT INTO categories (name, description, sort_order) VALUES
('맛집', '음식점 및 카페', 1),
('관광지', '관광 명소 및 여행지', 2),
('숙박', '호텔, 펜션, 게스트하우스', 3),
('쇼핑', '쇼핑몰, 마트, 전통시장', 4),
('문화시설', '박물관, 미술관, 공연장', 5),
('레저스포츠', '체육시설, 놀이공원, 스포츠센터', 6),
('의료시설', '병원, 약국, 보건소', 7),
('교육시설', '학교, 도서관, 학원', 8)
ON CONFLICT (name) DO NOTHING;

-- 샘플 장소 데이터 삽입
INSERT INTO places (name, description, address, category_id, latitude, longitude) VALUES
('서울타워', '서울의 대표적인 관광명소', '서울특별시 용산구 남산공원길 105', 2, 37.5512, 126.9882),
('명동성당', '한국 천주교의 중심지', '서울특별시 중구 명동길 74', 2, 37.5636, 126.9869),
('경복궁', '조선왕조의 정궁', '서울특별시 종로구 사직로 161', 2, 37.5796, 126.9770)
ON CONFLICT DO NOTHING;
