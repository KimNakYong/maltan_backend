#!/bin/bash

# 우분투 MySQL user_service 연결 테스트 스크립트
# 실행 방법: chmod +x ubuntu_test_connection.sh && ./ubuntu_test_connection.sh

echo "========================================"
echo "MySQL user_service 연결 테스트"
echo "========================================"
echo

# 1. MySQL 서비스 상태 확인
echo "[1/6] MySQL 서비스 상태 확인 중..."
if systemctl is-active --quiet mysql; then
    echo "✅ MySQL 서비스가 실행 중입니다."
else
    echo "❌ MySQL 서비스가 실행되지 않았습니다."
    echo "MySQL 서비스를 시작하세요: sudo systemctl start mysql"
    exit 1
fi

# 2. MySQL 접속 테스트
echo "[2/6] MySQL 접속 테스트 중..."
if mysql -u root -p -e "SELECT 1;" 2>/dev/null; then
    echo "✅ MySQL 접속이 성공했습니다."
else
    echo "❌ MySQL 접속에 실패했습니다."
    echo "MySQL 비밀번호를 확인하세요."
    exit 1
fi

# 3. user_service 데이터베이스 확인
echo "[3/6] user_service 데이터베이스 확인 중..."
if mysql -u root -p -e "USE user_service; SELECT 1;" 2>/dev/null; then
    echo "✅ user_service 데이터베이스가 존재합니다."
else
    echo "❌ user_service 데이터베이스가 존재하지 않습니다."
    echo "데이터베이스를 생성하세요: mysql -u root -p < ubuntu_mysql_manual.sql"
    exit 1
fi

# 4. users 테이블 확인
echo "[4/6] users 테이블 확인 중..."
TABLE_EXISTS=$(mysql -u root -p -e "USE user_service; SHOW TABLES LIKE 'users';" 2>/dev/null | grep -c "users")
if [ $TABLE_EXISTS -eq 1 ]; then
    echo "✅ users 테이블이 존재합니다."
else
    echo "❌ users 테이블이 존재하지 않습니다."
    echo "테이블을 생성하세요: mysql -u root -p < ubuntu_mysql_manual.sql"
    exit 1
fi

# 5. 데이터 확인
echo "[5/6] 데이터 확인 중..."
USER_COUNT=$(mysql -u root -p -e "USE user_service; SELECT COUNT(*) FROM users;" 2>/dev/null | tail -n 1)
if [ $USER_COUNT -gt 0 ]; then
    echo "✅ users 테이블에 $USER_COUNT 개의 데이터가 있습니다."
else
    echo "⚠️ users 테이블에 데이터가 없습니다."
fi

# 6. JSON 데이터 확인
echo "[6/6] JSON 데이터 확인 중..."
JSON_DATA=$(mysql -u root -p -e "USE user_service; SELECT JSON_EXTRACT(preferred_regions, '\$[0].cityName') FROM users LIMIT 1;" 2>/dev/null | tail -n 1)
if [ ! -z "$JSON_DATA" ] && [ "$JSON_DATA" != "NULL" ]; then
    echo "✅ JSON 데이터가 정상적으로 저장되어 있습니다."
else
    echo "⚠️ JSON 데이터에 문제가 있을 수 있습니다."
fi

echo
echo "========================================"
echo "연결 테스트 완료!"
echo "========================================"
echo
echo "MySQL 연결 정보:"
echo "- 호스트: localhost"
echo "- 포트: 3306"
echo "- 데이터베이스: user_service"
echo "- 사용자: root"
echo
echo "Spring Boot application.yml 설정:"
echo "spring:"
echo "  datasource:"
echo "    url: jdbc:mysql://localhost:3306/user_service?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
echo "    username: root"
echo "    password: [MySQL 비밀번호]"
echo "    driver-class-name: com.mysql.cj.jdbc.Driver"
echo
echo "서버 실행 명령어:"
echo "mvn spring-boot:run"
echo
echo "API 테스트 명령어:"
echo "curl http://localhost:8081/api/users/health"
