# Git 저장소 재구성 가이드 (MSA)

## 📋 개요

각 마이크로서비스를 독립적인 Git 저장소로 분리하여 관리합니다.

---

## 🏗️ 저장소 구조

### 서비스별 독립 저장소

| 서비스 | GitHub 저장소 | 담당 개발자 |
|--------|--------------|------------|
| User Service | `maltan-user-service` | 개발자 A |
| Community Service | `maltan-community-service` | 개발자 B |
| Place Service | `maltan-place-service` | 개발자 C |
| Gateway Service | `maltan-gateway-service` | 개발자 D |
| Recommendation Service | `maltan-recommendation-service` | 개발자 E |
| Frontend | `maltan-frontend` | 개발자 F |

### 공통 저장소 (선택)

| 저장소 | 용도 |
|--------|------|
| `maltan-infrastructure` | Docker Compose, 환경 설정 |
| `maltan-docs` | 공통 문서, API 명세 |

---

## 🚀 1. GitHub에 저장소 생성

각 서비스마다 GitHub에 새 저장소를 생성합니다:

1. GitHub 로그인
2. **New repository** 클릭
3. 저장소 이름:
   - `maltan-user-service`
   - `maltan-community-service`
   - `maltan-place-service`
   - `maltan-gateway-service`
   - `maltan-recommendation-service`
4. **Public** 또는 **Private** 선택
5. **Create repository**

---

## 📁 2. Windows에서 각 서비스 Git 초기화

### Community Service 예시

```powershell
# 1. 커뮤니티 서비스 디렉토리로 이동
cd F:\3project\maltan-backend\backend\community-service

# 2. Git 초기화
git init

# 3. 원격 저장소 연결
git remote add origin https://github.com/KimNakYong/maltan-community-service.git

# 4. 모든 파일 추가
git add .

# 5. 커밋
git commit -m "feat: 커뮤니티 서비스 초기 구현

- 게시글 CRUD
- 댓글 시스템
- 추천/비추천
- 모집 기능
- CI/CD 설정"

# 6. 브랜치를 main으로 변경
git branch -M main

# 7. Push
git push -u origin main
```

### User Service

```powershell
cd F:\3project\maltan-backend\backend\user-service

git init
git remote add origin https://github.com/KimNakYong/maltan-user-service.git
git add .
git commit -m "feat: 유저 서비스 초기 구현"
git branch -M main
git push -u origin main
```

### Place Service

```powershell
cd F:\3project\maltan-backend\backend\place-service

git init
git remote add origin https://github.com/KimNakYong/maltan-place-service.git
git add .
git commit -m "feat: 장소 서비스 초기 구현"
git branch -M main
git push -u origin main
```

### Gateway Service

```powershell
cd F:\3project\maltan-backend\backend\gateway-service

git init
git remote add origin https://github.com/KimNakYong/maltan-gateway-service.git
git add .
git commit -m "feat: 게이트웨이 서비스 초기 구현"
git branch -M main
git push -u origin main
```

### Recommendation Service

```powershell
cd F:\3project\maltan-backend\backend\recommendation-service

git init
git remote add origin https://github.com/KimNakYong/maltan-recommendation-service.git
git add .
git commit -m "feat: 추천 서비스 초기 구현"
git branch -M main
git push -u origin main
```

---

## 🖥️ 3. Ubuntu 서버 디렉토리 구조 변경

### 기존 구조 삭제 (선택)

```bash
# 백업
cd ~
mv maltan-project maltan-project-backup

# 새 디렉토리 생성
mkdir -p ~/maltan-project
cd ~/maltan-project
```

### 각 서비스 클론

```bash
cd ~/maltan-project

# User Service
git clone https://github.com/KimNakYong/maltan-user-service.git

# Community Service
git clone https://github.com/KimNakYong/maltan-community-service.git

# Place Service
git clone https://github.com/KimNakYong/maltan-place-service.git

# Gateway Service
git clone https://github.com/KimNakYong/maltan-gateway-service.git

# Recommendation Service
git clone https://github.com/KimNakYong/maltan-recommendation-service.git

# Frontend
git clone https://github.com/KimNakYong/maltan-frontend.git
```

### 디렉토리 확인

```bash
ls -la ~/maltan-project

# 출력:
# drwxr-xr-x maltan-user-service
# drwxr-xr-x maltan-community-service
# drwxr-xr-x maltan-place-service
# drwxr-xr-x maltan-gateway-service
# drwxr-xr-x maltan-recommendation-service
# drwxr-xr-x maltan-frontend
```

---

## ⚙️ 4. 각 서비스별 CI/CD 설정

각 서비스의 `.github/workflows/deploy.yml` 파일을 수정합니다:

### Community Service

```yaml
# .github/workflows/deploy.yml
script: |
  cd ~/maltan-project/maltan-community-service
  git pull origin main
  mvn clean package -DskipTests
  docker build -t community-service:latest .
  # ...
```

### User Service

```yaml
# .github/workflows/deploy.yml
script: |
  cd ~/maltan-project/maltan-user-service
  git pull origin main
  mvn clean package -DskipTests
  docker build -t user-service:latest .
  # ...
```

### 각 서비스마다 동일한 방식으로 경로만 변경

---

## 🔐 5. GitHub Secrets 설정 (각 저장소마다)

각 서비스 저장소마다 **Settings → Secrets and variables → Actions**에서 다음을 추가:

| Secret 이름 | 값 |
|------------|-----|
| `SERVER_HOST` | `10.0.2.15` |
| `SERVER_USER` | `root` |
| `SERVER_PASSWORD` | 서버 비밀번호 |
| `SERVER_PORT` | `22` |

---

## 🐳 6. Docker Compose 통합 관리

### Infrastructure 저장소 생성 (선택)

공통 설정을 위한 별도 저장소:

```bash
# GitHub에 maltan-infrastructure 저장소 생성

# Windows에서
cd F:\3project
mkdir maltan-infrastructure
cd maltan-infrastructure

git init
git remote add origin https://github.com/KimNakYong/maltan-infrastructure.git
```

### docker-compose.yml 생성

```yaml
# ~/maltan-project/docker-compose.yml
version: '3.8'

services:
  # PostgreSQL
  postgres:
    image: postgres:15-alpine
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - maltan-network

  # Redis
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - maltan-network

  # Gateway Service
  gateway-service:
    image: gateway-service:latest
    ports:
      - "8080:8080"
    environment:
      - SERVER_PORT=8080
    networks:
      - maltan-network
    depends_on:
      - user-service
      - community-service

  # User Service
  user-service:
    image: user-service:latest
    ports:
      - "8081:8081"
    environment:
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=user_db
      - REDIS_HOST=redis
    networks:
      - maltan-network
    depends_on:
      - postgres
      - redis

  # Community Service
  community-service:
    image: community-service:latest
    ports:
      - "8083:8083"
    environment:
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=community_db
      - REDIS_HOST=redis
    networks:
      - maltan-network
    depends_on:
      - postgres
      - redis

  # Place Service
  place-service:
    image: place-service:latest
    ports:
      - "8082:8082"
    environment:
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=place_db
      - REDIS_HOST=redis
    networks:
      - maltan-network
    depends_on:
      - postgres
      - redis

  # Frontend
  frontend:
    image: maltan-frontend:latest
    ports:
      - "3000:3000"
    networks:
      - maltan-network
    depends_on:
      - gateway-service

volumes:
  postgres-data:

networks:
  maltan-network:
    driver: bridge
```

---

## 👥 7. 팀 협업 워크플로우

### 개발자 A (User Service)

```bash
# 클론
git clone https://github.com/KimNakYong/maltan-user-service.git
cd maltan-user-service

# 개발
# ... 코드 수정 ...

# Commit & Push
git add .
git commit -m "feat: 로그인 기능 추가"
git push origin main

# 자동 배포됨!
```

### 개발자 B (Community Service)

```bash
# 클론
git clone https://github.com/KimNakYong/maltan-community-service.git
cd maltan-community-service

# 개발
# ... 코드 수정 ...

# Commit & Push
git add .
git commit -m "feat: 댓글 알림 기능 추가"
git push origin main

# 자동 배포됨!
```

---

## 🔄 8. 전체 시스템 업데이트

### Ubuntu 서버에서 모든 서비스 업데이트

```bash
#!/bin/bash
# update-all-services.sh

cd ~/maltan-project

# 모든 서비스 업데이트
for service in maltan-*; do
    echo "Updating $service..."
    cd $service
    git pull origin main
    cd ..
done

# Docker Compose로 재시작
docker-compose down
docker-compose up -d --build
```

---

## 📊 9. 장점

### 독립 저장소의 장점

1. **독립적 개발**
   - 각 팀이 자신의 서비스만 관리
   - 다른 서비스 코드 변경 영향 없음

2. **독립적 배포**
   - 서비스별 CI/CD 파이프라인
   - 한 서비스 배포가 다른 서비스에 영향 없음

3. **접근 권한 관리**
   - 서비스별 GitHub 팀 권한 설정
   - 민감한 코드 보호

4. **버전 관리**
   - 서비스별 독립 버전
   - 서비스별 릴리즈 노트

5. **코드 리뷰**
   - 서비스별 PR 관리
   - 담당자 명확

---

## 🎯 10. 다음 단계

### Phase 1: 저장소 분리
- [x] GitHub에 각 서비스 저장소 생성
- [ ] Windows에서 각 서비스 Git 초기화
- [ ] Ubuntu 서버에 각 서비스 클론

### Phase 2: CI/CD 설정
- [ ] 각 저장소에 GitHub Secrets 추가
- [ ] 각 서비스 deploy.yml 경로 수정
- [ ] 자동 배포 테스트

### Phase 3: 통합 관리
- [ ] docker-compose.yml 작성
- [ ] 전체 서비스 통합 테스트
- [ ] 모니터링 설정

---

**작성일**: 2025-10-24  
**버전**: 1.0.0

