# Maltan Backend Services

우리동네 백엔드 마이크로서비스 - Monorepo

## 🏗️ 서비스 구조

```
maltan-backend/
├── backend/
│   ├── user-service/         # 유저 관리 서비스 (Port: 8081)
│   ├── community-service/    # 커뮤니티 서비스 (Port: 8083)
│   ├── place-service/        # 장소 서비스 (Port: 8082)
│   ├── gateway-service/      # API Gateway (Port: 8080)
│   └── recommendation-service/ # 추천 서비스 (Port: 8084)
└── docs/                     # 문서
```

## 🚀 CI/CD

각 서비스는 독립적으로 배포됩니다:

- **Community Service 수정** → `deploy-community.yml` 실행
- **User Service 수정** → `deploy-user.yml` 실행
- **Place Service 수정** → `deploy-place.yml` 실행
- **Gateway Service 수정** → `deploy-gateway.yml` 실행

### 배포 트리거

```bash
# Community Service 배포
git add backend/community-service
git commit -m "feat: 커뮤니티 기능 추가"
git push origin main

# → deploy-community.yml 자동 실행
# → Ubuntu 서버에 자동 배포
```

## 📚 문서

- [커뮤니티 서비스 설계](docs/COMMUNITY_SERVICE_DESIGN.md)
- [PostgreSQL 설정 가이드](docs/POSTGRESQL_SETUP_GUIDE.md)
- [Monorepo CI/CD 가이드](docs/MONOREPO_CI_CD_GUIDE.md)
- [데이터베이스 전략](docs/DATABASE_STRATEGY_GUIDE.md)

## 🔧 로컬 개발

각 서비스 디렉토리에서:

```bash
cd backend/community-service
mvn spring-boot:run
```

## 🐳 Docker

```bash
# 개별 서비스 빌드
cd backend/community-service
docker build -t community-service:latest .

# 실행
docker run -p 8083:8083 community-service:latest
```

## 👥 팀 협업

1. Feature 브랜치 생성
```bash
git checkout -b feature/your-service-name
```

2. 자신의 서비스 폴더만 수정
```bash
# 예: Community Service
git add backend/community-service
git commit -m "feat: 새 기능 추가"
```

3. Push & Pull Request
```bash
git push origin feature/your-service-name
```

4. main 브랜치에 merge되면 자동 배포

## 📊 서비스 포트

| 서비스 | 포트 | 용도 |
|--------|------|------|
| Gateway | 8080 | API Gateway |
| User | 8081 | 유저 관리 |
| Place | 8082 | 장소 관리 |
| Community | 8083 | 커뮤니티 |
| Recommendation | 8084 | 추천 시스템 |

## 🗄️ 데이터베이스

- **PostgreSQL 15+**
- **Redis** (캐싱)

각 서비스별 독립 데이터베이스:
- `user_db`
- `community_db`
- `place_db`
- `recommendation_db`

---

**프로젝트**: Maltan (우리동네)  
**버전**: 1.0.0
