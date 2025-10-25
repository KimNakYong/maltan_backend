# 데이터베이스 설정 가이드

## 📋 개요
User Service를 위한 MySQL 데이터베이스 설정 가이드입니다.

## 🗄️ 데이터베이스 구조

### 주요 테이블
1. **users** - 사용자 기본 정보
2. **preferred_regions** - 사용자 선호 지역 (1:N 관계)
3. **user_sessions** - 사용자 세션 관리 (선택사항)
4. **login_history** - 로그인 이력 (선택사항)

## 🚀 설정 방법

### 1. MySQL 설치 및 실행
```bash
# Windows (Chocolatey)
choco install mysql

# macOS (Homebrew)
brew install mysql
brew services start mysql

# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```

### 2. MySQL 접속
```bash
mysql -u root -p
```

### 3. 데이터베이스 생성
```sql
-- 스키마 파일 실행
source /path/to/01_create_database.sql;

-- 또는 직접 실행
CREATE DATABASE user_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE user_service;
```

### 4. 샘플 데이터 삽입
```sql
-- 샘플 데이터 삽입
source /path/to/02_insert_sample_data.sql;
```

## 🔧 애플리케이션 설정

### application.yml 설정
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_service?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate  # 운영환경에서는 validate 사용
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
```

## 📊 데이터베이스 스키마 상세

### users 테이블
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 사용자 ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 사용자명 |
| email | VARCHAR(100) | UNIQUE, NOT NULL | 이메일 |
| password | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 |
| name | VARCHAR(100) | NOT NULL | 실제 이름 |
| phone_number | VARCHAR(20) | NULL | 전화번호 |
| role | ENUM | NOT NULL, DEFAULT 'USER' | 사용자 역할 |
| is_enabled | BOOLEAN | NOT NULL, DEFAULT TRUE | 계정 활성화 상태 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 수정일시 |

### preferred_regions 테이블
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 지역 ID |
| user_id | BIGINT | FK, NOT NULL | 사용자 ID |
| city | VARCHAR(50) | NOT NULL | 도시 코드 |
| city_name | VARCHAR(100) | NOT NULL | 도시명 |
| district | VARCHAR(50) | NOT NULL | 구/군 코드 |
| district_name | VARCHAR(100) | NOT NULL | 구/군명 |
| priority | INT | NOT NULL, CHECK(1-3) | 우선순위 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 수정일시 |

## 🔍 유용한 쿼리

### 사용자 및 선호 지역 조회
```sql
SELECT 
    u.username,
    u.name,
    u.email,
    pr.city_name,
    pr.district_name,
    pr.priority
FROM users u
LEFT JOIN preferred_regions pr ON u.id = pr.user_id
ORDER BY u.username, pr.priority;
```

### 지역별 사용자 통계
```sql
SELECT 
    pr.city_name,
    pr.district_name,
    COUNT(DISTINCT u.id) as user_count
FROM preferred_regions pr
JOIN users u ON pr.user_id = u.id
GROUP BY pr.city_name, pr.district_name
ORDER BY user_count DESC;
```

### 우선순위별 지역 분포
```sql
SELECT 
    pr.priority,
    pr.city_name,
    pr.district_name,
    COUNT(*) as count
FROM preferred_regions pr
GROUP BY pr.priority, pr.city_name, pr.district_name
ORDER BY pr.priority, count DESC;
```

## 🛠️ 트러블슈팅

### 1. 연결 오류
```
Error: Access denied for user 'root'@'localhost'
```
**해결방법**: MySQL root 비밀번호 확인 및 권한 설정
```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

### 2. 문자 인코딩 문제
```
Error: Incorrect string value
```
**해결방법**: UTF-8 설정 확인
```sql
SHOW VARIABLES LIKE 'character_set%';
SET NAMES utf8mb4;
```

### 3. 외래키 제약조건 오류
```
Error: Cannot add or update a child row
```
**해결방법**: 참조하는 부모 데이터 존재 확인
```sql
SELECT * FROM users WHERE id = ?;
```

## 📈 성능 최적화

### 인덱스 확인
```sql
SHOW INDEX FROM users;
SHOW INDEX FROM preferred_regions;
```

### 쿼리 성능 분석
```sql
EXPLAIN SELECT * FROM users u 
JOIN preferred_regions pr ON u.id = pr.user_id 
WHERE u.is_enabled = TRUE;
```

## 🔒 보안 설정

### 1. 사용자 권한 설정
```sql
-- 애플리케이션 전용 사용자 생성
CREATE USER 'user_service'@'localhost' IDENTIFIED BY 'secure_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON user_service.* TO 'user_service'@'localhost';
FLUSH PRIVILEGES;
```

### 2. 비밀번호 정책
```sql
-- 비밀번호 정책 확인
SHOW VARIABLES LIKE 'validate_password%';
```

## 📝 백업 및 복원

### 백업
```bash
mysqldump -u root -p user_service > user_service_backup.sql
```

### 복원
```bash
mysql -u root -p user_service < user_service_backup.sql
```
