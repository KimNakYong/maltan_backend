# User Service API Documentation

## 개요
프론트엔드 `https://reserved-jolie-untastily.ngrok-free.app/register`와 연동되는 사용자 관리 서비스입니다.

## 기본 정보
- **서버 주소**: `http://localhost:8081`
- **Context Path**: `/api`
- **전체 API URL**: `http://localhost:8081/api`

## API 엔드포인트

### 1. 회원가입
```
POST /api/users/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "username": "홍길동",
  "phone": "010-1234-5678",
  "preferredRegions": [
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
}
```

**응답 예시:**
```json
{
  "success": true,
  "message": "회원가입이 성공적으로 완료되었습니다.",
  "data": {
    "id": 1,
    "username": "홍길동",
    "email": "user@example.com",
    "name": "홍길동",
    "phone": "010-1234-5678",
    "preferredRegions": [
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
    ],
    "role": "USER",
    "isEnabled": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "timestamp": "2024-01-01T00:00:00",
  "status": 201
}
```

### 2. 사용자 조회
```
GET /api/users/{id}                    # ID로 조회
GET /api/users/username/{username}     # 사용자명으로 조회
GET /api/users/email/{email}           # 이메일로 조회
```

### 3. 중복 검사
```
GET /api/users/check-username/{username}  # 사용자명 중복 검사
GET /api/users/check-email/{email}        # 이메일 중복 검사
```

**응답 예시:**
```json
{
  "success": true,
  "message": "성공",
  "data": {
    "username": "testuser",
    "exists": false
  },
  "timestamp": "2024-01-01T00:00:00",
  "status": 200
}
```

### 4. 사용자 정보 수정
```
PUT /api/users/{id}
Content-Type: application/json

{
  "email": "새이메일",
  "password": "새비밀번호",
  "username": "새사용자명",
  "phone": "새전화번호",
  "preferredRegions": [
    {
      "city": "busan",
      "cityName": "부산광역시",
      "district": "haeundae",
      "districtName": "해운대구",
      "priority": 1
    }
  ]
}
```

### 5. 사용자 상태 변경
```
PATCH /api/users/{id}/status?enabled=true/false
```

### 6. 사용자 삭제
```
DELETE /api/users/{id}
```

### 7. 헬스 체크
```
GET /api/users/health
```

## 테스트 엔드포인트

### 1. 샘플 데이터 생성
```
POST /api/test/create-sample-data
```

### 2. API 정보 조회
```
GET /api/test/api-info
```

## CORS 설정
다음 도메인에서의 요청이 허용됩니다:
- `https://reserved-jolie-untastily.ngrok-free.app`
- `https://*.ngrok-free.app`
- `http://localhost:3000`
- `http://localhost:8080`
- `http://127.0.0.1:3000`

## 프론트엔드 연동 예시

### JavaScript/Fetch 예시
```javascript
// 회원가입
const registerUser = async (userData) => {
  try {
    const response = await fetch('http://localhost:8081/api/users/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: "user@example.com",
        password: "password123",
        username: "홍길동",
        phone: "010-1234-5678",
        preferredRegions: [
          {
            city: "seoul",
            cityName: "서울특별시",
            district: "gangnam",
            districtName: "강남구",
            priority: 1
          },
          {
            city: "seoul",
            cityName: "서울특별시",
            district: "mapo",
            districtName: "마포구",
            priority: 2
          }
        ]
      })
    });
    
    const result = await response.json();
    
    if (result.success) {
      console.log('회원가입 성공:', result.data);
    } else {
      console.error('회원가입 실패:', result.message);
    }
  } catch (error) {
    console.error('네트워크 오류:', error);
  }
};

// 사용자명 중복 검사
const checkUsername = async (username) => {
  try {
    const response = await fetch(`http://localhost:8081/api/users/check-username/${username}`);
    const result = await response.json();
    return result.data.exists;
  } catch (error) {
    console.error('중복 검사 오류:', error);
    return false;
  }
};
```

## 데이터베이스 설정

### MySQL 데이터베이스 생성
```sql
CREATE DATABASE user_service;
```

### application.yml 설정
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_service?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: password
```

## 실행 방법

1. **데이터베이스 설정**
   ```sql
   CREATE DATABASE user_service;
   ```

2. **애플리케이션 실행**
   ```bash
   mvn spring-boot:run
   ```

3. **테스트**
   ```bash
   # 헬스 체크
   curl http://localhost:8081/api/users/health
   
   # 샘플 데이터 생성
   curl -X POST http://localhost:8081/api/test/create-sample-data
   ```

## 에러 응답 형식

```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null,
  "timestamp": "2024-01-01T00:00:00",
  "status": 400
}
```

## 주요 기능

- ✅ 회원가입 (비밀번호 암호화)
- ✅ 사용자 조회 (ID, 사용자명, 이메일)
- ✅ 중복 검사 (사용자명, 이메일)
- ✅ 사용자 정보 수정
- ✅ 사용자 상태 관리
- ✅ 사용자 삭제
- ✅ **선호 지역 관리 (1:N 관계)**
- ✅ **우선순위 기반 지역 선택 (1-3순위)**
- ✅ CORS 설정 (ngrok URL 지원)
- ✅ 표준화된 API 응답 형식
- ✅ 데이터 검증
- ✅ 보안 설정

## 데이터베이스 스키마

### Users 테이블
- id (Primary Key)
- username (Unique)
- email (Unique)
- password (Encrypted)
- name
- phone_number
- role
- is_enabled
- created_at
- updated_at

### Preferred_Regions 테이블
- id (Primary Key)
- user_id (Foreign Key)
- city (도시 코드)
- city_name (도시명)
- district (구/군 코드)
- district_name (구/군명)
- priority (우선순위: 1, 2, 3)
