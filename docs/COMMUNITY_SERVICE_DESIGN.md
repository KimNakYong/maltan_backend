# 커뮤니티 서비스 설계 문서

## 📋 개요

지역 기반 커뮤니티 서비스로, 게시글 작성, 댓글, 추천/비추천, 그리고 **소모임/활동 인원 모집 기능**을 제공합니다.

---

## 🎯 주요 기능

### 1. 게시글 CRUD
- 게시글 작성, 조회, 수정, 삭제
- 이미지 첨부 (최대 5개)
- 지역별 필터링
- 카테고리별 분류

### 2. 댓글 시스템
- 댓글 작성, 수정, 삭제
- 대댓글 지원 (최대 1depth)
- 댓글 추천/비추천

### 3. 추천/비추천
- 게시글 추천/비추천
- 중복 방지 (사용자당 1회)
- 실시간 카운트 업데이트

### 4. 소모임/활동 인원 모집 ⭐
- 모집 게시글 생성 (날짜, 시간, 장소, 인원)
- 참여 신청/취소 (토글 방식)
- 실시간 모집 현황 표시 (예: 7/10)
- 마감 시간 자동 처리
- 참여자 목록 조회

### 5. 지역별 카테고리
- 시/도 → 시/군/구 → 읍/면/동 계층 구조
- 카테고리: 자유, 질문, 정보, 모임, 봉사, 운동, 취미 등

---

## 🗄️ 데이터베이스 스키마

### 배포 전략
- **개발 환경**: Docker 외부 PostgreSQL 사용
- **서비스별 독립성**: `community_service` 스키마 분리
- **위치**: Ubuntu 서버에 PostgreSQL 직접 설치

### ERD 구조

```sql
-- 게시글 테이블
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,  -- User Service의 사용자 ID
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,  -- 자유, 질문, 정보, 모임, 봉사, 운동, 취미
    
    -- 지역 정보
    region_si VARCHAR(50) NOT NULL,      -- 시/도
    region_gu VARCHAR(50),                -- 시/군/구
    region_dong VARCHAR(50),              -- 읍/면/동
    
    -- 모집 정보 (모임 게시글인 경우)
    is_recruitment BOOLEAN DEFAULT FALSE,
    recruitment_max INTEGER,              -- 최대 인원
    recruitment_current INTEGER DEFAULT 0, -- 현재 인원
    recruitment_deadline TIMESTAMP,       -- 모집 마감일
    event_date TIMESTAMP,                 -- 활동 일시
    event_location VARCHAR(200),          -- 활동 장소
    
    -- 통계
    view_count INTEGER DEFAULT 0,
    like_count INTEGER DEFAULT 0,
    dislike_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    
    -- 상태
    is_deleted BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'ACTIVE',  -- ACTIVE, CLOSED, DELETED
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    INDEX idx_region (region_si, region_gu, region_dong),
    INDEX idx_recruitment (is_recruitment, recruitment_deadline),
    INDEX idx_created_at (created_at DESC)
);

-- 게시글 이미지 테이블
CREATE TABLE post_images (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    image_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id)
);

-- 댓글 테이블
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_comment_id BIGINT,  -- 대댓글용 (NULL이면 일반 댓글)
    content TEXT NOT NULL,
    
    like_count INTEGER DEFAULT 0,
    dislike_count INTEGER DEFAULT 0,
    
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_comment_id (parent_comment_id)
);

-- 게시글 추천/비추천 테이블
CREATE TABLE post_votes (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    vote_type VARCHAR(10) NOT NULL,  -- LIKE, DISLIKE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    UNIQUE (post_id, user_id),  -- 사용자당 1회만
    INDEX idx_post_user (post_id, user_id)
);

-- 댓글 추천/비추천 테이블
CREATE TABLE comment_votes (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    vote_type VARCHAR(10) NOT NULL,  -- LIKE, DISLIKE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    UNIQUE (comment_id, user_id),
    INDEX idx_comment_user (comment_id, user_id)
);

-- 모집 참여자 테이블 ⭐
CREATE TABLE recruitment_participants (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'JOINED',  -- JOINED, CANCELLED
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    UNIQUE (post_id, user_id),  -- 사용자당 1회만 참여
    INDEX idx_post_status (post_id, status),
    INDEX idx_user_id (user_id)
);
```

---

## 🏗️ API 설계

### 1. 게시글 API

#### 게시글 목록 조회
```
GET /api/community/posts
Query Parameters:
  - category: string (optional)
  - region_si: string (optional)
  - region_gu: string (optional)
  - region_dong: string (optional)
  - is_recruitment: boolean (optional)
  - page: integer (default: 0)
  - size: integer (default: 20)
  - sort: string (default: "createdAt,desc")

Response:
{
  "content": [
    {
      "id": 1,
      "userId": 123,
      "userNickname": "홍길동",
      "title": "12월 25일 종로구 봉사활동 10명 모집",
      "content": "...",
      "category": "봉사",
      "regionSi": "서울특별시",
      "regionGu": "종로구",
      "regionDong": "혜화동",
      "isRecruitment": true,
      "recruitmentMax": 10,
      "recruitmentCurrent": 7,
      "recruitmentDeadline": "2025-12-24T23:59:59",
      "eventDate": "2025-12-25T10:00:00",
      "eventLocation": "혜화역 2번 출구",
      "viewCount": 150,
      "likeCount": 25,
      "dislikeCount": 2,
      "commentCount": 8,
      "status": "ACTIVE",
      "createdAt": "2025-12-20T10:00:00",
      "images": [
        "https://...",
        "https://..."
      ]
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "currentPage": 0
}
```

#### 게시글 상세 조회
```
GET /api/community/posts/{postId}

Response:
{
  "id": 1,
  "userId": 123,
  "userNickname": "홍길동",
  "userProfileImage": "https://...",
  "title": "12월 25일 종로구 봉사활동 10명 모집",
  "content": "어려운 이웃들에게 도시락을 전달하는 봉사활동입니다...",
  "category": "봉사",
  "regionSi": "서울특별시",
  "regionGu": "종로구",
  "regionDong": "혜화동",
  "isRecruitment": true,
  "recruitmentMax": 10,
  "recruitmentCurrent": 7,
  "recruitmentDeadline": "2025-12-24T23:59:59",
  "eventDate": "2025-12-25T10:00:00",
  "eventLocation": "혜화역 2번 출구",
  "viewCount": 151,
  "likeCount": 25,
  "dislikeCount": 2,
  "commentCount": 8,
  "status": "ACTIVE",
  "createdAt": "2025-12-20T10:00:00",
  "updatedAt": "2025-12-20T10:00:00",
  "images": ["https://..."],
  "isLiked": false,
  "isDisliked": false,
  "isJoined": true,  // 현재 사용자가 참여 중인지
  "participants": [  // 모집 게시글인 경우만
    {
      "userId": 456,
      "nickname": "김철수",
      "profileImage": "https://...",
      "joinedAt": "2025-12-21T10:00:00"
    }
  ]
}
```

#### 게시글 작성
```
POST /api/community/posts
Headers:
  Authorization: Bearer {token}

Request Body:
{
  "title": "12월 25일 종로구 봉사활동 10명 모집",
  "content": "...",
  "category": "봉사",
  "regionSi": "서울특별시",
  "regionGu": "종로구",
  "regionDong": "혜화동",
  "isRecruitment": true,
  "recruitmentMax": 10,
  "recruitmentDeadline": "2025-12-24T23:59:59",
  "eventDate": "2025-12-25T10:00:00",
  "eventLocation": "혜화역 2번 출구",
  "images": [
    "base64EncodedImage1",
    "base64EncodedImage2"
  ]
}

Response: 201 Created
{
  "id": 1,
  "message": "게시글이 작성되었습니다."
}
```

#### 게시글 수정
```
PUT /api/community/posts/{postId}
Headers:
  Authorization: Bearer {token}

Request Body: (게시글 작성과 동일)

Response: 200 OK
```

#### 게시글 삭제
```
DELETE /api/community/posts/{postId}
Headers:
  Authorization: Bearer {token}

Response: 204 No Content
```

### 2. 모집 참여 API ⭐

#### 모집 참여/취소 (토글)
```
POST /api/community/posts/{postId}/participate
Headers:
  Authorization: Bearer {token}

Response: 200 OK
{
  "isJoined": true,  // true: 참여, false: 취소
  "currentCount": 8,
  "maxCount": 10,
  "message": "모집에 참여했습니다."
}
```

#### 참여자 목록 조회
```
GET /api/community/posts/{postId}/participants

Response:
{
  "participants": [
    {
      "userId": 456,
      "nickname": "김철수",
      "profileImage": "https://...",
      "joinedAt": "2025-12-21T10:00:00"
    }
  ],
  "currentCount": 8,
  "maxCount": 10,
  "isFull": false
}
```

### 3. 댓글 API

#### 댓글 목록 조회
```
GET /api/community/posts/{postId}/comments

Response:
{
  "comments": [
    {
      "id": 1,
      "userId": 789,
      "userNickname": "이영희",
      "userProfileImage": "https://...",
      "content": "좋은 활동이네요! 참여하고 싶습니다.",
      "likeCount": 5,
      "dislikeCount": 0,
      "isLiked": false,
      "isDisliked": false,
      "createdAt": "2025-12-21T10:00:00",
      "replies": [
        {
          "id": 2,
          "userId": 123,
          "userNickname": "홍길동",
          "userProfileImage": "https://...",
          "content": "감사합니다! 많은 참여 부탁드려요!",
          "likeCount": 2,
          "dislikeCount": 0,
          "createdAt": "2025-12-21T10:30:00"
        }
      ]
    }
  ]
}
```

#### 댓글 작성
```
POST /api/community/posts/{postId}/comments
Headers:
  Authorization: Bearer {token}

Request Body:
{
  "content": "좋은 활동이네요!",
  "parentCommentId": null  // 대댓글인 경우 부모 댓글 ID
}

Response: 201 Created
{
  "id": 1,
  "message": "댓글이 작성되었습니다."
}
```

#### 댓글 수정
```
PUT /api/community/comments/{commentId}
Headers:
  Authorization: Bearer {token}

Request Body:
{
  "content": "수정된 댓글 내용"
}

Response: 200 OK
```

#### 댓글 삭제
```
DELETE /api/community/comments/{commentId}
Headers:
  Authorization: Bearer {token}

Response: 204 No Content
```

### 4. 추천/비추천 API

#### 게시글 추천/비추천
```
POST /api/community/posts/{postId}/vote
Headers:
  Authorization: Bearer {token}

Request Body:
{
  "voteType": "LIKE"  // LIKE 또는 DISLIKE
}

Response: 200 OK
{
  "likeCount": 26,
  "dislikeCount": 2,
  "userVoteType": "LIKE"  // 사용자의 현재 투표 상태
}
```

#### 게시글 추천/비추천 취소
```
DELETE /api/community/posts/{postId}/vote
Headers:
  Authorization: Bearer {token}

Response: 200 OK
{
  "likeCount": 25,
  "dislikeCount": 2,
  "userVoteType": null
}
```

---

## 🔧 비즈니스 로직

### 1. 모집 참여 처리

```java
@Transactional
public ParticipateResponse toggleParticipation(Long postId, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new PostNotFoundException());
    
    // 모집 게시글인지 확인
    if (!post.isRecruitment()) {
        throw new NotRecruitmentPostException();
    }
    
    // 마감 시간 확인
    if (LocalDateTime.now().isAfter(post.getRecruitmentDeadline())) {
        throw new RecruitmentClosedException();
    }
    
    // 기존 참여 여부 확인
    Optional<RecruitmentParticipant> existingParticipant = 
        participantRepository.findByPostIdAndUserId(postId, userId);
    
    if (existingParticipant.isPresent()) {
        // 이미 참여 중 → 취소
        RecruitmentParticipant participant = existingParticipant.get();
        
        if (participant.getStatus() == ParticipantStatus.JOINED) {
            participant.cancel();
            post.decrementCurrentCount();
            
            return ParticipateResponse.builder()
                .isJoined(false)
                .currentCount(post.getRecruitmentCurrent())
                .maxCount(post.getRecruitmentMax())
                .message("참여가 취소되었습니다.")
                .build();
        } else {
            // 취소 상태 → 다시 참여
            if (post.isFull()) {
                throw new RecruitmentFullException();
            }
            participant.rejoin();
            post.incrementCurrentCount();
            
            return ParticipateResponse.builder()
                .isJoined(true)
                .currentCount(post.getRecruitmentCurrent())
                .maxCount(post.getRecruitmentMax())
                .message("모집에 참여했습니다.")
                .build();
        }
    } else {
        // 신규 참여
        if (post.isFull()) {
            throw new RecruitmentFullException();
        }
        
        RecruitmentParticipant participant = RecruitmentParticipant.builder()
            .postId(postId)
            .userId(userId)
            .status(ParticipantStatus.JOINED)
            .build();
        
        participantRepository.save(participant);
        post.incrementCurrentCount();
        
        return ParticipateResponse.builder()
            .isJoined(true)
            .currentCount(post.getRecruitmentCurrent())
            .maxCount(post.getRecruitmentMax())
            .message("모집에 참여했습니다.")
            .build();
    }
}
```

### 2. 동시성 제어

모집 인원이 마감되는 순간 여러 사용자가 동시에 참여하는 경우를 대비:

```java
// 방법 1: 낙관적 락 (Optimistic Lock)
@Entity
@Table(name = "posts")
public class Post {
    @Version
    private Long version;
    
    // ...
}

// 방법 2: 비관적 락 (Pessimistic Lock)
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Post p WHERE p.id = :postId")
Optional<Post> findByIdForUpdate(@Param("postId") Long postId);

// 방법 3: Redis를 이용한 분산 락
@Transactional
public ParticipateResponse toggleParticipation(Long postId, Long userId) {
    String lockKey = "post:recruitment:" + postId;
    
    try (RLock lock = redissonClient.getLock(lockKey)) {
        boolean isLocked = lock.tryLock(10, 3, TimeUnit.SECONDS);
        
        if (!isLocked) {
            throw new LockAcquisitionException();
        }
        
        // 참여 처리 로직
        // ...
    }
}
```

### 3. 자동 마감 처리

스케줄러를 이용한 자동 마감:

```java
@Scheduled(cron = "0 */10 * * * *")  // 10분마다 실행
public void closeExpiredRecruitments() {
    LocalDateTime now = LocalDateTime.now();
    
    List<Post> expiredPosts = postRepository
        .findByIsRecruitmentTrueAndRecruitmentDeadlineBeforeAndStatus(
            now, PostStatus.ACTIVE
        );
    
    expiredPosts.forEach(post -> {
        post.setStatus(PostStatus.CLOSED);
        log.info("모집 마감: postId={}, title={}", post.getId(), post.getTitle());
    });
    
    postRepository.saveAll(expiredPosts);
}
```

---

## 📊 성능 최적화

### 1. 인덱스 전략
```sql
-- 게시글 목록 조회 최적화
CREATE INDEX idx_posts_list ON posts(is_deleted, created_at DESC, category, region_si);

-- 모집 게시글 조회 최적화
CREATE INDEX idx_recruitment_active ON posts(is_recruitment, recruitment_deadline, status)
WHERE is_recruitment = true AND status = 'ACTIVE';
```

### 2. 캐싱 전략
```java
// 인기 게시글 캐싱 (Redis)
@Cacheable(value = "popularPosts", key = "#regionSi + ':' + #category")
public List<PostDto> getPopularPosts(String regionSi, String category) {
    // ...
}

// 조회수 비동기 업데이트
@Async
public void incrementViewCount(Long postId) {
    String key = "post:view:" + postId;
    redisTemplate.opsForValue().increment(key);
    
    // 10회마다 DB에 반영
    Long viewCount = redisTemplate.opsForValue().get(key);
    if (viewCount % 10 == 0) {
        postRepository.updateViewCount(postId, viewCount);
    }
}
```

### 3. N+1 문제 해결
```java
// Fetch Join 사용
@Query("SELECT p FROM Post p " +
       "LEFT JOIN FETCH p.images " +
       "WHERE p.id = :postId")
Optional<Post> findByIdWithImages(@Param("postId") Long postId);

// Batch Size 설정
@BatchSize(size = 10)
@OneToMany(mappedBy = "post")
private List<PostImage> images;
```

---

## 🔐 보안 및 권한

### 1. 권한 검증
```java
// 게시글 작성자만 수정/삭제 가능
public void validateOwnership(Long postId, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new PostNotFoundException());
    
    if (!post.getUserId().equals(userId)) {
        throw new UnauthorizedException("권한이 없습니다.");
    }
}
```

### 2. Rate Limiting
```java
// 게시글 작성 제한 (1시간에 10개)
@RateLimiter(name = "postCreation", fallbackMethod = "postCreationFallback")
public PostDto createPost(CreatePostRequest request, Long userId) {
    // ...
}
```

---

## 🧪 테스트 시나리오

### 1. 모집 기능 테스트
```java
@Test
void 모집_참여_토글_테스트() {
    // Given
    Post recruitmentPost = createRecruitmentPost(10); // 최대 10명
    User user = createUser();
    
    // When - 첫 참여
    ParticipateResponse response1 = communityService.toggleParticipation(
        recruitmentPost.getId(), user.getId()
    );
    
    // Then
    assertThat(response1.isJoined()).isTrue();
    assertThat(response1.getCurrentCount()).isEqualTo(1);
    
    // When - 참여 취소
    ParticipateResponse response2 = communityService.toggleParticipation(
        recruitmentPost.getId(), user.getId()
    );
    
    // Then
    assertThat(response2.isJoined()).isFalse();
    assertThat(response2.getCurrentCount()).isEqualTo(0);
}

@Test
void 모집_정원_초과_테스트() {
    // Given
    Post recruitmentPost = createRecruitmentPost(2); // 최대 2명
    fillRecruitment(recruitmentPost, 2); // 2명 채움
    User newUser = createUser();
    
    // When & Then
    assertThatThrownBy(() -> 
        communityService.toggleParticipation(recruitmentPost.getId(), newUser.getId())
    ).isInstanceOf(RecruitmentFullException.class);
}

@Test
void 모집_마감_시간_초과_테스트() {
    // Given
    LocalDateTime pastDeadline = LocalDateTime.now().minusHours(1);
    Post expiredPost = createRecruitmentPost(10, pastDeadline);
    User user = createUser();
    
    // When & Then
    assertThatThrownBy(() -> 
        communityService.toggleParticipation(expiredPost.getId(), user.getId())
    ).isInstanceOf(RecruitmentClosedException.class);
}
```

---

## 🚀 배포 및 환경 설정

### PostgreSQL 설치 (Ubuntu)
```bash
# PostgreSQL 설치
sudo apt update
sudo apt install postgresql-15 postgresql-contrib

# PostgreSQL 시작
sudo systemctl start postgresql
sudo systemctl enable postgresql

# 데이터베이스 생성
sudo -u postgres psql
CREATE DATABASE community_db;
CREATE USER community_user WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE community_db TO community_user;
\q
```

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/community_db
    username: community_user
    password: your_password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate  # 운영: validate, 개발: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        default_batch_fetch_size: 100
  
  redis:
    host: localhost
    port: 6379
    password: your_redis_password

# 파일 업로드 설정
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB
```

---

## 📈 향후 확장 계획

1. **알림 기능**
   - 댓글 작성 시 게시글 작성자에게 알림
   - 모집 마감 1일 전 참여자에게 알림
   - 답글 작성 시 원 댓글 작성자에게 알림

2. **검색 기능**
   - Elasticsearch를 통한 전문 검색
   - 제목, 내용, 작성자 통합 검색
   - 검색어 자동완성

3. **신고 및 관리**
   - 부적절한 게시글/댓글 신고
   - 관리자 대시보드
   - 자동 필터링 (욕설, 스팸)

4. **소셜 기능**
   - 사용자 팔로우
   - 게시글 북마크
   - 참여 내역 조회

---

**작성일**: 2025-10-24  
**버전**: 1.0.0

