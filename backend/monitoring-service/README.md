# Monitoring Service

시스템 및 서비스 모니터링을 담당하는 독립적인 마이크로서비스입니다.

## 📋 주요 기능

- **시스템 메트릭**: CPU, 메모리, 디스크 사용량 모니터링
- **서비스 메트릭**: Docker 컨테이너별 리소스 사용량 추적
- **데이터베이스 메트릭**: MySQL, PostgreSQL 연결 및 상태 모니터링
- **시스템 로그**: 실시간 로그 수집 및 필터링

## 🚀 실행 방법

### 로컬 개발

```bash
# Maven으로 빌드 및 실행
cd backend/monitoring-service
mvn clean package
mvn spring-boot:run
```

### Docker로 실행

```bash
# Docker 이미지 빌드
docker build -t monitoring-service:latest .

# 컨테이너 실행 (Docker 소켓 마운트 필수)
docker run -d \
  --name monitoring-service \
  -p 8085:8085 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  monitoring-service:latest
```

### Docker Compose로 실행

```bash
cd docker
docker-compose up -d monitoring-service
```

## 📡 API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/monitoring/system/metrics` | 시스템 메트릭 조회 |
| GET | `/api/monitoring/services/metrics` | 서비스 메트릭 조회 |
| GET | `/api/monitoring/databases/metrics` | 데이터베이스 메트릭 조회 |
| GET | `/api/monitoring/logs` | 시스템 로그 조회 |
| GET | `/api/monitoring/health` | Health Check |

### 예시

```bash
# 시스템 메트릭 조회
curl http://localhost:8085/api/monitoring/system/metrics

# 서비스 메트릭 조회
curl http://localhost:8085/api/monitoring/services/metrics

# 로그 조회 (필터링)
curl "http://localhost:8085/api/monitoring/logs?limit=50&service=gateway&level=ERROR"
```

## ⚙️ 설정

### 필수 요구사항

- Java 17+
- Docker (컨테이너 메트릭 수집용)
- Docker 소켓 접근 권한 (`/var/run/docker.sock`)

### 환경 변수

```yaml
SERVER_PORT: 8085                    # 서비스 포트
SPRING_PROFILES_ACTIVE: docker       # 프로파일
```

## 🔒 권한

**중요**: 이 서비스는 Docker 소켓(`/var/run/docker.sock`)에 접근해야 합니다.

```bash
# Docker 소켓 권한 확인
ls -l /var/run/docker.sock

# 필요시 권한 부여
sudo chmod 666 /var/run/docker.sock
```

## 📊 모니터링 대상

현재 모니터링 중인 서비스:
- gateway-service (8080)
- user-service (8081)
- place-service (8082)
- community-service (8083)
- recommendation-service (8084)

## 🛠️ 개발

### 기술 스택

- Spring Boot 3.2.0
- Java 17
- Docker Java Client
- Spring Boot Actuator

### 프로젝트 구조

```
monitoring-service/
├── src/
│   ├── main/
│   │   ├── java/com/maltan/monitoring/
│   │   │   ├── controller/      # REST API
│   │   │   ├── service/         # 비즈니스 로직
│   │   │   ├── dto/             # 데이터 전송 객체
│   │   │   └── MonitoringServiceApplication.java
│   │   └── resources/
│   │       └── application.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## 🔄 CI/CD

GitHub Actions를 통한 자동 배포:

```yaml
# .github/workflows/deploy-monitoring.yml
on:
  push:
    branches: [main]
    paths:
      - 'backend/monitoring-service/**'
```

## 📝 로그

로그 레벨:
- ERROR: 오류 발생
- WARNING: 경고
- INFO: 일반 정보
- DEBUG: 디버그 정보

## 🐛 트러블슈팅

### Docker 소켓 접근 불가

```bash
# 오류: Permission denied: '/var/run/docker.sock'
# 해결: Docker 그룹에 사용자 추가
sudo usermod -aG docker $USER
```

### 컨테이너 메트릭이 0으로 표시

```bash
# 원인: Docker API 버전 불일치
# 해결: Docker 버전 확인 및 업데이트
docker --version
```

## 📄 라이선스

MIT License

