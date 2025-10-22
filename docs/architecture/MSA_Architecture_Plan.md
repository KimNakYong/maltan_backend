# 우리동네 소개 서비스 MSA 기획서

## 1. 서비스 개요

### 1.1 서비스 목적
- 사용자의 주소 기반으로 주변 맛집, 관광지, 가볼만한 곳 추천
- 지역 커뮤니티 활성화 및 지역 정보 공유
- 개인화된 지역 정보 제공

### 1.2 핵심 기능
- 사용자 주소 기반 주변 장소 추천
- 맛집, 카페, 관광지, 문화시설 정보 제공
- 리뷰 및 평점 시스템
- 지역 커뮤니티 게시판
- 실시간 위치 기반 알림

## 2. MSA 아키텍처 설계 (2명 개발팀 최적화)

### 2.1 마이크로서비스 구성 (5개 서비스로 통합)

#### 2.1.1 User Service (사용자 + 인증 관리)
- **역할**: 사용자 인증, 회원가입, 프로필, 주소 관리
- **기능**:
  - JWT 기반 인증/인가
  - 사용자 프로필 CRUD
  - 주소 정보 관리 및 좌표 변환
  - 관심 지역 설정
  - 주소 → 좌표 변환 (카카오/네이버 지도 API)

#### 2.1.2 Place Service (장소 + 리뷰 관리)
- **역할**: 맛집, 관광지 정보 + 리뷰/평점 시스템
- **기능**:
  - 장소 정보 CRUD
  - 카테고리별 분류
  - 장소 상세 정보 (영업시간, 연락처)
  - 리뷰 작성/수정/삭제
  - 평점 시스템 및 사진 업로드
  - 리뷰 통계

#### 2.1.3 Recommendation Service (추천 + 위치 관리)
- **역할**: 개인화 추천 + 위치 기반 검색
- **기능**:
  - 사용자 선호도 기반 추천
  - 거리 기반 추천
  - 반경 내 장소 검색
  - 위치 기반 필터링
  - 인기 장소 추천
  - 지리적 거리 계산

#### 2.1.4 Community Service (커뮤니티 + 알림)
- **역할**: 지역 커뮤니티 + 알림 시스템
- **기능**:
  - 게시글 CRUD
  - 댓글 시스템
  - 좋아요/북마크
  - 지역별 게시판
  - 실시간 알림 (위치 기반, 리뷰, 커뮤니티 활동)
  - 푸시 알림

#### 2.1.5 Gateway Service (API 게이트웨이)
- **역할**: 요청 라우팅, 인증, 로드밸런싱
- **기능**:
  - API 라우팅
  - 인증 토큰 검증
  - 요청/응답 로깅
  - Rate Limiting

## 3. 기술 스택

### 3.1 Backend
- **언어**: Java 17 (Spring Boot 3.x)
- **프레임워크**: Spring Cloud Gateway, Spring Security
- **데이터베이스**: 
  - PostgreSQL (메인 데이터)
  - Redis (캐싱, 세션)
  - MongoDB (리뷰, 로그)
- **메시지 큐**: Apache Kafka
- **서비스 디스커버리**: Eureka Server
- **설정 관리**: Spring Cloud Config

### 3.2 Frontend
- **프레임워크**: React 18 + TypeScript
- **상태관리**: Redux Toolkit
- **UI 라이브러리**: Material-UI 또는 Ant Design
- **지도**: 카카오맵 API
- **빌드**: Vite

### 3.3 Infrastructure
- **컨테이너**: Docker + Docker Compose
- **오케스트레이션**: Kubernetes (선택사항)
- **모니터링**: Prometheus + Grafana
- **로깅**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **CI/CD**: GitHub Actions

## 4. 데이터베이스 설계

### 4.1 User Service DB
```sql
-- users 테이블
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    profile_image VARCHAR(500),
    address VARCHAR(500),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4.2 Place Service DB (장소 + 리뷰 통합)
```sql
-- places 테이블
CREATE TABLE places (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    address VARCHAR(500) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    phone VARCHAR(20),
    business_hours JSONB,
    description TEXT,
    average_rating DECIMAL(3, 2) DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- reviews 테이블 (PostgreSQL로 통합)
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    place_id BIGINT REFERENCES places(id),
    user_id BIGINT REFERENCES users(id),
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    content TEXT,
    images JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4.3 Community Service DB
```sql
-- posts 테이블
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100),
    location VARCHAR(500),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    like_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- comments 테이블
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT REFERENCES posts(id),
    user_id BIGINT REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 5. API 설계

### 5.1 주요 API 엔드포인트

#### User Service
- `POST /api/users/register` - 회원가입
- `POST /api/users/login` - 로그인
- `GET /api/users/profile` - 프로필 조회
- `PUT /api/users/profile` - 프로필 수정

#### Place Service
- `GET /api/places/nearby` - 주변 장소 검색
- `GET /api/places/{id}` - 장소 상세 정보
- `GET /api/places/category/{category}` - 카테고리별 장소

#### Place Service (리뷰 포함)
- `POST /api/places/{id}/reviews` - 리뷰 작성
- `GET /api/places/{id}/reviews` - 장소별 리뷰 조회
- `PUT /api/reviews/{id}` - 리뷰 수정
- `DELETE /api/reviews/{id}` - 리뷰 삭제

#### Recommendation Service
- `GET /api/recommendations/personal` - 개인화 추천
- `GET /api/recommendations/popular` - 인기 장소
- `GET /api/recommendations/nearby` - 주변 추천

#### Community Service
- `GET /api/community/posts` - 게시글 목록
- `POST /api/community/posts` - 게시글 작성
- `GET /api/community/posts/{id}` - 게시글 상세
- `POST /api/community/posts/{id}/comments` - 댓글 작성
- `GET /api/community/posts/{id}/comments` - 댓글 목록

## 6. 개발 단계별 계획 (2명 개발팀)

### 개발자 역할 분담
- **개발자 A**: User Service + Place Service + Gateway Service
- **개발자 B**: Recommendation Service + Community Service + Frontend

### Phase 1: 기본 인프라 구축 (1주)
1. Docker 환경 설정
2. 기본 MSA 구조 설정
3. API Gateway 구성
4. 데이터베이스 설계 및 구축

### Phase 2: 핵심 서비스 개발 (3주)
**개발자 A**:
- User Service 개발 (인증, 프로필, 주소 관리)
- Place Service 개발 (장소 정보, 리뷰 시스템)

**개발자 B**:
- Recommendation Service 개발 (추천 알고리즘, 위치 검색)
- 기본 API 연동

### Phase 3: 고급 기능 개발 (2주)
**개발자 A**:
- Place Service 고도화 (리뷰 통계, 사진 업로드)
- Gateway Service 보안 강화

**개발자 B**:
- Community Service 개발 (게시판, 댓글)
- 알림 시스템 개발

### Phase 4: 프론트엔드 개발 (3주)
**개발자 B**:
- React 앱 기본 구조
- 지도 API 연동
- 사용자 인터페이스 구현
- 반응형 디자인

**개발자 A**:
- Backend API 최적화
- 데이터베이스 성능 튜닝

### Phase 5: 통합 및 최적화 (1주)
1. 서비스 간 통합 테스트
2. 성능 최적화
3. 보안 강화
4. 모니터링 설정

## 7. 배포 및 운영

### 7.1 개발 환경
- VirtualBox Ubuntu 22.04 LTS
- Docker Desktop
- 로컬 개발 서버

### 7.2 프로덕션 환경
- AWS EC2 또는 GCP
- Kubernetes 클러스터
- 로드밸런서 (AWS ALB)
- CDN (CloudFront)

## 8. 보안 고려사항

### 8.1 인증/인가
- JWT 토큰 기반 인증
- OAuth 2.0 소셜 로그인
- API 키 관리

### 8.2 데이터 보안
- HTTPS 통신
- 데이터 암호화
- 개인정보 보호 (GDPR 준수)

## 9. 모니터링 및 로깅

### 9.1 모니터링
- 서비스 헬스체크
- 성능 메트릭 수집
- 알람 설정

### 9.2 로깅
- 구조화된 로그 (JSON)
- 중앙집중식 로그 관리
- 로그 분석 및 대시보드

## 10. 확장성 고려사항

### 10.1 수평적 확장
- 서비스별 독립적 스케일링
- 로드밸런싱
- 캐싱 전략

### 10.2 데이터 확장
- 데이터베이스 샤딩
- 읽기 전용 복제본
- CDN 활용

이 기획서를 바탕으로 단계별로 개발을 진행하시면 됩니다. 각 단계별로 구체적인 구현 방법이나 추가 질문이 있으시면 언제든 말씀해 주세요!
