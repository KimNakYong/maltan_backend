# MSA 데이터베이스 전략 가이드

## 📊 데이터베이스 전략 비교

### 1. 서비스별 독립 DB (Database per Service) ⭐ **추천**

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ User Service│     │Place Service│     │Community Svc│
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       ▼                   ▼                   ▼
  ┌─────────┐         ┌─────────┐         ┌─────────┐
  │ User DB │         │Place DB │         │Comm. DB │
  └─────────┘         └─────────┘         └─────────┘
```

#### ✅ 장점
- **독립성**: 각 서비스가 완전히 독립적으로 운영
- **확장성**: 서비스별로 DB 스케일링 가능
- **장애 격리**: 한 DB 장애가 다른 서비스에 영향 없음
- **기술 선택의 자유**: 서비스마다 다른 DB 사용 가능 (PostgreSQL, MongoDB, Redis 등)
- **배포 독립성**: DB 스키마 변경이 다른 서비스에 영향 없음

#### ❌ 단점
- **복잡성 증가**: 여러 DB 관리 필요
- **트랜잭션 처리 어려움**: 분산 트랜잭션 필요 (Saga 패턴)
- **데이터 일관성**: Eventual Consistency 필요
- **조인 불가**: 서비스 간 데이터 조인 불가능
- **비용**: DB 인스턴스가 여러 개 필요

---

### 2. 통합 DB + 테이블 분리

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ User Service│     │Place Service│     │Community Svc│
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       └───────────────────┴───────────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ Shared DB   │
                    │ ├─ users    │
                    │ ├─ places   │
                    │ └─ posts    │
                    └─────────────┘
```

#### ✅ 장점
- **단순성**: 하나의 DB만 관리
- **트랜잭션 용이**: ACID 트랜잭션 가능
- **조인 가능**: 테이블 간 JOIN 가능
- **비용 절감**: 단일 DB 인스턴스
- **데이터 일관성**: 강한 일관성 보장

#### ❌ 단점
- **결합도 증가**: 서비스 간 DB 의존성 발생
- **확장성 제한**: 전체 DB를 함께 스케일링해야 함
- **장애 전파**: DB 장애 시 모든 서비스 영향
- **배포 의존성**: 스키마 변경 시 모든 서비스 영향
- **MSA 원칙 위배**: 서비스 독립성 저하

---

### 3. 하이브리드 전략 (추천 - 2인 팀에 최적) 🎯

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ User Service│     │Place Service│     │Community Svc│
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       ▼                   └───────┬───────────┘
  ┌─────────┐                     ▼
  │ User DB │              ┌─────────────┐
  └─────────┘              │ Content DB  │
                           │ ├─ places   │
                           │ └─ posts    │
                           └─────────────┘
```

**핵심 서비스만 분리 + 나머지는 통합**

#### 전략
1. **User Service**: 독립 DB (보안 중요)
2. **Content DB**: Place + Community + Recommendation (읽기 많음)
3. **Redis**: 캐시 및 세션 (공유)

#### ✅ 장점
- **균형잡힌 접근**: 복잡성과 독립성의 균형
- **비용 효율**: DB 인스턴스 2~3개로 충분
- **보안**: 사용자 정보는 격리
- **성능**: 컨텐츠 DB는 읽기 최적화 가능
- **관리 용이**: 2인 팀에 적합한 복잡도

---

### 4. Schema per Service (같은 DB, 스키마 분리)

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ User Service│     │Place Service│     │Community Svc│
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       └───────────────────┴───────────────────┘
                           │
                           ▼
                    ┌─────────────────┐
                    │   PostgreSQL    │
                    │ ┌─────────────┐ │
                    │ │user_schema  │ │
                    │ │├─ users     │ │
                    │ │└─ regions   │ │
                    │ └─────────────┘ │
                    │ ┌─────────────┐ │
                    │ │place_schema │ │
                    │ │└─ places    │ │
                    │ └─────────────┘ │
                    │ ┌─────────────┐ │
                    │ │comm_schema  │ │
                    │ │└─ posts     │ │
                    │ └─────────────┘ │
                    └─────────────────┘
```

#### ✅ 장점
- **논리적 분리**: 스키마로 서비스 격리
- **단일 DB 관리**: 백업, 모니터링 간편
- **권한 관리**: 스키마별 접근 권한 설정 가능
- **비용 효율**: 단일 DB 인스턴스
- **마이그레이션 용이**: 나중에 DB 분리 쉬움

#### ❌ 단점
- **물리적 격리 없음**: 여전히 같은 DB 사용
- **리소스 경쟁**: CPU/메모리 공유
- **확장성 제한**: 전체 DB 스케일링 필요

---

## 🎯 2인 개발팀 추천 전략

### Phase 1: 개발 초기 (현재)
**통합 DB + 스키마 분리**

```yaml
PostgreSQL 단일 인스턴스:
  - user_schema (User Service)
  - content_schema (Place, Community, Recommendation)
  - shared_schema (공통 코드, 설정)

Redis:
  - 캐시
  - 세션
  - Rate Limiting
```

#### 구현 예시
```sql
-- 스키마 생성
CREATE SCHEMA user_schema;
CREATE SCHEMA content_schema;
CREATE SCHEMA shared_schema;

-- 사용자 생성 및 권한 부여
CREATE USER user_service WITH PASSWORD 'user_pass';
GRANT USAGE ON SCHEMA user_schema TO user_service;
GRANT ALL ON ALL TABLES IN SCHEMA user_schema TO user_service;

CREATE USER content_service WITH PASSWORD 'content_pass';
GRANT USAGE ON SCHEMA content_schema TO content_service;
GRANT ALL ON ALL TABLES IN SCHEMA content_schema TO content_service;
```

#### application.yml (User Service)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/maltan?currentSchema=user_schema
    username: user_service
    password: user_pass
  jpa:
    properties:
      hibernate:
        default_schema: user_schema
```

---

### Phase 2: 성장 단계 (사용자 100+)
**핵심 서비스 분리**

```yaml
User DB (PostgreSQL):
  - users
  - preferred_regions
  - user_sessions

Content DB (PostgreSQL):
  - places
  - posts
  - comments
  - recommendations

Redis Cluster:
  - 캐시
  - 세션
```

---

### Phase 3: 확장 단계 (사용자 1000+)
**완전 분리 + 특화 DB**

```yaml
User DB (PostgreSQL):
  - 사용자 정보

Place DB (PostgreSQL + PostGIS):
  - 장소 정보 (지리 데이터)

Community DB (PostgreSQL):
  - 게시글, 댓글

Recommendation DB (MongoDB):
  - 추천 알고리즘 데이터
  - 사용자 행동 로그

Search DB (Elasticsearch):
  - 전문 검색

Cache (Redis Cluster):
  - 분산 캐시
```

---

## 📋 구현 가이드

### 1. 통합 DB + 스키마 분리 (추천 시작점)

#### Docker Compose 설정
```yaml
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: maltan
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    networks:
      - maltan-network

volumes:
  postgres-data:
```

#### 초기화 스크립트 (init-scripts/01-create-schemas.sql)
```sql
-- 스키마 생성
CREATE SCHEMA IF NOT EXISTS user_schema;
CREATE SCHEMA IF NOT EXISTS content_schema;
CREATE SCHEMA IF NOT EXISTS shared_schema;

-- 사용자 생성
CREATE USER user_service WITH PASSWORD 'user_pass';
CREATE USER content_service WITH PASSWORD 'content_pass';

-- 권한 부여
GRANT USAGE ON SCHEMA user_schema TO user_service;
GRANT ALL ON ALL TABLES IN SCHEMA user_schema TO user_service;
GRANT ALL ON ALL SEQUENCES IN SCHEMA user_schema TO user_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA user_schema 
  GRANT ALL ON TABLES TO user_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA user_schema 
  GRANT ALL ON SEQUENCES TO user_service;

GRANT USAGE ON SCHEMA content_schema TO content_service;
GRANT ALL ON ALL TABLES IN SCHEMA content_schema TO content_service;
GRANT ALL ON ALL SEQUENCES IN SCHEMA content_schema TO content_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA content_schema 
  GRANT ALL ON TABLES TO content_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA content_schema 
  GRANT ALL ON SEQUENCES TO content_service;

-- 공유 스키마는 모두 읽기 가능
GRANT USAGE ON SCHEMA shared_schema TO user_service, content_service;
GRANT SELECT ON ALL TABLES IN SCHEMA shared_schema TO user_service, content_service;
```

---

### 2. 서비스 간 데이터 공유 패턴

#### 패턴 1: API 호출 (추천)
```java
// Place Service에서 User 정보 필요 시
@Service
public class PlaceService {
    @Autowired
    private UserServiceClient userServiceClient;
    
    public PlaceWithOwner getPlaceWithOwner(Long placeId) {
        Place place = placeRepository.findById(placeId);
        User owner = userServiceClient.getUserById(place.getOwnerId());
        return new PlaceWithOwner(place, owner);
    }
}
```

#### 패턴 2: 이벤트 기반 (비동기)
```java
// User Service: 사용자 생성 시 이벤트 발행
@Service
public class UserService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public User createUser(UserDto dto) {
        User user = userRepository.save(new User(dto));
        eventPublisher.publishEvent(new UserCreatedEvent(user));
        return user;
    }
}

// Recommendation Service: 이벤트 수신
@EventListener
public void handleUserCreated(UserCreatedEvent event) {
    // 새 사용자를 위한 초기 추천 생성
    recommendationService.createInitialRecommendations(event.getUser());
}
```

#### 패턴 3: 데이터 복제 (읽기 전용)
```java
// Place Service에 User 정보 캐시
@Entity
@Table(name = "user_cache")
public class UserCache {
    @Id
    private Long userId;
    private String username;
    private String email;
    private LocalDateTime lastUpdated;
}

// 주기적으로 동기화
@Scheduled(fixedRate = 300000) // 5분마다
public void syncUserCache() {
    List<User> users = userServiceClient.getAllUsers();
    users.forEach(user -> {
        UserCache cache = new UserCache(user);
        userCacheRepository.save(cache);
    });
}
```

---

### 3. 분산 트랜잭션 처리 (Saga 패턴)

#### Choreography Saga (이벤트 기반)
```java
// 1. Place 생성 요청
@Service
public class PlaceService {
    public Place createPlace(PlaceDto dto) {
        Place place = placeRepository.save(new Place(dto));
        eventPublisher.publishEvent(new PlaceCreatedEvent(place));
        return place;
    }
}

// 2. Recommendation Service: 추천 생성
@EventListener
public void handlePlaceCreated(PlaceCreatedEvent event) {
    try {
        recommendationService.createRecommendations(event.getPlace());
        eventPublisher.publishEvent(new RecommendationCreatedEvent());
    } catch (Exception e) {
        // 실패 시 보상 트랜잭션
        eventPublisher.publishEvent(new PlaceCreationFailedEvent(event.getPlace()));
    }
}

// 3. Place Service: 보상 트랜잭션
@EventListener
public void handlePlaceCreationFailed(PlaceCreationFailedEvent event) {
    placeRepository.delete(event.getPlace());
}
```

#### Orchestration Saga (중앙 제어)
```java
@Service
public class PlaceCreationOrchestrator {
    public void createPlaceWithRecommendations(PlaceDto dto) {
        Place place = null;
        try {
            // 1. Place 생성
            place = placeService.createPlace(dto);
            
            // 2. Recommendation 생성
            recommendationService.createRecommendations(place);
            
            // 3. 알림 전송
            notificationService.notifyNewPlace(place);
            
        } catch (Exception e) {
            // 보상 트랜잭션
            if (place != null) {
                placeService.deletePlace(place.getId());
            }
            throw new PlaceCreationException("Failed to create place", e);
        }
    }
}
```

---

## 🔄 마이그레이션 전략

### 통합 DB → 분리 DB 마이그레이션

#### Step 1: 준비
```sql
-- 새 DB 생성
CREATE DATABASE user_db;
CREATE DATABASE content_db;
```

#### Step 2: 데이터 복사
```bash
# User 스키마 덤프
pg_dump -h localhost -U postgres -n user_schema maltan > user_schema.sql

# 새 DB로 복원
psql -h localhost -U postgres user_db < user_schema.sql
```

#### Step 3: 애플리케이션 설정 변경
```yaml
# Before
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/maltan?currentSchema=user_schema

# After
spring:
  datasource:
    url: jdbc:postgresql://user-db:5432/user_db
```

#### Step 4: 점진적 전환
```java
@Configuration
public class DataSourceConfig {
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        // 새 DB
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://user-db:5432/user_db")
            .build();
    }
    
    @Bean
    public DataSource fallbackDataSource() {
        // 기존 DB (백업용)
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://postgres:5432/maltan?currentSchema=user_schema")
            .build();
    }
}
```

---

## 📊 성능 최적화

### 1. Read Replica
```yaml
# docker-compose.yml
services:
  postgres-master:
    image: postgres:15
    environment:
      POSTGRES_REPLICATION_MODE: master
    
  postgres-replica:
    image: postgres:15
    environment:
      POSTGRES_REPLICATION_MODE: slave
      POSTGRES_MASTER_HOST: postgres-master
```

### 2. Connection Pooling
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 3. 쿼리 최적화
```java
// N+1 문제 해결
@Query("SELECT p FROM Place p JOIN FETCH p.owner WHERE p.id = :id")
Place findByIdWithOwner(@Param("id") Long id);

// 페이징
Page<Place> findAll(Pageable pageable);

// 인덱스 활용
@Table(indexes = {
    @Index(name = "idx_place_category", columnList = "category"),
    @Index(name = "idx_place_region", columnList = "city, district")
})
```

---

## 🎯 최종 추천

### 2인 개발팀 최적 전략

```
📦 Phase 1 (현재 - 개발 중)
└─ PostgreSQL 단일 인스턴스
   ├─ user_schema (User Service)
   ├─ content_schema (Place, Community, Recommendation)
   └─ Redis (캐시, 세션)

📦 Phase 2 (사용자 100+ 또는 6개월 후)
├─ User DB (PostgreSQL)
├─ Content DB (PostgreSQL)
└─ Redis (캐시, 세션)

📦 Phase 3 (사용자 1000+ 또는 1년 후)
├─ User DB (PostgreSQL)
├─ Place DB (PostgreSQL + PostGIS)
├─ Community DB (PostgreSQL)
├─ Recommendation DB (MongoDB)
└─ Redis Cluster
```

### 이유
1. **단순성**: 초기에는 관리 복잡도 최소화
2. **비용**: VM 리소스 효율적 사용
3. **확장성**: 필요시 쉽게 분리 가능
4. **학습**: MSA 패턴 학습에 적합
5. **실용성**: 2인 팀에 현실적

### 시작 코드
```sql
-- init-scripts/01-init-db.sql
CREATE SCHEMA user_schema;
CREATE SCHEMA content_schema;
CREATE SCHEMA shared_schema;

-- 공통 코드 테이블 (shared_schema)
CREATE TABLE shared_schema.regions (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL
);

-- 초기 데이터
INSERT INTO shared_schema.regions VALUES
('seoul', '서울특별시', 'city'),
('gangnam', '강남구', 'district');
```

이 전략으로 시작하면 나중에 필요할 때 쉽게 확장할 수 있습니다! 🚀

