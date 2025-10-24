# MSA 트래픽 관리 가이드

## 목차
1. [현재 인프라 상황](#현재-인프라-상황)
2. [트래픽 관리 전략](#트래픽-관리-전략)
3. [단계별 구현 방안](#단계별-구현-방안)
4. [모니터링 및 알림](#모니터링-및-알림)
5. [비용 효율적인 확장 전략](#비용-효율적인-확장-전략)

---

## 현재 인프라 상황

### 리소스 제약
- **VirtualBox VM**: RAM 4GB, CPU 2코어
- **서비스 구성**: 5개 마이크로서비스 + Gateway + Frontend + DB + Redis
- **개발 환경**: 2인 개발팀

### 예상 트래픽 패턴
- **초기 단계**: 동시 접속자 10~50명
- **성장 단계**: 동시 접속자 100~500명
- **안정화 단계**: 동시 접속자 500~2000명

---

## 트래픽 관리 전략

### 1. **API Gateway를 통한 트래픽 제어**

#### 1.1 Rate Limiting (속도 제한)
```yaml
# Gateway Service에서 설정
rate_limiting:
  # 사용자별 요청 제한
  per_user:
    requests: 100
    window: 60s  # 1분에 100개 요청
  
  # IP별 요청 제한
  per_ip:
    requests: 200
    window: 60s
  
  # 전역 요청 제한
  global:
    requests: 1000
    window: 1s
```

**구현 방법 (Spring Cloud Gateway 예시)**:
```java
@Configuration
public class RateLimitConfig {
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Id")
        );
    }
    
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(
            100,  // replenishRate: 초당 생성되는 토큰 수
            200   // burstCapacity: 버킷 최대 용량
        );
    }
}
```

#### 1.2 Request Throttling (요청 조절)
```yaml
throttling:
  # 서비스별 동시 요청 수 제한
  user_service:
    max_concurrent: 50
  place_service:
    max_concurrent: 100
  recommendation_service:
    max_concurrent: 30
  community_service:
    max_concurrent: 50
```

---

### 2. **로드 밸런싱 (Load Balancing)**

#### 2.1 Docker Compose에서 서비스 복제
```yaml
# docker-compose.yml
services:
  user-service:
    image: maltan/user-service:latest
    deploy:
      replicas: 2  # 2개 인스턴스 실행
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    networks:
      - maltan-network

  # Nginx를 로드 밸런서로 사용
  load-balancer:
    image: nginx:alpine
    volumes:
      - ./nginx-lb.conf:/etc/nginx/nginx.conf
    ports:
      - "80:80"
    depends_on:
      - user-service
      - place-service
      - recommendation-service
      - community-service
    networks:
      - maltan-network
```

#### 2.2 Nginx 로드 밸런싱 설정
```nginx
# nginx-lb.conf
upstream user_service {
    least_conn;  # 가장 적은 연결을 가진 서버로 라우팅
    server user-service-1:8081 weight=1 max_fails=3 fail_timeout=30s;
    server user-service-2:8081 weight=1 max_fails=3 fail_timeout=30s;
}

upstream place_service {
    least_conn;
    server place-service-1:8082 weight=1;
    server place-service-2:8082 weight=1;
}

server {
    listen 80;
    
    location /api/users {
        proxy_pass http://user_service;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # 타임아웃 설정
        proxy_connect_timeout 5s;
        proxy_send_timeout 10s;
        proxy_read_timeout 10s;
    }
    
    location /api/places {
        proxy_pass http://place_service;
        # ... 동일한 헤더 설정
    }
}
```

---

### 3. **캐싱 전략 (Caching)**

#### 3.1 Redis 캐싱 계층
```java
@Service
public class PlaceService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Cacheable(value = "places", key = "#id", unless = "#result == null")
    public Place getPlace(Long id) {
        // DB 조회는 캐시 미스 시에만 발생
        return placeRepository.findById(id).orElse(null);
    }
    
    @CacheEvict(value = "places", key = "#place.id")
    public Place updatePlace(Place place) {
        return placeRepository.save(place);
    }
}
```

#### 3.2 캐싱 전략별 적용
```yaml
caching_strategy:
  # 읽기 전용 데이터 (장기 캐싱)
  static_data:
    ttl: 3600s  # 1시간
    examples:
      - 지역 코드 데이터
      - 카테고리 목록
  
  # 자주 조회되는 데이터 (중기 캐싱)
  hot_data:
    ttl: 300s  # 5분
    examples:
      - 인기 장소 목록
      - 추천 장소
  
  # 실시간 데이터 (단기 캐싱)
  realtime_data:
    ttl: 60s  # 1분
    examples:
      - 커뮤니티 최신 글
      - 사용자 프로필
```

---

### 4. **데이터베이스 최적화**

#### 4.1 Connection Pool 설정
```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10  # 최대 연결 수
      minimum-idle: 5        # 최소 유휴 연결 수
      connection-timeout: 30000  # 30초
      idle-timeout: 600000   # 10분
      max-lifetime: 1800000  # 30분
```

#### 4.2 Read Replica 구성 (성장 단계)
```yaml
# docker-compose.yml
services:
  postgres-master:
    image: postgres:15
    environment:
      - POSTGRES_DB=maltan
      - POSTGRES_USER=maltan_user
      - POSTGRES_PASSWORD=maltan_pass
    volumes:
      - postgres-master-data:/var/lib/postgresql/data
    command: postgres -c wal_level=replica -c max_wal_senders=3

  postgres-replica:
    image: postgres:15
    environment:
      - POSTGRES_DB=maltan
      - POSTGRES_USER=maltan_user
      - POSTGRES_PASSWORD=maltan_pass
    volumes:
      - postgres-replica-data:/var/lib/postgresql/data
    depends_on:
      - postgres-master
```

**애플리케이션에서 Read/Write 분리**:
```java
@Configuration
public class DataSourceConfig {
    @Bean
    @Primary
    public DataSource dataSource() {
        return new LazyConnectionDataSourceProxy(routingDataSource());
    }
    
    @Bean
    public DataSource routingDataSource() {
        ReplicationRoutingDataSource routingDataSource = 
            new ReplicationRoutingDataSource();
        
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("master", masterDataSource());
        dataSourceMap.put("replica", replicaDataSource());
        
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(masterDataSource());
        
        return routingDataSource;
    }
}
```

---

### 5. **서킷 브레이커 (Circuit Breaker)**

#### 5.1 Resilience4j 설정
```java
@Configuration
public class CircuitBreakerConfig {
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .slidingWindowSize(10)  // 최근 10개 요청 기준
                .failureRateThreshold(50)  // 실패율 50% 이상
                .waitDurationInOpenState(Duration.ofSeconds(30))  // 30초 대기
                .permittedNumberOfCallsInHalfOpenState(5)  // 반열림 상태에서 5개 테스트
                .build())
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))  // 3초 타임아웃
                .build())
            .build());
    }
}

@Service
public class RecommendationService {
    @CircuitBreaker(name = "recommendation", fallbackMethod = "fallbackRecommendations")
    public List<Place> getRecommendations(Long userId) {
        // 추천 서비스 호출
        return restTemplate.getForObject(
            "http://recommendation-service/api/recommendations/" + userId,
            List.class
        );
    }
    
    // Fallback 메서드
    private List<Place> fallbackRecommendations(Long userId, Exception e) {
        // 캐시된 데이터 또는 기본 추천 반환
        return cacheService.getCachedRecommendations(userId);
    }
}
```

---

## 단계별 구현 방안

### Phase 1: 초기 단계 (동시 접속자 10~50명)
**현재 VM 리소스로 충분**

```yaml
구현 항목:
  ✅ 기본 Rate Limiting (Gateway)
  ✅ Redis 캐싱 (자주 조회되는 데이터)
  ✅ Connection Pool 최적화
  ✅ 기본 모니터링 (Prometheus + Grafana)
  
리소스 할당:
  - 각 서비스: 1 인스턴스
  - DB: 단일 인스턴스
  - Redis: 단일 인스턴스
```

### Phase 2: 성장 단계 (동시 접속자 100~500명)
**VM 업그레이드 또는 클라우드 마이그레이션 고려**

```yaml
구현 항목:
  🔄 서비스 복제 (User, Place 서비스 2개씩)
  🔄 Nginx 로드 밸런싱
  🔄 Circuit Breaker 패턴
  🔄 DB Read Replica 추가
  🔄 고급 캐싱 전략
  
리소스 할당:
  - 주요 서비스: 2 인스턴스
  - 부하가 적은 서비스: 1 인스턴스
  - DB: Master 1 + Replica 1
  - Redis: 단일 인스턴스 (충분)
```

### Phase 3: 안정화 단계 (동시 접속자 500~2000명)
**클라우드 환경 권장 (AWS, GCP, Azure)**

```yaml
구현 항목:
  ☁️ Kubernetes 마이그레이션
  ☁️ Auto Scaling (HPA)
  ☁️ CDN 도입 (정적 파일)
  ☁️ DB 샤딩 고려
  ☁️ Message Queue (Kafka/RabbitMQ)
  ☁️ 분산 트레이싱 (Jaeger)
  
리소스 할당:
  - Auto Scaling (2~10 인스턴스)
  - DB: Master 1 + Replica 2+
  - Redis Cluster
  - CDN: CloudFront, CloudFlare
```

---

## 모니터링 및 알림

### 1. Prometheus + Grafana 대시보드

#### docker-compose.yml에 추가
```yaml
services:
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    ports:
      - "9090:9090"
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    networks:
      - maltan-network

  grafana:
    image: grafana/grafana:latest
    volumes:
      - grafana-data:/var/lib/grafana
      - ./monitoring/grafana-dashboards:/etc/grafana/provisioning/dashboards
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    depends_on:
      - prometheus
    networks:
      - maltan-network

  # 각 서비스의 메트릭 수집
  node-exporter:
    image: prom/node-exporter:latest
    ports:
      - "9100:9100"
    networks:
      - maltan-network

volumes:
  prometheus-data:
  grafana-data:
```

### 2. 주요 모니터링 지표

```yaml
시스템 메트릭:
  - CPU 사용률 (> 80% 경고)
  - 메모리 사용률 (> 85% 경고)
  - 디스크 I/O
  - 네트워크 트래픽

애플리케이션 메트릭:
  - 요청 처리 시간 (p50, p95, p99)
  - 요청 성공률 (< 95% 경고)
  - 에러율 (> 5% 경고)
  - 동시 접속자 수

데이터베이스 메트릭:
  - Connection Pool 사용률
  - 쿼리 실행 시간
  - Slow Query 발생 빈도
  - DB 연결 실패율

캐시 메트릭:
  - 캐시 히트율 (< 80% 경고)
  - Redis 메모리 사용률
  - 캐시 응답 시간
```

### 3. 알림 설정 (Alertmanager)

```yaml
# alertmanager.yml
route:
  receiver: 'team-notifications'
  group_by: ['alertname', 'service']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 4h

receivers:
  - name: 'team-notifications'
    slack_configs:
      - api_url: 'YOUR_SLACK_WEBHOOK_URL'
        channel: '#maltan-alerts'
        title: '🚨 {{ .GroupLabels.alertname }}'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'

# Prometheus alert rules
groups:
  - name: service_alerts
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          description: "서비스 에러율이 5%를 초과했습니다"
      
      - alert: HighMemoryUsage
        expr: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes > 0.85
        for: 5m
        annotations:
          description: "메모리 사용률이 85%를 초과했습니다"
```

---

## 비용 효율적인 확장 전략

### 1. 현재 환경 최적화 (무료/저비용)
```
✅ Docker Compose 리소스 제한 설정
✅ Redis 캐싱으로 DB 부하 감소
✅ Nginx 정적 파일 캐싱
✅ Connection Pool 튜닝
✅ 불필요한 로그 최소화
```

### 2. 저비용 클라우드 옵션
```
AWS Lightsail: $10~20/월
  - 2GB RAM, 1 vCPU
  - 고정 IP 포함
  - 간단한 관리

DigitalOcean Droplet: $12~24/월
  - 2~4GB RAM, 1~2 vCPU
  - 개발자 친화적

Oracle Cloud Free Tier: 무료
  - 4 vCPU, 24GB RAM (ARM)
  - 영구 무료
```

### 3. 단계적 마이그레이션
```
Step 1: VirtualBox (현재)
  ↓
Step 2: 단일 클라우드 VM (Lightsail/Droplet)
  ↓
Step 3: 멀티 VM + 로드 밸런서
  ↓
Step 4: Kubernetes (EKS/GKE/AKS)
```

---

## 실전 체크리스트

### 배포 전 확인사항
- [ ] Rate Limiting 설정 완료
- [ ] Redis 캐싱 적용
- [ ] Connection Pool 설정
- [ ] 로그 레벨 최적화 (WARN 이상)
- [ ] Health Check 엔드포인트 구현
- [ ] Graceful Shutdown 설정

### 운영 중 모니터링
- [ ] CPU/메모리 사용률 확인 (일 1회)
- [ ] 에러 로그 확인 (일 1회)
- [ ] 응답 시간 모니터링
- [ ] 캐시 히트율 확인
- [ ] DB Slow Query 분석 (주 1회)

### 트래픽 급증 시 대응
1. **즉시 조치**
   - Rate Limiting 강화
   - 캐시 TTL 증가
   - 불필요한 서비스 일시 중단

2. **단기 조치 (1~3일)**
   - 서비스 복제 (2배)
   - DB Connection Pool 증가
   - Redis 메모리 증설

3. **중장기 조치 (1~2주)**
   - VM 업그레이드
   - Read Replica 추가
   - CDN 도입

---

## 추천 도구 및 라이브러리

### Spring Boot 기반
```xml
<!-- Rate Limiting -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
</dependency>

<!-- Circuit Breaker -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot2</artifactId>
</dependency>

<!-- Caching -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Monitoring -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 모니터링 스택
- **Prometheus**: 메트릭 수집
- **Grafana**: 시각화 대시보드
- **Alertmanager**: 알림 관리
- **Loki**: 로그 수집 (선택)
- **Jaeger**: 분산 트레이싱 (선택)

---

## 결론

### 현재 단계 (개발 중)
**우선순위 높음**:
1. Gateway에 Rate Limiting 구현
2. Redis 캐싱 적용
3. 기본 모니터링 설정 (Prometheus + Grafana)

**우선순위 중간**:
4. Connection Pool 최적화
5. Circuit Breaker 패턴 적용

**우선순위 낮음** (성장 단계에서):
6. 서비스 복제 및 로드 밸런싱
7. DB Read Replica

### 핵심 원칙
- **측정 가능하게**: 모니터링 먼저, 최적화는 나중에
- **점진적 확장**: 필요할 때 단계적으로 확장
- **비용 효율**: 무료/저비용 도구 최대 활용
- **자동화**: 반복 작업은 스크립트화

현재 VM 환경에서도 **동시 접속자 100명까지는 충분히 처리 가능**합니다!

