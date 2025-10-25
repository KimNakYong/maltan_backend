#!/bin/bash

# Place Service용 MySQL 데이터베이스 설정 스크립트

echo "=== Place Service MySQL 데이터베이스 설정 시작 ==="

# MySQL 서비스 상태 확인
echo "1. MySQL 서비스 상태 확인..."
sudo systemctl status mysql --no-pager

# MySQL 접속하여 placedb 생성
echo "2. placedb 데이터베이스 생성..."
mysql -u root -p1234 << 'EOF'
-- placedb 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS placedb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- placedb 사용
USE placedb;

-- 권한 확인
SHOW GRANTS FOR 'root'@'%';

-- 데이터베이스 목록 확인
SHOW DATABASES;

-- placedb 선택 및 테이블 확인
USE placedb;
SHOW TABLES;

SELECT 'placedb 데이터베이스가 성공적으로 생성되었습니다.' AS message;
EOF

echo "3. 초기화 스크립트 실행..."
mysql -u root -p1234 placedb < docker/init-scripts/03-create-place-db.sql

echo "4. 생성된 테이블 확인..."
mysql -u root -p1234 -e "USE placedb; SHOW TABLES; SELECT COUNT(*) as category_count FROM categories; SELECT COUNT(*) as place_count FROM places;"

echo "=== Place Service MySQL 데이터베이스 설정 완료 ==="
echo ""
echo "🎉 placedb 데이터베이스가 성공적으로 생성되었습니다!"
echo ""
echo "📋 생성된 테이블:"
echo "   - categories (카테고리)"
echo "   - places (장소)"
echo "   - reviews (리뷰)"
echo "   - photos (사진)"
echo ""
echo "🔧 연결 정보:"
echo "   - 데이터베이스: placedb"
echo "   - 호스트: localhost (또는 10.0.2.15)"
echo "   - 포트: 3306"
echo "   - 사용자: root"
echo "   - 비밀번호: 1234"
echo ""
echo "✅ place-service가 이제 MySQL placedb에 연결할 수 있습니다!"
