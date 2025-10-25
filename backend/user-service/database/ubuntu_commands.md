# 우분투 MySQL 설치 및 userdb 데이터베이스 생성 명령어

## 🚀 자동 설치 (권장)

### 1. 스크립트 실행 권한 부여
```bash
chmod +x ubuntu_mysql_setup.sh
```

### 2. 자동 설치 실행
```bash
./ubuntu_mysql_setup.sh
```

## 🔧 수동 설치

### 1. 시스템 업데이트
```bash
sudo apt update && sudo apt upgrade -y
```

### 2. MySQL 서버 설치
```bash
sudo apt install mysql-server -y
```

### 3. MySQL 서비스 시작
```bash
sudo systemctl start mysql
sudo systemctl enable mysql
```

### 4. MySQL 보안 설정
```bash
sudo mysql_secure_installation
```

### 5. MySQL 접속 테스트
```bash
sudo mysql -u root -p
```

### 6. 데이터베이스 생성 (MySQL 접속 후)
```sql
-- 데이터베이스 생성
CREATE DATABASE userdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 데이터베이스 사용
USE userdb;

-- 테이블 생성
source ubuntu_mysql_manual.sql;
```

### 7. 또는 SQL 파일 직접 실행
```bash
mysql -u root -p < ubuntu_mysql_manual.sql
```

## 🔍 데이터베이스 확인

### 1. MySQL 접속
```bash
mysql -u root -p
```

### 2. 데이터베이스 목록 확인
```sql
SHOW DATABASES;
```

### 3. userdb 사용
```sql
USE userdb;
```

### 4. 테이블 목록 확인
```sql
SHOW TABLES;
```

### 5. users 테이블 구조 확인
```sql
DESCRIBE users;
```

### 6. 데이터 확인
```sql
SELECT * FROM users;
```

### 7. JSON 데이터 확인
```sql
SELECT username, JSON_PRETTY(preferred_regions) FROM users;
```

## 🧪 테스트 쿼리

### 1. 특정 지역을 선호하는 사용자 찾기
```sql
SELECT username, name, phone_number
FROM users 
WHERE JSON_SEARCH(preferred_regions, 'one', 'seoul') IS NOT NULL;
```

### 2. 우선순위 1인 지역 조회
```sql
SELECT 
    username,
    JSON_EXTRACT(preferred_regions, '$[0].cityName') as city,
    JSON_EXTRACT(preferred_regions, '$[0].districtName') as district
FROM users 
WHERE JSON_EXTRACT(preferred_regions, '$[0].priority') = 1;
```

### 3. JSON 배열 길이 확인
```sql
SELECT 
    username,
    JSON_LENGTH(preferred_regions) as region_count
FROM users;
```

## 🔧 Spring Boot 설정

### 1. application.yml 설정
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/userdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: your_mysql_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 2. 서버 실행
```bash
mvn spring-boot:run
```

### 3. API 테스트
```bash
curl http://localhost:8081/api/users/health
```

## 🛠️ 문제 해결

### 1. MySQL 접속 오류
```bash
# MySQL 서비스 상태 확인
sudo systemctl status mysql

# MySQL 서비스 재시작
sudo systemctl restart mysql

# MySQL 로그 확인
sudo tail -f /var/log/mysql/error.log
```

### 2. 권한 오류
```bash
# MySQL 사용자 권한 확인
sudo mysql -u root -p -e "SELECT user, host FROM mysql.user;"

# 새 사용자 생성 (선택사항)
sudo mysql -u root -p -e "CREATE USER 'userdb'@'localhost' IDENTIFIED BY 'password';"
sudo mysql -u root -p -e "GRANT ALL PRIVILEGES ON userdb.* TO 'userdb'@'localhost';"
```

### 3. 방화벽 설정
```bash
# MySQL 포트 열기
sudo ufw allow 3306

# 방화벽 상태 확인
sudo ufw status
```

## 📊 데이터베이스 정보

- **데이터베이스명**: userdb
- **테이블명**: users
- **문자셋**: utf8mb4
- **콜레이션**: utf8mb4_unicode_ci
- **엔진**: InnoDB
- **포트**: 3306
- **호스트**: localhost

## 🎯 완료 확인

1. ✅ MySQL 설치 완료
2. ✅ userdb 데이터베이스 생성
3. ✅ users 테이블 생성
4. ✅ 샘플 데이터 삽입
5. ✅ Spring Boot 연결 설정
6. ✅ API 테스트 성공
