# 우리동네 (Maltan) - 프로젝트 문서

## 📑 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [시스템 아키텍처](#2-시스템-아키텍처)
3. [백엔드 구조](#3-백엔드-구조)
4. [프론트엔드 구조](#4-프론트엔드-구조)
5. [데이터베이스 설계](#5-데이터베이스-설계)
6. [배포 및 인프라](#6-배포-및-인프라)
7. [주요 기능 구현](#7-주요-기능-구현)
8. [트러블슈팅](#8-트러블슈팅)

---

## 1. 프로젝트 개요

### 1.1 프로젝트 소개

**우리동네 (Maltan)**는 사용자의 관심 지역 기반 장소 정보 제공 및 지역 커뮤니티 플랫폼입니다.

### 1.2 주요 기능

- **사용자 관리**: 회원가입, 로그인, 관심 지역 설정 (최대 3개)
- **장소 정보**: 주변 장소 검색, 카테고리 필터링, 지도 기반 동적 탐색
- **커뮤니티**: 게시글 작성, 댓글, 투표, 모집 기능, Place DB 연동 장소 선택
- **관리자 페이지**: 사용자, 장소, 커뮤니티 관리 (지도 클릭으로 위치 자동 입력)
- **모니터링**: 시스템 리소스, 서비스 상태, DB 메트릭, 로그 수집

### 1.3 기술 스택

#### 백엔드
- Java 17, Spring Boot 3.2.0
- MSA (Microservices Architecture)
- Spring Cloud Gateway
- MySQL 8.0 (User, Place) / PostgreSQL 15 (Community)
- Redis 7, Docker Compose

#### 프론트엔드
- TypeScript, React 18, Vite
- Material-UI, Redux Toolkit, React Query
- Google Maps API

#### DevOps
- GitHub Actions (CI/CD)
- Nginx, Ubuntu Server
- Ngrok (개발/테스트용 HTTPS 터널)

---

## 2. 시스템 아키텍처

### 2.1 전체 구조

```
브라우저 → Ngrok (HTTPS) → Nginx (3000) → Gateway (8080) → Services
                                                 ├─ User (8081) → MySQL (Host)
                                                 ├─ Place (8082) → MySQL (Host)
                                                 ├─ Community (8083) → PostgreSQL (Host)
                                                 ├─ Recommendation (8084) → PostgreSQL (Host)
                                                 └─ Monitoring (8085) → Docker API
```

**DB 연결 방식**:
- 모든 마이크로서비스는 Docker 컨테이너로 실행
- MySQL, PostgreSQL, Redis는 Ubuntu 호스트에서 직접 실행 (`10.0.2.15`)
- Docker 컨테이너 → `10.0.2.15:3306/5432` → 호스트 DB

### 2.2 마이크로서비스 구성

| 서비스 | 포트 | DB | 주요 기능 |
|--------|------|-----|----------|
| **Gateway** | 8080 | Redis (Host) | API 라우팅, CORS |
| **User** | 8081 | MySQL (Host) | 회원가입, 로그인, 관심 지역 |
| **Place** | 8082 | MySQL (Host) | 장소 CRUD, 검색, 카테고리 |
| **Community** | 8083 | PostgreSQL (Host) | 게시글, 댓글, 투표 |
| **Recommendation** | 8084 | PostgreSQL (Host) | 추천 (미구현) |
| **Monitoring** | 8085 | - | 시스템/서비스/DB 모니터링, 로그 수집 |

---

## 3. 백엔드 구조

### 3.1 Gateway Service

**라우팅 규칙** (`application.yml`)

```yaml
routes:
  - id: user-service
    uri: http://localhost:8081
    predicates: [Path=/api/user/**]
    filters: [RewritePath=/api/user/(?<segment>.*), /api/$\{segment}]
  
  - id: place-service-places
    uri: http://localhost:8082
    predicates: [Path=/api/places/**]
  
  - id: place-service-categories
    uri: http://localhost:8082
    predicates: [Path=/api/categories/**]
  
  - id: community-service
    uri: http://localhost:8083
    predicates: [Path=/api/community/**]
  
  - id: monitoring-service
    uri: http://localhost:8085
    predicates: [Path=/api/monitoring/**]
```

**CORS 설정**

```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowedOrigins: ["http://localhost:3000", "https://*.ngrok-free.dev"]
      allowedMethods: [GET, POST, PUT, DELETE, PATCH, OPTIONS]
      allowCredentials: true
```

### 3.2 User Service

**주요 엔티티**

```sql
-- 사용자
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 관심 지역 (사용자당 최대 3개)
CREATE TABLE preferred_regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    city VARCHAR(50) NOT NULL,
    district VARCHAR(50) NOT NULL,
    priority INT NOT NULL CHECK (priority BETWEEN 1 AND 3),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_priority (user_id, priority)
);
```

**주요 API**

- `POST /api/user/auth/register` - 회원가입
- `POST /api/user/auth/login` - 로그인 (JWT 토큰 발급)
- `GET /api/user/regions/preferred` - 관심 지역 조회
- `POST /api/user/regions/preferred` - 관심 지역 추가

### 3.3 Place Service

**주요 엔티티**

```sql
-- 카테고리
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    sort_order INT DEFAULT 0
);

-- 장소
CREATE TABLE places (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    category_id BIGINT,
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    review_count INT DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_location (latitude, longitude)
);
```

**주변 장소 검색 (Haversine Formula)**

```java
@Query("SELECT p, " +
       "(6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * " +
       "cos(radians(p.longitude) - radians(:lng)) + " +
       "sin(radians(:lat)) * sin(radians(p.latitude)))) AS distance " +
       "FROM Place p WHERE p.isActive = true " +
       "HAVING distance <= :radius ORDER BY distance")
List<Place> findNearbyPlaces(@Param("lat") BigDecimal lat, 
                              @Param("lng") BigDecimal lng, 
                              @Param("radius") Double radius);
```

**주요 API**

- `GET /api/places/nearby?latitude=37.5665&longitude=126.978&radius=5&categoryId=1` - 주변 장소 검색
- `GET /api/categories` - 카테고리 목록
- `GET /api/categories/with-count` - 장소 개수 포함 카테고리 목록

### 3.4 Community Service

**주요 엔티티**

```sql
-- 게시글
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50),
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    is_recruitment BOOLEAN DEFAULT FALSE,
    recruitment_max_participants INT,
    recruitment_deadline TIMESTAMP,
    -- 장소 정보 (Place DB 참조)
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    address VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 댓글
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_comment_id BIGINT,  -- 대댓글
    content TEXT NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);
```

### 3.5 Monitoring Service (신규)

**주요 기능**

- **시스템 메트릭**: CPU, 메모리, 디스크 사용률
- **서비스 메트릭**: Docker 컨테이너 상태, 리소스 사용량, 가동 시간
- **데이터베이스 메트릭**: 연결 수, DB 크기, 테이블 수
- **로그 수집**: 각 서비스의 로그 수집 및 필터링

**주요 API**

- `GET /api/monitoring/system/metrics` - 시스템 메트릭
- `GET /api/monitoring/services/metrics` - 서비스별 메트릭
- `GET /api/monitoring/databases/metrics` - DB 메트릭
- `GET /api/monitoring/logs?limit=100&service=user-service&level=ERROR` - 로그 조회

**기술 스택**

```xml
<!-- Docker Java API Client -->
<dependency>
    <groupId>com.github.docker-java</groupId>
    <artifactId>docker-java-core</artifactId>
    <version>3.3.4</version>
</dependency>
```

**Docker 소켓 마운트**

```yaml
monitoring-service:
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock  # Docker API 접근
```

---

## 4. 프론트엔드 구조

### 4.1 프로젝트 구조

```
maltan-frontend/
├── src/
│   ├── components/           # 재사용 컴포넌트
│   │   ├── Layout.tsx        # 네비게이션 바 포함 레이아웃
│   │   ├── GoogleMap.tsx     # Google Maps 통합 컴포넌트 (debounce 포함)
│   │   └── PrivateRoute.tsx  # 인증 라우트 보호
│   ├── pages/               # 페이지
│   │   ├── HomePage.tsx      # 홈페이지 (관심 지역 표시)
│   │   ├── LoginPage.tsx
│   │   ├── MapTestPage.tsx   # 주변 장소 찾기 (동적 지도 필터링)
│   │   ├── CommunityPage.tsx
│   │   ├── CommunityWritePage.tsx  # Place DB 연동 장소 선택
│   │   └── admin/            # 관리자 페이지
│   │       ├── PlacesPage.tsx   # 장소 관리 (지도 클릭 위치 입력)
│   │       ├── DashboardPage.tsx
│   │       ├── ServicesPage.tsx
│   │       ├── DatabasesPage.tsx
│   │       └── LogsPage.tsx
│   ├── services/            # API 호출
│   │   ├── authService.ts
│   │   ├── placeService.ts
│   │   ├── categoryService.ts
│   │   ├── adminService.ts
│   │   └── monitoringService.ts  # 모니터링 서비스 (새로 분리)
│   ├── store/               # Redux
│   │   └── slices/
│   │       └── authSlice.ts
│   └── utils/               # 유틸리티
│       └── placeCategories.tsx
└── .github/workflows/deploy.yml
```

### 4.2 라우팅

```typescript
// 공개
/                   → HomePage
/map-test           → MapTestPage (주변 장소 찾기)
/community          → CommunityPage

// 인증 필요
/profile            → ProfilePage
/community/write    → CommunityWritePage

// 관리자
/admin/places       → PlacesPage
/admin/users        → UsersPage
```

### 4.3 API 서비스 레이어

**Axios 인터셉터** (`api.ts`)

```typescript
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
});

// 요청 인터셉터: JWT 토큰 자동 추가
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// 401 에러 시 로그아웃
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 4.4 환경 변수

```bash
# .env
VITE_API_BASE_URL=           # 빈 값 = Nginx 프록시 사용
VITE_GOOGLE_MAPS_API_KEY=your_google_maps_api_key
```

---

## 5. 데이터베이스 설계

### 5.1 MySQL (User Service) - `userdb`

- `users`: 사용자 정보
- `preferred_regions`: 관심 지역 (1:N)

### 5.2 MySQL (Place Service) - `placedb`

- `categories`: 카테고리 (음식점, 관광지, 숙박, 쇼핑, 문화)
- `places`: 장소 정보 (N:1 categories)
- `reviews`: 리뷰 (N:1 places)

### 5.3 PostgreSQL (Community Service) - `community_db`

- `posts`: 게시글
- `comments`: 댓글 (N:1 posts, 대댓글 지원)
- `post_votes`: 투표

---

## 6. 배포 및 인프라

### 6.1 배포 구조

```
Internet → Ngrok (HTTPS) → Ubuntu Server
                            ├─ Nginx (3000)
                            │   ├─ /var/www/maltan-frontend/dist (React 빌드)
                            │   └─ /api/* → http://localhost:8080
                            ├─ Docker Containers
                            │   ├─ Gateway (8080)
                            │   ├─ User (8081)
                            │   ├─ Place (8082)
                            │   ├─ Community (8083)
                            │   ├─ Recommendation (8084)
                            │   └─ Monitoring (8085)
                            └─ Host Services (10.0.2.15)
                                ├─ MySQL (3306) - userdb, placedb
                                ├─ PostgreSQL (5432) - community_db, recommendation_db
                                └─ Redis (6379)
```

**DB 연결 구조**

모든 Docker 컨테이너는 VirtualBox NAT 게이트웨이 IP (`10.0.2.15`)를 통해 호스트의 DB에 접근합니다:

```yaml
# docker-compose.yml
user-service:
  environment:
    - DB_HOST=10.0.2.15  # VirtualBox 호스트 게이트웨이
    - DB_PORT=3306
    - DB_NAME=user_service
```

### 6.2 GitHub Actions 배포

**프론트엔드** (`.github/workflows/deploy.yml`)

```yaml
name: Deploy Frontend
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - run: npm ci
      - run: |
          echo "VITE_API_BASE_URL=" > .env
          echo "VITE_GOOGLE_MAPS_API_KEY=${{ secrets.VITE_GOOGLE_MAPS_API_KEY }}" >> .env
      - run: npm run build
      
      - name: Deploy to Server
        uses: appleboy/scp-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USERNAME }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          source: "dist/*"
          target: "/var/www/maltan-frontend"
          strip_components: 1
```

**백엔드** (수동 배포)

```bash
# 서버 SSH 접속
ssh user@server

# 백엔드 코드 업데이트
cd ~/maltan-backend
git pull origin main

# 서비스 재빌드
cd backend/gateway-service
./mvnw clean package -DskipTests

# Docker 재시작
cd ~/maltan-backend/docker
docker-compose restart gateway-service

# 로그 확인
docker logs -f gateway-service
```

### 6.3 Nginx 설정

`/etc/nginx/sites-available/maltan-frontend`

```nginx
server {
    listen 3000 default_server;
    root /var/www/maltan-frontend/dist;
    index index.html;
    
    # API 프록시
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        
        # 캐시 방지
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
        add_header Expires "0";
    }
    
    # React Router (SPA)
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 6.4 Ngrok 실행

**백그라운드 실행**

```bash
# nohup 사용
nohup ngrok http 3000 > ngrok.log 2>&1 &

# systemd 서비스 (자동 재시작)
sudo tee /etc/systemd/system/ngrok.service > /dev/null <<EOF
[Unit]
Description=Ngrok Tunnel
After=network.target

[Service]
Type=simple
User=$USER
ExecStart=/usr/local/bin/ngrok http 3000
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl enable --now ngrok
sudo systemctl status ngrok
```

**URL 확인**

```bash
curl http://localhost:4040/api/tunnels | jq '.tunnels[0].public_url'
# https://reserved-jolie-untastily.ngrok-free.dev
```

---

## 7. 주요 기능 구현

### 7.1 주변 장소 찾기 (MapTestPage)

**지도 범위 기반 동적 필터링**

```typescript
const loadPlacesInBounds = async (bounds: google.maps.LatLngBounds, categoryCode?: string) => {
  const center = bounds.getCenter();
  const ne = bounds.getNorthEast();
  
  // 현재 지도 범위의 1.5배 반경으로 서버에서 데이터 가져오기
  const radius = (google.maps.geometry.spherical.computeDistanceBetween(
    center, ne
  ) / 1000) * 1.5;

  const categoryId = categoryCode
    ? PLACE_CATEGORIES.find(c => c.code === categoryCode)?.id
    : undefined;

  const nearbyPlaces = await getNearbyPlaces(
    center.lat(), center.lng(), radius, categoryId
  );

  // 클라이언트 측에서 현재 지도 범위 내의 장소만 필터링
  const placesInView = nearbyPlaces.filter((place) =>
    bounds.contains(new google.maps.LatLng(place.latitude, place.longitude))
  );

  setPlaces(placesInView);
  setMarkers(placesInView.map(p => ({
    id: p.id.toString(),
    position: { lat: p.latitude, lng: p.longitude },
    title: p.name,
  })));
};
```

**지도 이벤트 처리 (디바운스)**

```typescript
<GoogleMap
  center={center}
  zoom={14}
  markers={markers}
  onMapLoad={(map) => {
    setMap(map);
    const bounds = map.getBounds();
    if (bounds) loadPlacesInBounds(bounds);
  }}
  onBoundsChanged={(map) => {
    // 디바운스 0.5초 (GoogleMap 컴포넌트 내부 구현)
    const bounds = map.getBounds();
    if (bounds) loadPlacesInBounds(bounds, selectedCategory || undefined);
  }}
/>
```

### 7.2 커뮤니티 게시글 작성 - Place DB 장소 선택

**Material-UI Autocomplete로 Place DB 검색**

```typescript
// 장소 검색 (debounce 300ms)
useEffect(() => {
  if (placeSearchInput.length < 2) {
    setPlaceOptions([]);
    return;
  }

  const timer = setTimeout(async () => {
    setPlaceLoading(true);
    try {
      const response = await searchPlaces(placeSearchInput, undefined, 0, 20);
      setPlaceOptions(response.content || []);
    } finally {
      setPlaceLoading(false);
    }
  }, 300);

  return () => clearTimeout(timer);
}, [placeSearchInput]);

// UI
<Autocomplete
  options={placeOptions}
  getOptionLabel={(option) => option.name}
  loading={placeLoading}
  onChange={(_event, newValue) => {
    setSelectedPlace(newValue);
    if (newValue) {
      setFormData(prev => ({
        ...prev,
        latitude: newValue.latitude,
        longitude: newValue.longitude,
        address: newValue.address,
      }));
    }
  }}
  renderOption={(props, option) => (
    <Box component="li" {...props}>
      <Typography>{option.name}</Typography>
      <Typography variant="caption" color="text.secondary">
        {option.address} • {option.categoryName}
      </Typography>
    </Box>
  )}
/>
```

**장점**
- ✅ Google Places API 비용 절감
- ✅ 관리자가 검증한 장소만 노출
- ✅ 장소 리뷰와 커뮤니티 게시글 연계 가능

### 7.3 관심 지역 설정 (ProfilePage)

```typescript
const handleAddRegion = async () => {
  if (preferredRegions.length >= 3) {
    toast.error('관심 지역은 최대 3개까지 등록 가능');
    return;
  }
  
  await addPreferredRegion({
    city: selectedRegion.city,
    district: selectedRegion.district,
    priority: preferredRegions.length + 1
  });
  
  toast.success('관심 지역 추가 완료');
  fetchPreferredRegions();
};
```

### 7.4 관리자 페이지 - 지도 클릭으로 장소 추가 (PlacesPage)

**지도 클릭 → Geocoding → 자동 입력**

```typescript
const handleMapClick = async (lat: number, lng: number) => {
  // 위도/경도 설정
  setFormData((prev) => ({
    ...prev,
    latitude: lat.toFixed(8),
    longitude: lng.toFixed(8),
  }));
  setMapMarker({ lat, lng });

  // Google Geocoding API로 주소 가져오기
  try {
    const geocoder = new google.maps.Geocoder();
    const response = await geocoder.geocode({ location: { lat, lng } });
    
    if (response.results && response.results.length > 0) {
      const address = response.results[0].formatted_address;
      setFormData((prev) => ({ ...prev, address }));
      toast.success('위치와 주소가 설정되었습니다');
    }
  } catch (error) {
    toast.error('주소를 가져오는데 실패했습니다');
  }
};
```

**UI 구성**

- 왼쪽: Google Map (클릭 시 마커 표시)
- 오른쪽: 장소 정보 입력 폼 (위도/경도/주소 자동 입력)
- "내 위치" 버튼으로 현재 위치 사용 가능

**장점**
- ✅ 관리자가 직접 지도에서 정확한 위치 선택 가능
- ✅ Geocoding으로 주소 자동 입력, 입력 오류 방지
- ✅ 직관적인 UX

---

## 8. 트러블슈팅

### 8.1 API 요청이 HTML을 반환

**증상**: `response.data: "<!doctype html>..."`

**원인**: `VITE_API_BASE_URL`이 ngrok URL로 설정되어 프론트엔드 순환 참조 발생

**해결**:
```yaml
# .github/workflows/deploy.yml
echo "VITE_API_BASE_URL=" > .env  # 빈 값으로 설정
```

빈 값으로 설정 시 상대 경로 `/api/*` 사용 → Nginx가 `http://localhost:8080/api/*`로 프록시

---

### 8.2 브라우저 캐싱 (304 Not Modified)

**증상**: API 요청이 캐시된 HTML 반환

**해결**:

1. **Nginx에서 캐시 방지**
```nginx
location /api/ {
    add_header Cache-Control "no-cache, no-store, must-revalidate";
    add_header Pragma "no-cache";
    add_header Expires "0";
}
```

2. **브라우저에서**
- 하드 리프레시: `Ctrl + Shift + R`
- 개발자 도구 → Network → Disable cache 활성화

---

### 8.3 카테고리 필터링 미작동

**증상**: `categoryId: undefined`

**원인**: `categoryCode`를 `categoryId`로 변환하지 않음

**해결**:
```typescript
const categoryId = categoryCode 
  ? PLACE_CATEGORIES.find(c => c.code === categoryCode)?.id 
  : undefined;

await getNearbyPlaces(lat, lng, radius, categoryId);  // ✅ 추가
```

---

### 8.4 Ngrok 연결 실패 (ERR_NGROK_8012)

**증상**: `dial tcp 127.0.0.1:80: connect: connection refused`

**원인**: Ngrok이 포트 80으로 연결 시도, 실제 서버는 3000 포트

**해결**:
```bash
pkill ngrok
ngrok http 3000
```

---

### 8.5 Gateway 라우팅 누락 (404 Not Found)

**증상**: `/api/categories/with-count` 요청 시 404

**원인**: Gateway에 `/api/categories/**` 라우팅 설정 누락

**해결**:
```yaml
# application.yml
routes:
  - id: place-service-categories
    uri: http://localhost:8082
    predicates:
      - Path=/api/categories/**
```

---

### 8.6 User Service DB 연결 실패 (500 Error)

**증상**: `Access denied for user 'root'@'172.19.0.6'`

**원인**: Docker 컨테이너에서 호스트 MySQL 접근 권한 없음

**해결**:
```bash
# MySQL 접속
sudo mysql -u root -p

# Docker 네트워크에서 접근 허용
GRANT ALL PRIVILEGES ON userdb.* TO 'root'@'172.%.%.%' IDENTIFIED BY 'password';
FLUSH PRIVILEGES;

# MySQL 바인드 주소 확인
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
# bind-address = 0.0.0.0

sudo systemctl restart mysql
```

---

## 9. 향후 개발 계획

### 9.1 기능 개선
- **추천 시스템 구현**: 협업 필터링 기반 장소 추천
- **실시간 알림**: WebSocket을 통한 댓글, 좋아요 알림
- **검색 고도화**: Elasticsearch 도입으로 전체 텍스트 검색
- **이미지 업로드**: 게시글/장소 사진 업로드 (AWS S3 or MinIO)
- **리뷰 시스템**: 장소별 사용자 리뷰 및 평점

### 9.2 인증 및 보안
- **소셜 로그인**: Google, Kakao, Naver OAuth 연동
- **JWT Refresh Token**: 토큰 갱신 로직 추가
- **API Rate Limiting**: Redis 기반 요청 제한
- **HTTPS**: Let's Encrypt SSL 인증서 적용 (Ngrok 대체)

### 9.3 인프라 및 모니터링
- **Prometheus + Grafana**: 메트릭 시각화
- **ELK Stack**: 로그 통합 관리 (Elasticsearch, Logstash, Kibana)
- **Kubernetes**: 오케스트레이션으로 배포 자동화
- **CI/CD 고도화**: Blue-Green 배포, Canary 배포

### 9.4 기타
- **모바일 앱**: React Native 또는 Flutter
- **다국어 지원**: i18n 라이브러리 적용 (한국어, 영어)
- **테스트 커버리지**: Unit Test, Integration Test 확대

---

## 10. 참고 자료

- [Spring Boot](https://spring.io/projects/spring-boot)
- [React](https://react.dev/)
- [Material-UI](https://mui.com/)
- [Docker](https://docs.docker.com/)
- [Nginx](https://nginx.org/en/docs/)

---

## 11. 변경 이력

### 2025-10-26 (최신)
- ✅ **Monitoring Service 추가**: 시스템/서비스/DB 모니터링, 로그 수집 (포트 8085)
- ✅ **주변 장소 찾기 기능 개선**: 
  - 지도 범위 기반 동적 필터링 (검색 반경 입력 제거)
  - 지도 이동 시 자동으로 장소 갱신 (debounce 적용)
  - 카테고리 필터링 버그 수정 (`categoryId` 전달)
- ✅ **커뮤니티 게시글 작성 개선**: Place DB 장소 선택 (Google Places API 대신)
- ✅ **관리자 페이지 개선**: 지도 클릭으로 위도/경도/주소 자동 입력 (Geocoding)
- ✅ **프론트엔드 서비스 분리**: `monitoringService.ts` 독립 (기존 `adminService.ts`에서 분리)
- ✅ **Docker Compose 최적화**: DB를 호스트(`10.0.2.15`)에서 실행, 컨테이너는 마이크로서비스만
- ✅ **Gateway 라우팅 추가**: `/api/categories/**`, `/api/monitoring/**`
- ✅ **네비게이션 바 변경**: "지도 테스트" → "주변 장소 찾기"

### 2025-10-25
- 초기 프로젝트 구축 (MSA, Gateway, User/Place/Community Service)
- GitHub Actions CI/CD 설정
- Nginx + Ngrok 배포 환경 구성

---

**마지막 업데이트**: 2025년 10월 26일
