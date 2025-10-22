# Maltan Backend

## 🏗️ **아키텍처**

### **MSA 구조 (5개 서비스)**
- **User Service**: 사용자 관리 + 인증
- **Place Service**: 장소 + 리뷰 관리
- **Recommendation Service**: 추천 + 위치 검색
- **Community Service**: 커뮤니티 + 알림
- **Gateway Service**: API 게이트웨이

### **기술 스택**
- **언어**: Java 17 + Spring Boot 3.x
- **데이터베이스**: PostgreSQL + Redis
- **인프라**: Docker + Docker Compose
- **인증**: JWT

## 🚀 **빠른 시작**

### **1. 환경 설정**
```bash
# 환경 변수 설정
cp docker/env.example docker/.env
# .env 파일에서 API 키 등 설정
```

### **2. 서비스 시작**
```bash
# 전체 서비스 시작
cd docker
docker-compose up -d

# 서비스 상태 확인
docker-compose ps
```

### **3. 접속 URL**
- **API Gateway**: http://localhost:8080
- **User Service**: http://localhost:8081
- **Place Service**: http://localhost:8082
- **Recommendation Service**: http://localhost:8083
- **Community Service**: http://localhost:8084

## 📁 **프로젝트 구조**

```
maltan-backend/
├── backend/                    # 백엔드 서비스들
│   ├── user-service/
│   ├── place-service/
│   ├── recommendation-service/
│   ├── community-service/
│   └── gateway-service/
├── docker/                     # Docker 설정
│   ├── docker-compose.yml
│   ├── init-scripts/
│   └── monitoring/
├── docs/                       # 백엔드 관련 문서
│   ├── architecture/
│   ├── infrastructure/
│   └── development/
└── README.md
```

## 🔧 **개발 환경**

### **요구사항**
- **RAM**: 4GB (최소)
- **CPU**: 2 코어 (최소)
- **Storage**: 30GB (최소)
- **OS**: Ubuntu 22.04 LTS (권장)

### **개발 도구**
- Docker & Docker Compose
- Java 17
- Git

## 📊 **리소스 할당**

### **서비스별 리소스**
| 서비스 | 메모리 | CPU | 용도 |
|--------|--------|-----|------|
| PostgreSQL | 512MB | 0.5 코어 | 메인 데이터베이스 |
| Redis | 128MB | 0.25 코어 | 캐싱 및 세션 |
| User Service | 256MB | 0.3 코어 | 사용자 관리 |
| Place Service | 256MB | 0.3 코어 | 장소 관리 |
| Recommendation Service | 256MB | 0.3 코어 | 추천 시스템 |
| Community Service | 256MB | 0.3 코어 | 커뮤니티 |
| Gateway Service | 256MB | 0.3 코어 | API 게이트웨이 |

## 🛠️ **개발 워크플로우**

### **브랜치 전략**
```
main (메인 브랜치)
├── develop (개발 브랜치)
├── feature/user-service-auth
├── feature/place-service-review
├── feature/recommendation-algorithm
├── feature/community-board
└── feature/gateway-routing
```

### **커밋 규칙**
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 스타일 변경
refactor: 코드 리팩토링
test: 테스트 추가/수정
chore: 빌드/설정 변경
```

### **서비스별 개발**
```bash
# User Service 개발
git checkout -b feature/user-service-auth
# 개발 작업...
git add backend/user-service/
git commit -m "feat(user-service): implement JWT authentication"
```

## 📚 **문서**

### **🏗️ Architecture (아키텍처)**
- [MSA 아키텍처 설계](./docs/architecture/MSA_Architecture_Plan.md)
- [MSA 배포 전략](./docs/architecture/MSA_Deployment_Strategy.md)

### **🔧 Infrastructure (인프라)**
- [Docker 인프라 가이드](./docs/infrastructure/Docker_Infrastructure_Guide.md)
- [리소스 할당 가이드](./docs/infrastructure/Resource_Allocation_Guide.md)
- [VirtualBox Ubuntu 설정](./docs/infrastructure/VirtualBox_Ubuntu_Setup_Guide.md)

### **💻 Development (개발)**
- [SSH 기반 개발 워크플로우](./docs/development/SSH_Development_Workflow.md)
- [Git 저장소 전략](./docs/development/Git_Repository_Strategy.md)

### **📖 전체 문서**
- [문서 인덱스](./docs/README.md)

## 🔍 **모니터링**

### **서비스 상태 확인**
```bash
# 전체 서비스 상태
docker-compose ps

# 특정 서비스 로그
docker-compose logs user-service

# 리소스 사용량
docker stats
```

### **헬스체크**
```bash
# API Gateway 헬스체크
curl http://localhost:8080/actuator/health

# User Service 헬스체크
curl http://localhost:8081/actuator/health
```

## 🚨 **문제 해결**

### **일반적인 문제들**
1. **포트 충돌**: `netstat -tlnp | grep :8080`
2. **메모리 부족**: `docker stats`로 확인
3. **서비스 시작 실패**: `docker-compose logs <service-name>`

### **유용한 명령어**
```bash
# 서비스 재시작
docker-compose restart user-service

# 특정 서비스만 시작
docker-compose up user-service

# 전체 서비스 중지
docker-compose down
```

## 👥 **개발팀**

- **개발자 A**: User Service + Place Service + Gateway Service
- **개발자 B**: Recommendation Service + Community Service

## 📄 **라이선스**

MIT License

## 🤝 **기여하기**

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

**우리동네 소개 서비스 백엔드 API를 개발하세요!** 🚀
