#!/bin/bash

# 우분투 MySQL 설치 및 userdb 데이터베이스 생성 스크립트
# 실행 방법: chmod +x ubuntu_mysql_setup.sh && ./ubuntu_mysql_setup.sh

echo "========================================"
echo "우분투 MySQL 설치 및 userdb 데이터베이스 생성"
echo "========================================"
echo

# 1. 시스템 업데이트
echo "[1/8] 시스템 패키지 업데이트 중..."
sudo apt update && sudo apt upgrade -y

# 2. MySQL 서버 설치
echo "[2/8] MySQL 서버 설치 중..."
sudo apt install mysql-server -y

# 3. MySQL 서비스 시작 및 자동 시작 설정
echo "[3/8] MySQL 서비스 시작 중..."
sudo systemctl start mysql
sudo systemctl enable mysql

# 4. MySQL 보안 설정
echo "[4/8] MySQL 보안 설정 중..."
echo "MySQL root 비밀번호를 설정하세요:"
sudo mysql_secure_installation

# 5. MySQL 접속 테스트
echo "[5/8] MySQL 접속 테스트 중..."
sudo mysql -u root -p -e "SELECT VERSION();"

if [ $? -ne 0 ]; then
    echo "MySQL 접속에 실패했습니다. 비밀번호를 확인하세요."
    exit 1
fi

# 6. userdb 데이터베이스 생성
echo "[6/8] userdb 데이터베이스 생성 중..."
sudo mysql -u root -p << EOF
CREATE DATABASE IF NOT EXISTS userdb 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE userdb;

-- 기존 테이블 삭제 (있다면)
DROP TABLE IF EXISTS users;

-- 통합 사용자 테이블 생성
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '사용자명',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '이메일',
    password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    name VARCHAR(100) NOT NULL COMMENT '실명',
    phone_number VARCHAR(20) COMMENT '전화번호',
    preferred_regions JSON COMMENT '선호지역 정보 (JSON 배열)',
    role ENUM('USER', 'ADMIN') DEFAULT 'USER' COMMENT '사용자 역할',
    is_enabled BOOLEAN DEFAULT TRUE COMMENT '계정 활성화 상태',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_created_at (created_at),
    INDEX idx_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='통합 사용자 정보 테이블';

-- 샘플 데이터 삽입
INSERT INTO users (username, email, password, name, phone_number, preferred_regions, role, is_enabled) VALUES
('admin', 'admin@example.com', '\$2a\$10\$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '관리자', '010-0000-0000', 
JSON_ARRAY(
    JSON_OBJECT('city', 'seoul', 'cityName', '서울특별시', 'district', 'gangnam', 'districtName', '강남구', 'priority', 1),
    JSON_OBJECT('city', 'seoul', 'cityName', '서울특별시', 'district', 'mapo', 'districtName', '마포구', 'priority', 2)
), 'ADMIN', TRUE),

('testuser', 'test@example.com', '\$2a\$10\$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '테스트사용자', '010-1234-5678',
JSON_ARRAY(
    JSON_OBJECT('city', 'seoul', 'cityName', '서울특별시', 'district', 'gangnam', 'districtName', '강남구', 'priority', 1),
    JSON_OBJECT('city', 'busan', 'cityName', '부산광역시', 'district', 'haeundae', 'districtName', '해운대구', 'priority', 2)
), 'USER', TRUE);

-- 결과 확인
SELECT '=== 데이터베이스 목록 ===' as info;
SHOW DATABASES;

SELECT '=== userdb 테이블 목록 ===' as info;
SHOW TABLES;

SELECT '=== users 테이블 데이터 ===' as info;
SELECT 
    id, username, email, name, phone_number, 
    JSON_PRETTY(preferred_regions) as preferred_regions,
    role, is_enabled, created_at 
FROM users;

SELECT '=== JSON 쿼리 테스트 ===' as info;
SELECT 
    username,
    JSON_EXTRACT(preferred_regions, '\$[0].cityName') as first_city,
    JSON_EXTRACT(preferred_regions, '\$[0].districtName') as first_district,
    JSON_EXTRACT(preferred_regions, '\$[0].priority') as first_priority
FROM users;
EOF

# 7. MySQL 서비스 상태 확인
echo "[7/8] MySQL 서비스 상태 확인 중..."
sudo systemctl status mysql

# 8. 방화벽 설정 (선택사항)
echo "[8/8] 방화벽 설정 중..."
sudo ufw allow mysql

echo
echo "========================================"
echo "설치 완료!"
echo "========================================"
echo
echo "MySQL 접속 정보:"
echo "- 호스트: localhost"
echo "- 포트: 3306"
echo "- 데이터베이스: userdb"
echo "- 사용자: root"
echo "- 비밀번호: 설정한 비밀번호"
echo
echo "MySQL 접속 명령어:"
echo "mysql -u root -p"
echo
echo "데이터베이스 확인 명령어:"
echo "mysql -u root -p -e \"USE userdb; SHOW TABLES;\""
echo
echo "Spring Boot application.yml 설정:"
echo "spring:"
echo "  datasource:"
echo "    url: jdbc:mysql://localhost:3306/userdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
echo "    username: root"
echo "    password: [설정한 비밀번호]"
echo "    driver-class-name: com.mysql.cj.jdbc.Driver"
echo
