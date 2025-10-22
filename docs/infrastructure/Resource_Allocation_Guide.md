# 리소스 할당 가이드 (4GB RAM, 2 CPU)

## 🖥️ **가상머신 스펙**
- **RAM**: 4GB
- **CPU**: 2 코어
- **Storage**: 30GB

## 📊 **서비스별 리소스 할당**

### **데이터베이스 서비스**
| 서비스 | 메모리 | CPU | 용도 |
|--------|--------|-----|------|
| **PostgreSQL** | 512MB (최대) / 256MB (최소) | 0.5 코어 | 메인 데이터베이스 |
| **Redis** | 128MB (최대) / 64MB (최소) | 0.25 코어 | 캐싱 및 세션 |

### **마이크로서비스 (5개)**
| 서비스 | 메모리 | CPU | 용도 |
|--------|--------|-----|------|
| **User Service** | 256MB (최대) / 128MB (최소) | 0.3 코어 | 사용자 관리 + 인증 |
| **Place Service** | 256MB (최대) / 128MB (최소) | 0.3 코어 | 장소 + 리뷰 관리 |
| **Recommendation Service** | 256MB (최대) / 128MB (최소) | 0.3 코어 | 추천 + 위치 검색 |
| **Community Service** | 256MB (최대) / 128MB (최소) | 0.3 코어 | 커뮤니티 + 알림 |
| **Gateway Service** | 256MB (최대) / 128MB (최소) | 0.3 코어 | API 게이트웨이 |

### **프론트엔드**
| 서비스 | 메모리 | CPU | 용도 |
|--------|--------|-----|------|
| **Frontend** | 128MB (최대) / 64MB (최소) | 0.2 코어 | React 앱 |

### **모니터링 (선택사항)**
| 서비스 | 메모리 | CPU | 용도 |
|--------|--------|-----|------|
| **Prometheus** | 128MB (최대) / 64MB (최소) | 0.2 코어 | 메트릭 수집 |
| **Grafana** | 128MB (최대) / 64MB (최소) | 0.2 코어 | 대시보드 |

## 📈 **리소스 사용량 분석**

### **전체 메모리 사용량**
```
최대 사용량: 2.3GB
├── PostgreSQL: 512MB
├── Redis: 128MB
├── 마이크로서비스 (5개): 1.28GB (256MB × 5)
├── Frontend: 128MB
├── 모니터링: 256MB (128MB × 2)
└── 시스템 여유: 1.7GB
```

### **전체 CPU 사용량**
```
최대 사용량: 2.5 코어
├── PostgreSQL: 0.5 코어
├── Redis: 0.25 코어
├── 마이크로서비스 (5개): 1.5 코어 (0.3 × 5)
├── Frontend: 0.2 코어
├── 모니터링: 0.4 코어 (0.2 × 2)
└── 시스템 여유: 0.5 코어
```

## ⚡ **최적화 전략**

### **1. 단계별 서비스 시작**
```bash
# 1단계: 기본 인프라만
docker-compose up postgres redis

# 2단계: 핵심 서비스 추가
docker-compose up user-service place-service

# 3단계: 전체 서비스 시작
docker-compose up -d
```

### **2. 개발 환경 최적화**
```bash
# 개발 시 필요한 서비스만 시작
docker-compose up postgres redis user-service frontend

# 테스트 시에만 전체 서비스 시작
docker-compose up -d
```

### **3. 리소스 모니터링**
```bash
# 리소스 사용량 확인
docker stats

# 특정 서비스 리소스 확인
docker stats maltan-user-service
```

## 🔧 **Docker Compose 리소스 설정**

### **리소스 제한 설정 예시**
```yaml
services:
  user-service:
    deploy:
      resources:
        limits:
          memory: 256M
          cpus: '0.3'
        reservations:
          memory: 128M
          cpus: '0.1'
```

### **메모리 최적화 설정**
```yaml
# JVM 힙 메모리 설정
environment:
  - JAVA_OPTS=-Xms128m -Xmx256m
  - SPRING_PROFILES_ACTIVE=docker
```

## 📊 **성능 모니터링**

### **리소스 사용량 확인 명령어**
```bash
# 전체 컨테이너 리소스 사용량
docker stats

# 특정 서비스 리소스 사용량
docker stats maltan-user-service

# 메모리 사용량 상세 확인
docker exec maltan-user-service cat /proc/meminfo

# CPU 사용량 확인
docker exec maltan-user-service top
```

### **리소스 부족 시 대응**
```bash
# 메모리 부족 시
docker-compose restart user-service

# CPU 부족 시
docker-compose scale user-service=1

# 불필요한 서비스 중지
docker-compose stop prometheus grafana
```

## 🎯 **개발 워크플로우 최적화**

### **1. 개발 단계별 리소스 할당**
```
Phase 1: 기본 인프라 (1GB)
├── PostgreSQL: 512MB
├── Redis: 128MB
└── 시스템: 360MB

Phase 2: 핵심 서비스 (2GB)
├── User Service: 256MB
├── Place Service: 256MB
└── Frontend: 128MB

Phase 3: 전체 서비스 (3.5GB)
├── 모든 마이크로서비스: 1.28GB
├── 모니터링: 256MB
└── 시스템 여유: 1.5GB
```

### **2. 효율적인 개발 방법**
```bash
# 개발 시: 필요한 서비스만
docker-compose up postgres redis user-service frontend

# 테스트 시: 전체 서비스
docker-compose up -d

# 프로덕션 시: 모니터링 포함
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## 🚨 **주의사항**

### **리소스 부족 시 증상**
- 서비스 시작 실패
- 응답 속도 저하
- 메모리 부족 오류
- CPU 사용률 100%

### **대응 방법**
1. **불필요한 서비스 중지**
2. **리소스 제한 조정**
3. **서비스 순차 시작**
4. **모니터링 서비스 비활성화**

이제 4GB RAM, 2 CPU 환경에 최적화된 리소스 할당이 완료되었습니다! 🚀
