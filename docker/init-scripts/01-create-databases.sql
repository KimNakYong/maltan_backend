-- 데이터베이스 생성 스크립트
-- PostgreSQL 컨테이너 초기화 시 자동 실행

-- 사용자 데이터베이스
CREATE DATABASE user_db;
GRANT ALL PRIVILEGES ON DATABASE user_db TO maltan_user;

-- 장소 데이터베이스
CREATE DATABASE place_db;
GRANT ALL PRIVILEGES ON DATABASE place_db TO maltan_user;

-- 추천 데이터베이스
CREATE DATABASE recommendation_db;
GRANT ALL PRIVILEGES ON DATABASE recommendation_db TO maltan_user;

-- 커뮤니티 데이터베이스
CREATE DATABASE community_db;
GRANT ALL PRIVILEGES ON DATABASE community_db TO maltan_user;

-- 확장 기능 활성화
\c user_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

\c place_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "postgis";

\c recommendation_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

\c community_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
