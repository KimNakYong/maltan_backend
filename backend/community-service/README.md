# Community Service

커뮤니티 서비스 - 게시글, 댓글, 추천/비추천, 소모임 모집 기능

## 🚀 기능

### 1. 게시글 관리
- 게시글 CRUD
- 카테고리별 분류 (자유, 질문, 정보, 모임, 봉사, 운동, 취미)
- 지역별 필터링 (시/도 → 시/군/구 → 읍/면/동)
- 이미지 첨부 (최대 5개)

### 2. 댓글 시스템
- 댓글 작성, 수정, 삭제
- 대댓글 지원 (1depth)
- 댓글 추천/비추천

### 3. 추천/비추천
- 게시글 추천/비추천
- 댓글 추천/비추천
- 중복 방지 (사용자당 1회)

### 4. 소모임/활동 인원 모집 ⭐
- 모집 게시글 생성 (날짜, 시간, 장소, 인원)
- 참여 신청/취소 (토글 방식)
- 실시간 모집 현황
- 마감 시간 자동 처리
- 참여자 목록 조회

## 📦 기술 스택

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Data JPA**: PostgreSQL 연동
- **Spring Security**: JWT 인증
- **PostgreSQL**: 15+
- **Redis**: 캐싱 및 분산 락
- **Maven**: 빌드 도구

## 🗄️ 데이터베이스

### 연결 정보
- Host: `10.0.2.15` (Ubuntu 서버)
- Port: `5432`
- Database: `community_db`
- User: `community_user`

### 테이블 구조
- `posts` - 게시글
- `post_images` - 게시글 이미지
- `comments` - 댓글
- `post_votes` - 게시글 추천/비추천
- `comment_votes` - 댓글 추천/비추천
- `recruitment_participants` - 모집 참여자

## 🛠️ 로컬 개발 환경 설정

### 1. 환경 변수 설정

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=community_db
export DB_USERNAME=community_user
export DB_PASSWORD=Community@2025!
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

### 2. 애플리케이션 실행

```bash
mvn spring-boot:run
```

### 3. Docker로 실행

```bash
# 빌드
docker build -t community-service:latest .

# 실행
docker run -d \
  --name community-service \
  -p 8083:8083 \
  -e DB_HOST=10.0.2.15 \
  -e DB_PORT=5432 \
  -e DB_NAME=community_db \
  -e DB_USERNAME=community_user \
  -e DB_PASSWORD=Community@2025! \
  -e REDIS_HOST=10.0.2.15 \
  community-service:latest
```

## 📡 API 엔드포인트

### 게시글 API
```
GET    /api/community/posts              # 게시글 목록
GET    /api/community/posts/{id}         # 게시글 상세
POST   /api/community/posts              # 게시글 작성
PUT    /api/community/posts/{id}         # 게시글 수정
DELETE /api/community/posts/{id}         # 게시글 삭제
```

### 댓글 API
```
GET    /api/community/posts/{postId}/comments              # 댓글 목록
POST   /api/community/posts/{postId}/comments              # 댓글 작성
PUT    /api/community/comments/{id}                        # 댓글 수정
DELETE /api/community/comments/{id}                        # 댓글 삭제
```

### 추천/비추천 API
```
POST   /api/community/posts/{postId}/vote                  # 게시글 투표
DELETE /api/community/posts/{postId}/vote                  # 게시글 투표 취소
POST   /api/community/comments/{commentId}/vote            # 댓글 투표
DELETE /api/community/comments/{commentId}/vote            # 댓글 투표 취소
```

### 모집 참여 API
```
POST   /api/community/posts/{postId}/participate           # 참여/취소 토글
GET    /api/community/posts/{postId}/participants          # 참여자 목록
```

## 🧪 테스트

```bash
mvn test
```

## 📊 모니터링

### Actuator 엔드포인트
- Health: `http://localhost:8083/actuator/health`
- Metrics: `http://localhost:8083/actuator/metrics`
- Prometheus: `http://localhost:8083/actuator/prometheus`

## 📝 상세 문서

- [커뮤니티 서비스 설계 문서](../../docs/COMMUNITY_SERVICE_DESIGN.md)
- [PostgreSQL 설치 가이드](../../docs/POSTGRESQL_SETUP_GUIDE.md)

## 👥 작성자

Maltan Project Team

## 🚀 CI/CD

GitHub Actions를 통한 자동 배포가 설정되어 있습니다.
- `backend/community-service/**` 경로의 변경사항이 `main` 브랜치에 push되면 자동으로 배포됩니다.
- Self-Hosted Runner가 Ubuntu 서버에서 직접 빌드 및 배포를 수행합니다.

