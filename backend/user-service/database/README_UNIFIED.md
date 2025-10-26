# 통합 사용자 데이터베이스 설정 가이드

## 📋 개요
사용자 정보와 선호 지역을 하나의 테이블에 JSON 형태로 저장하는 통합 데이터베이스 설정 가이드입니다.

## 🗄️ 데이터베이스 구조

### 1. 데이터베이스: `user_service`
- **문자셋**: utf8mb4
- **콜레이션**: utf8mb4_unicode_ci

### 2. 통합 테이블 구조

#### `users` 테이블 (통합 사용자 정보)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 사용자 ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 사용자명 |
| email | VARCHAR(100) | UNIQUE, NOT NULL | 이메일 |
| password | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 |
| name | VARCHAR(100) | NOT NULL | 실명 |
| phone_number | VARCHAR(20) | NULL | 전화번호 |
| **preferred_regions** | **JSON** | **NULL** | **선호지역 정보 (JSON 배열)** |
| role | ENUM('USER','ADMIN') | DEFAULT 'USER' | 사용자 역할 |
| is_enabled | BOOLEAN | DEFAULT TRUE | 계정 활성화 상태 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

### 3. JSON 구조 예시

#### 선호지역 JSON 형태:
```json
[
  {
    "city": "seoul",
    "cityName": "서울특별시",
    "district": "gangnam",
    "districtName": "강남구",
    "priority": 1
  },
  {
    "city": "seoul",
    "cityName": "서울특별시",
    "district": "mapo",
    "districtName": "마포구",
    "priority": 2
  }
]
```

## 🚀 설정 방법

### 방법 1: 자동 설정 (권장)
```bash
# 데이터베이스 디렉토리로 이동
cd database

# 통합 데이터베이스 자동 설정
setup_unified_mysql.bat
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

#### 4. 통합 테이블 생성
```bash
# SQL 스크립트 실행
mysql -u root -p user_service < create_unified_user_service.sql
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

### 선호 지역 JSON 데이터
```json
// 관리자 선호지역
[
  {"city": "seoul", "cityName": "서울특별시", "district": "gangnam", "districtName": "강남구", "priority": 1},
  {"city": "seoul", "cityName": "서울특별시", "district": "mapo", "districtName": "마포구", "priority": 2}
]

// 테스트 사용자 선호지역
[
  {"city": "seoul", "cityName": "서울특별시", "district": "gangnam", "districtName": "강남구", "priority": 1},
  {"city": "busan", "cityName": "부산광역시", "district": "haeundae", "districtName": "해운대구", "priority": 2}
]
```

## 🧪 테스트 방법

### 1. 데이터베이스 연결 확인
```sql
-- MySQL 접속
mysql -u root -p user_service

-- 테이블 확인
SHOW TABLES;

-- 데이터 확인
SELECT id, username, email, JSON_PRETTY(preferred_regions) as preferred_regions FROM users;
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

### 4. JSON 쿼리 예제
```sql
-- 특정 지역을 선호하는 사용자 찾기
SELECT username, name, phone_number
FROM users 
WHERE JSON_SEARCH(preferred_regions, 'one', 'seoul') IS NOT NULL;

-- 첫 번째 선호지역 정보 조회
SELECT 
    username,
    JSON_EXTRACT(preferred_regions, '$[0].cityName') as first_city,
    JSON_EXTRACT(preferred_regions, '$[0].districtName') as first_district,
    JSON_EXTRACT(preferred_regions, '$[0].priority') as first_priority
FROM users;
```

## 🔍 장점

### 1. 단순한 구조
- **하나의 테이블**로 모든 사용자 정보 관리
- **복잡한 JOIN 없이** 빠른 조회
- **스키마 변경이 쉬움**

### 2. 유연한 데이터 저장
- **JSON 형태**로 선호지역 정보 저장
- **동적 필드 추가** 가능
- **MySQL JSON 함수** 활용 가능

### 3. 성능 최적화
- **인덱스 설정**으로 빠른 검색
- **JSON 인덱스** 지원
- **단일 테이블** 조회로 성능 향상

## 🔍 문제 해결

### MySQL 연결 오류
1. MySQL 서비스가 실행 중인지 확인
2. 포트 3306이 사용 가능한지 확인
3. root 비밀번호가 올바른지 확인

### JSON 파싱 오류
1. JSON 형식이 올바른지 확인
2. Jackson 라이브러리 의존성 확인
3. ObjectMapper 설정 확인

## 📝 추가 정보

- **JSON 인덱스**: MySQL 5.7+ 에서 JSON 컬럼 인덱스 지원
- **성능**: 단일 테이블 조회로 JOIN 성능 이슈 해결
- **확장성**: JSON 구조로 새로운 필드 추가 용이
- **호환성**: 기존 API 구조 유지하면서 데이터 저장 방식만 변경
