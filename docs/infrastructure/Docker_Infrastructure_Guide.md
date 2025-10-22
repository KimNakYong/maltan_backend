# Docker 기반 MSA 인프라 구축 가이드

## 🏗️ **구축된 인프라 구조**

### 📁 **프로젝트 구조**
```
maltan-project/
├── docker/
│   ├── docker-compose.yml          # 전체 서비스 오케스트레이션
│   ├── env.example                 # 환경 변수 예시
│   └── monitoring/
│       └── prometheus.yml          # 모니터링 설정
├── backend/
│   ├── user-service/
│   │   └── Dockerfile
│   ├── place-service/
│   │   └── Dockerfile
│   ├── recommendation-service/
│   │   └── Dockerfile
│   ├── community-service/
│   │   └── Dockerfile
│   └── gateway-service/
│       └── Dockerfile
└── frontend/
    ├── Dockerfile
    └── nginx.conf
```

## 🐳 **Docker Compose 서비스 구성**

### **데이터베이스 서비스**
- **PostgreSQL**: 메인 데이터베이스 (5개 DB 분리)
- **Redis**: 캐싱 및 세션 저장소

### **마이크로서비스 (5개)**
- **User Service** (8081): 사용자 관리 + 인증
- **Place Service** (8082): 장소 + 리뷰 관리
- **Recommendation Service** (8083): 추천 + 위치 검색
- **Community Service** (8084): 커뮤니티 + 알림
- **Gateway Service** (8080): API 게이트웨이

### **프론트엔드**
- **React App** (3000): 사용자 인터페이스

### **모니터링 (선택사항)**
- **Prometheus** (9090): 메트릭 수집
- **Grafana** (3001): 대시보드

## 🚀 **실행 방법**

### 1. 환경 설정
```bash
# 프로젝트 디렉토리로 이동
cd ~/maltan-project

# 환경 변수 파일 생성
cp docker/env.example docker/.env

# .env 파일 편집 (API 키 등 설정)
nano docker/.env
```

### 2. 전체 서비스 시작
```bash
# Docker Compose로 모든 서비스 시작
cd docker
docker-compose up -d

# 서비스 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f
```

### 3. 개별 서비스 관리
```bash
# 특정 서비스만 시작
docker-compose up user-service place-service

# 특정 서비스 재시작
docker-compose restart user-service

# 특정 서비스 중지
docker-compose stop user-service

# 서비스 로그 확인
docker-compose logs user-service
```

## 🔧 **서비스별 접근 URL**

### **API 엔드포인트**
- **API Gateway**: http://localhost:8080
- **User Service**: http://localhost:8081
- **Place Service**: http://localhost:8082
- **Recommendation Service**: http://localhost:8083
- **Community Service**: http://localhost:8084

### **프론트엔드**
- **React App**: http://localhost:3000

### **모니터링**
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001 (admin/admin123)

### **데이터베이스**
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

## 🗄️ **데이터베이스 구조**

### **PostgreSQL 데이터베이스 (5개 분리)**
```
maltan_db (메인)
├── user_db (사용자 데이터)
├── place_db (장소 + 리뷰 데이터)
├── recommendation_db (추천 데이터)
└── community_db (커뮤니티 데이터)
```

### **Redis 사용 용도**
- 세션 저장소
- 캐싱
- 실시간 알림
- 임시 데이터 저장

## 🌐 **네트워크 설정**

### **Docker 네트워크**
- **네트워크명**: maltan-network
- **서브넷**: 172.20.0.0/16
- **서비스 간 통신**: 컨테이너명으로 접근

### **포트 포워딩**
```
호스트 포트 → 컨테이너 포트
8080 → Gateway Service
8081 → User Service
8082 → Place Service
8083 → Recommendation Service
8084 → Community Service
3000 → Frontend
5432 → PostgreSQL
6379 → Redis
9090 → Prometheus
3001 → Grafana
```

## 📊 **모니터링 설정**

### **Prometheus 설정**
- 모든 서비스의 메트릭 수집
- 15초 간격으로 스크래핑
- 헬스체크 및 성능 메트릭

### **Grafana 설정**
- Prometheus 데이터 시각화
- 서비스별 대시보드
- 알림 설정

## 🔍 **개발 워크플로우**

### **1. 개발 환경**
```bash
# 개발용 서비스만 시작
docker-compose up postgres redis user-service

# 특정 서비스 개발
cd backend/user-service
# 코드 수정 후
docker-compose restart user-service
```

### **2. 테스트 환경**
```bash
# 전체 서비스 시작
docker-compose up -d

# 테스트 실행
curl http://localhost:8080/health
```

### **3. 프로덕션 환경**
```bash
# 프로덕션 설정으로 시작
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## 🛠️ **문제 해결**

### **일반적인 문제들**

#### **1. 포트 충돌**
```bash
# 포트 사용 확인
netstat -tlnp | grep :8080

# Docker 프로세스 확인
docker ps
```

#### **2. 서비스 시작 실패**
```bash
# 서비스 로그 확인
docker-compose logs user-service

# 컨테이너 상태 확인
docker-compose ps
```

#### **3. 데이터베이스 연결 실패**
```bash
# PostgreSQL 연결 테스트
docker-compose exec postgres psql -U maltan_user -d maltan_db

# Redis 연결 테스트
docker-compose exec redis redis-cli ping
```

### **유용한 명령어**
```bash
# 전체 서비스 상태 확인
docker-compose ps

# 서비스 로그 실시간 확인
docker-compose logs -f

# 특정 서비스 재시작
docker-compose restart service-name

# 볼륨 확인
docker volume ls

# 네트워크 확인
docker network ls

# 리소스 사용량 확인
docker stats
```

## 📊 **리소스 할당 (4GB RAM, 2 CPU 최적화)**

### **서비스별 리소스 할당**
```
전체 메모리 사용량: 2.3GB (최대)
├── PostgreSQL: 512MB
├── Redis: 128MB
├── 마이크로서비스 (5개): 1.28GB (256MB × 5)
├── Frontend: 128MB
├── 모니터링: 256MB
└── 시스템 여유: 1.7GB

전체 CPU 사용량: 2.5 코어 (최대)
├── PostgreSQL: 0.5 코어
├── Redis: 0.25 코어
├── 마이크로서비스 (5개): 1.5 코어 (0.3 × 5)
├── Frontend: 0.2 코어
├── 모니터링: 0.4 코어
└── 시스템 여유: 0.5 코어
```

### **효율적인 개발 방법**
```bash
# 개발 시: 필요한 서비스만
docker-compose up postgres redis user-service frontend

# 테스트 시: 전체 서비스
docker-compose up -d

# 리소스 모니터링
docker stats
```

## 🎯 **다음 단계**

인프라 구축이 완료되면:
1. **첫 번째 마이크로서비스 개발** (User Service)
2. **데이터베이스 스키마 구현**
3. **API 엔드포인트 개발**
4. **프론트엔드 기본 구조 설정**

이제 4GB RAM, 2 CPU 환경에 최적화된 Docker 기반 MSA 인프라가 준비되었습니다! 🚀
