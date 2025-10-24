# User Service

마이크로서비스 아키텍처를 위한 사용자 관리 서비스입니다.

## 기능

- 사용자 등록 (회원가입)
- 사용자 조회 (ID, 사용자명, 이메일로 조회)
- 사용자 정보 수정
- 사용자 상태 관리 (활성화/비활성화)
- 사용자 삭제
- 중복 검사 (사용자명, 이메일)

## API 엔드포인트

### 사용자 등록
```
POST /api/users/register
Content-Type: application/json

{
  "username": "사용자명",
  "email": "이메일",
  "password": "비밀번호",
  "name": "이름",
  "phoneNumber": "전화번호"
}
```

### 사용자 조회
```
GET /api/users/{id}                    # ID로 조회
GET /api/users/username/{username}     # 사용자명으로 조회
GET /api/users/email/{email}           # 이메일로 조회
```

### 중복 검사
```
GET /api/users/check-username/{username}  # 사용자명 중복 검사
GET /api/users/check-email/{email}        # 이메일 중복 검사
```

### 사용자 정보 수정
```
PUT /api/users/{id}
Content-Type: application/json

{
  "username": "새사용자명",
  "email": "새이메일",
  "password": "새비밀번호",
  "name": "새이름",
  "phoneNumber": "새전화번호"
}
```

### 사용자 상태 변경
```
PATCH /api/users/{id}/status?enabled=true/false
```

### 사용자 삭제
```
DELETE /api/users/{id}
```

### 헬스 체크
```
GET /api/users/health
```

## 실행 방법

1. MySQL 데이터베이스 설정
2. application.yml에서 데이터베이스 연결 정보 수정
3. 애플리케이션 실행:
   ```bash
   mvn spring-boot:run
   ```

## 데이터베이스 설정

MySQL 데이터베이스를 사용하며, 다음과 같은 설정이 필요합니다:

```sql
CREATE DATABASE user_service;
```

## 보안

- Spring Security를 사용한 인증/인가
- BCrypt를 사용한 비밀번호 암호화
- CORS 설정으로 크로스 오리진 요청 허용

## 기술 스택

- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- MySQL 8.0
- Maven
- Java 17