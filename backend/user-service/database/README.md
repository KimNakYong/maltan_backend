# MySQL 데이터베이스 설정 가이드

## 📋 개요
회원가입 시스템을 위한 MySQL 데이터베이스 `user_service` 설정 가이드입니다.

## 🗄️ 데이터베이스 구조

### 1. 데이터베이스: `user_service`
- **문자셋**: utf8mb4
- **콜레이션**: utf8mb4_unicode_ci

### 2. 테이블 구조

#### `users` 테이블 (사용자 정보)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 사용자 ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 사용자명 |
| email | VARCHAR(100) | UNIQUE, NOT NULL | 이메일 |
| password | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 |
| name | VARCHAR(100) | NOT NULL | 실명 |
| phone_number | VARCHAR(20) | NULL | 전화번호 |
| role | ENUM('USER','ADMIN') | DEFAULT 'USER' | 사용자 역할 |
| is_enabled | BOOLEAN | DEFAULT TRUE | 계정 활성화 상태 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

#### `preferred_regions` 테이블 (선호 지역)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 지역 ID |
| user_id | BIGINT | FOREIGN KEY, NOT NULL | 사용자 ID |
| city | VARCHAR(50) | NOT NULL | 도시 코드 |
| city_name | VARCHAR(100) | NOT NULL | 도시명 |
| district | VARCHAR(50) | NOT NULL | 구/군 코드 |
| district_name | VARCHAR(100) | NOT NULL | 구/군명 |
| priority | INT | CHECK (1-3), NOT NULL | 우선순위 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |

## 🚀 설정 방법

### 방법 1: 자동 설정 (권장)
```bash
# 데이터베이스 디렉토리로 이동
cd database

# 자동 설정 스크립트 실행
setup_mysql.bat
```

### 방법 2: 수동 설정

#### 1. MySQL 설치
- [MySQL 공식 사이트](https://dev.mysql.com/downloads/mysql/)에서 다운로드
- 또는 [XAMPP](https://www.apachefriends.org/) 사용

#### 2. MySQL 서비스 시작
```bash
# Windows 서비스에서 MySQL 시작
# 또는 XAMPP Control Panel에서 MySQL 시작
```

#### 3. 데이터베이스 생성
```sql
-- MySQL에 접속
mysql -u root -p

-- 데이터베이스 생성
CREATE DATABASE user_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 4. 테이블 생성
```bash
# SQL 스크립트 실행
mysql -u root -p user_service < create_user_service.sql
```

## 🔧 Spring Boot 설정

### application.yml 설정
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_service?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
    username: root
    password: # MySQL root 비밀번호
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
```

## 📊 샘플 데이터

### 사용자 데이터
- **관리자**: admin@example.com / admin
- **테스트 사용자**: test@example.com / test

### 선호 지역 데이터
- 관리자: 서울 강남구(1순위), 서울 마포구(2순위)
- 테스트 사용자: 서울 강남구(1순위), 부산 해운대구(2순위)

## 🧪 테스트 방법

### 1. 데이터베이스 연결 확인
```sql
-- MySQL 접속
mysql -u root -p user_service

-- 테이블 확인
SHOW TABLES;

-- 데이터 확인
SELECT * FROM users;
SELECT * FROM preferred_regions;
```

### 2. Spring Boot 서버 실행
```bash
# 서버 실행
mvn spring-boot:run

# API 테스트
curl http://localhost:8081/api/users/health
```

### 3. 회원가입 API 테스트
```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "password123",
    "username": "새사용자",
    "phone": "010-9999-9999",
    "preferredRegions": [
      {
        "city": "seoul",
        "cityName": "서울특별시",
        "district": "gangnam",
        "districtName": "강남구",
        "priority": 1
      }
    ]
  }'
```

## 🔍 문제 해결

### MySQL 연결 오류
1. MySQL 서비스가 실행 중인지 확인
2. 포트 3306이 사용 가능한지 확인
3. root 비밀번호가 올바른지 확인

### 테이블 생성 오류
1. 데이터베이스가 존재하는지 확인
2. 사용자 권한이 충분한지 확인
3. SQL 스크립트 문법 오류 확인

## 📝 추가 정보

- **인덱스**: 성능 최적화를 위한 인덱스 설정 완료
- **외래키**: 데이터 무결성을 위한 외래키 제약조건 설정
- **체크 제약**: 우선순위는 1-3 범위로 제한
- **유니크 제약**: 사용자별 지역-우선순위 조합 중복 방지