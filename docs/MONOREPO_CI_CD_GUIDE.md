# Monorepo CI/CD 가이드

## 📋 개요

단일 Backend 저장소(Monorepo)에서 각 서비스별로 독립적인 CI/CD 파이프라인을 구성합니다.

---

## 🏗️ 저장소 구조

```
maltan-backend/ (단일 Git 저장소)
├── .github/
│   └── workflows/
│       ├── deploy-community.yml    # 커뮤니티 서비스 배포
│       ├── deploy-user.yml         # 유저 서비스 배포
│       ├── deploy-place.yml        # 장소 서비스 배포
│       ├── deploy-gateway.yml      # 게이트웨이 서비스 배포
│       └── deploy-recommendation.yml
├── backend/
│   ├── user-service/
│   ├── community-service/
│   ├── place-service/
│   ├── gateway-service/
│   └── recommendation-service/
└── docs/
```

---

## 🎯 핵심 기능

### 1. 변경 감지 (Path Filtering)

각 서비스는 **해당 폴더의 변경사항이 있을 때만** 배포됩니다:

```yaml
on:
  push:
    branches:
      - main
    paths:
      - 'backend/community-service/**'  # 이 폴더 변경 시에만 실행
      - '.github/workflows/deploy-community.yml'
```

### 2. 독립적 배포

- User Service 코드 변경 → User Service만 배포
- Community Service 코드 변경 → Community Service만 배포
- 다른 서비스에 영향 없음

---

## 🚀 Ubuntu 서버 설정

### 1. 저장소 클론

```bash
# maltan-backend 저장소 클론
cd ~/maltan-project
git clone https://github.com/KimNakYong/maltan_backend.git maltan-backend

# 디렉토리 확인
cd maltan-backend
ls -la backend/
```

### 2. Maven 및 Docker 준비

```bash
# Maven 설치 확인
mvn --version

# Docker 네트워크 생성
docker network create maltan-network || echo "Already exists"
```

---

## 🔐 GitHub Secrets 설정

**maltan-backend 저장소**의 Settings → Secrets에 추가:

| Secret 이름 | 값 |
|------------|-----|
| `SERVER_HOST` | `10.0.2.15` |
| `SERVER_USER` | `root` |
| `SERVER_PASSWORD` | Ubuntu 서버 비밀번호 |
| `SERVER_PORT` | `22` |

---

## 📝 사용 시나리오

### 시나리오 1: 커뮤니티 서비스만 수정

```bash
# Windows에서
cd F:\3project\maltan-backend

# Community Service 코드 수정
# backend/community-service/src/main/java/...

# Commit & Push
git add backend/community-service
git commit -m "feat: 커뮤니티 서비스 댓글 기능 추가"
git push origin main

# ✅ deploy-community.yml만 실행됨
# ❌ 다른 서비스는 배포 안 됨
```

### 시나리오 2: 여러 서비스 동시 수정

```bash
# User Service와 Community Service 동시 수정
git add backend/user-service backend/community-service
git commit -m "feat: 유저 프로필과 커뮤니티 연동"
git push origin main

# ✅ deploy-user.yml 실행됨
# ✅ deploy-community.yml 실행됨
# ❌ 다른 서비스는 배포 안 됨
```

### 시나리오 3: 문서만 수정

```bash
# 문서만 수정
git add docs/
git commit -m "docs: API 문서 업데이트"
git push origin main

# ❌ 모든 배포 워크플로우 실행 안 됨 (코드 변경 없음)
```

---

## 👥 팀 협업 워크플로우

### 개발자 A (User Service 담당)

```bash
# 1. 저장소 클론
git clone https://github.com/KimNakYong/maltan_backend.git
cd maltan_backend

# 2. Feature 브랜치 생성
git checkout -b feature/user-login

# 3. User Service 코드 수정
# backend/user-service/...

# 4. Commit
git add backend/user-service
git commit -m "feat: 로그인 기능 추가"

# 5. Push
git push origin feature/user-login

# 6. GitHub에서 Pull Request 생성

# 7. Merge 후 자동 배포됨
```

### 개발자 B (Community Service 담당)

```bash
git clone https://github.com/KimNakYong/maltan_backend.git
cd maltan_backend

git checkout -b feature/comment-notification

# Community Service 코드 수정
# backend/community-service/...

git add backend/community-service
git commit -m "feat: 댓글 알림 기능 추가"
git push origin feature/comment-notification

# PR 생성 → Merge → 자동 배포
```

**중요**: 각 개발자는 자신의 서비스 폴더만 수정합니다!

---

## 🔄 수동 배포

특정 서비스를 수동으로 배포하려면:

1. GitHub 저장소의 **Actions** 탭
2. 원하는 워크플로우 선택 (예: Deploy Community Service)
3. **Run workflow** 클릭
4. 브랜치 선택 (main)
5. **Run workflow** 버튼 클릭

---

## 📊 모니터링

### 배포 상태 확인

```bash
# Ubuntu 서버에서
docker ps

# 출력 예시:
# user-service          Up 2 hours    0.0.0.0:8081->8081/tcp
# community-service     Up 1 hour     0.0.0.0:8083->8083/tcp
# place-service         Up 3 hours    0.0.0.0:8082->8082/tcp
# gateway-service       Up 4 hours    0.0.0.0:8080->8080/tcp
```

### 로그 확인

```bash
# 특정 서비스 로그
docker logs -f community-service

# 모든 서비스 로그 동시 확인
docker-compose logs -f
```

---

## 🎨 장점 vs 단점

### ✅ Monorepo 장점

1. **코드 공유 용이**
   - 공통 모듈, 라이브러리 공유
   - 일관된 코드 스타일

2. **통합 관리**
   - 하나의 저장소로 전체 관리
   - 버전 동기화 쉬움

3. **리팩토링 편리**
   - 여러 서비스 동시 수정
   - 한 번의 PR로 여러 서비스 업데이트

4. **초기 설정 간단**
   - 저장소 하나만 관리
   - GitHub Secrets 한 번만 설정

### ❌ Monorepo 단점

1. **저장소 크기**
   - 모든 서비스 코드가 한 곳에
   - 클론 시간 길어질 수 있음

2. **빌드 시간**
   - 큰 저장소는 빌드 느릴 수 있음
   - (Path filtering으로 완화)

3. **권한 관리**
   - 서비스별 접근 제어 어려움
   - 모든 개발자가 전체 코드 접근

---

## 🔧 고급 설정

### 1. Docker Compose 통합

```yaml
# maltan-backend/docker-compose.yml
version: '3.8'

services:
  user-service:
    build: ./backend/user-service
    ports:
      - "8081:8081"
    environment:
      - DB_HOST=postgres
    networks:
      - maltan-network

  community-service:
    build: ./backend/community-service
    ports:
      - "8083:8083"
    environment:
      - DB_HOST=postgres
    networks:
      - maltan-network

  # ... 다른 서비스들
```

### 2. 공통 의존성 관리

```xml
<!-- maltan-backend/pom.xml (Parent POM) -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.maltan</groupId>
    <artifactId>maltan-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>backend/user-service</module>
        <module>backend/community-service</module>
        <module>backend/place-service</module>
        <module>backend/gateway-service</module>
    </modules>
    
    <!-- 공통 의존성 -->
</project>
```

---

## 📈 다음 단계

- [ ] .github/workflows/ 폴더에 각 서비스별 배포 파일 생성
- [ ] GitHub Secrets 설정
- [ ] Ubuntu 서버에 maltan-backend 저장소 클론
- [ ] 각 서비스 테스트 배포
- [ ] 모니터링 대시보드 설정

---

**작성일**: 2025-10-24  
**버전**: 1.0.0

