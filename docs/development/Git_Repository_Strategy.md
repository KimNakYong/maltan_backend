# Git 저장소 전략 및 구조 설계

## 🏗️ **Git 저장소 구조 옵션**

### **Option 1: 모노레포 (Monorepo) - 권장**
```
maltan-project/ (단일 저장소)
├── backend/
│   ├── user-service/
│   ├── place-service/
│   ├── recommendation-service/
│   ├── community-service/
│   └── gateway-service/
├── frontend/
├── docker/
├── docs/
└── .gitignore
```

**장점:**
- 통합 관리 용이
- 의존성 관리 간편
- 전체 프로젝트 일관성
- CI/CD 설정 단순

**단점:**
- 저장소 크기 증가
- 서비스별 독립성 제한

### **Option 2: 멀티레포 (Multi-repo)**
```
maltan-user-service/ (별도 저장소)
maltan-place-service/ (별도 저장소)
maltan-recommendation-service/ (별도 저장소)
maltan-community-service/ (별도 저장소)
maltan-gateway-service/ (별도 저장소)
maltan-frontend/ (별도 저장소)
maltan-infrastructure/ (별도 저장소)
```

**장점:**
- 서비스별 완전 독립성
- 팀별 권한 관리
- 서비스별 릴리즈 독립

**단점:**
- 관리 복잡도 증가
- 의존성 관리 어려움
- 통합 테스트 복잡

## 🎯 **권장 구조: 모노레포 + 서비스별 브랜치**

### **저장소 구조**
```
maltan-project/
├── .git/
├── .gitignore
├── README.md
├── docker-compose.yml
├── backend/
│   ├── user-service/
│   │   ├── src/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── README.md
│   ├── place-service/
│   │   ├── src/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── README.md
│   ├── recommendation-service/
│   ├── community-service/
│   └── gateway-service/
├── frontend/
│   ├── src/
│   ├── package.json
│   ├── Dockerfile
│   └── README.md
├── docker/
│   ├── docker-compose.yml
│   ├── init-scripts/
│   └── monitoring/
└── docs/
    ├── MSA_Architecture_Plan.md
    ├── Docker_Infrastructure_Guide.md
    └── Resource_Allocation_Guide.md
```

## 🌿 **브랜치 전략**

### **브랜치 구조**
```
main (메인 브랜치)
├── develop (개발 브랜치)
├── feature/user-service-auth (User Service 인증 기능)
├── feature/place-service-review (Place Service 리뷰 기능)
├── feature/recommendation-algorithm (추천 알고리즘)
├── feature/community-board (커뮤니티 게시판)
├── feature/gateway-routing (API Gateway 라우팅)
├── feature/frontend-map (프론트엔드 지도)
├── hotfix/critical-bug (긴급 버그 수정)
└── release/v1.0.0 (릴리즈 브랜치)
```

### **브랜치 명명 규칙**
```
feature/{service-name}-{feature-name}
hotfix/{service-name}-{bug-description}
release/v{major}.{minor}.{patch}
```

## 📁 **디렉토리 구조 상세**

### **루트 디렉토리**
```
maltan-project/
├── .gitignore
├── README.md
├── docker-compose.yml
├── docker-compose.dev.yml
├── docker-compose.prod.yml
└── .env.example
```

### **백엔드 서비스 구조**
```
backend/user-service/
├── src/
│   ├── main/
│   │   ├── java/com/maltan/userservice/
│   │   │   ├── UserServiceApplication.java
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   └── dto/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-docker.yml
│   └── test/
├── pom.xml
├── Dockerfile
├── README.md
└── .gitignore
```

### **프론트엔드 구조**
```
frontend/
├── src/
│   ├── components/
│   ├── pages/
│   ├── services/
│   └── utils/
├── public/
├── package.json
├── Dockerfile
├── nginx.conf
└── README.md
```

## 🔧 **Git 워크플로우**

### **1. 초기 설정**
```bash
# 저장소 초기화
git init

# .gitignore 생성
cat > .gitignore << EOF
# IDE
.idea/
.vscode/
*.iml

# Build
target/
build/
dist/
node_modules/

# Logs
*.log
logs/

# Environment
.env
.env.local

# Database
*.db
*.sqlite

# Docker
.docker/
EOF

# 첫 커밋
git add .
git commit -m "Initial project setup"
```

### **2. 서비스별 개발 워크플로우**
```bash
# User Service 개발
git checkout -b feature/user-service-auth
# 개발 작업...
git add backend/user-service/
git commit -m "feat: implement user authentication"
git push origin feature/user-service-auth

# Place Service 개발
git checkout -b feature/place-service-review
# 개발 작업...
git add backend/place-service/
git commit -m "feat: implement place review system"
git push origin feature/place-service-review
```

### **3. 브랜치 병합**
```bash
# develop 브랜치로 병합
git checkout develop
git merge feature/user-service-auth
git push origin develop

# main 브랜치로 병합 (릴리즈 시)
git checkout main
git merge develop
git tag v1.0.0
git push origin main --tags
```

## 📋 **커밋 메시지 규칙**

### **커밋 타입**
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 스타일 변경
refactor: 코드 리팩토링
test: 테스트 추가/수정
chore: 빌드/설정 변경
```

### **서비스별 커밋 예시**
```
feat(user-service): implement JWT authentication
fix(place-service): resolve review rating calculation bug
docs(gateway-service): update API documentation
refactor(recommendation-service): optimize recommendation algorithm
test(community-service): add unit tests for post service
```

## 🚀 **CI/CD 파이프라인**

### **GitHub Actions 워크플로우**
```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Test User Service
        run: |
          cd backend/user-service
          mvn test
      - name: Test Place Service
        run: |
          cd backend/place-service
          mvn test

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build Docker Images
        run: |
          docker-compose build
      - name: Push to Registry
        run: |
          docker-compose push
```

## 📊 **서비스별 관리**

### **서비스별 README 구조**
```
backend/user-service/README.md
├── 서비스 개요
├── API 엔드포인트
├── 데이터베이스 스키마
├── 실행 방법
├── 테스트 방법
└── 배포 방법
```

### **서비스별 설정 파일**
```
backend/user-service/
├── application.yml (기본 설정)
├── application-docker.yml (Docker 설정)
├── application-test.yml (테스트 설정)
└── application-prod.yml (프로덕션 설정)
```

## 🎯 **권장 Git 전략**

### **1. 모노레포 + 서비스별 브랜치**
- **장점**: 통합 관리 + 서비스별 독립성
- **적합한 경우**: 2명 개발팀, 통합 관리 필요

### **2. 서비스별 태그 관리**
```bash
# 서비스별 버전 태그
git tag user-service-v1.0.0
git tag place-service-v1.0.0
git tag frontend-v1.0.0
```

### **3. 의존성 관리**
```bash
# 서비스 간 의존성 확인
git log --oneline --grep="feat(user-service)"
git log --oneline --grep="feat(place-service)"
```

## 🔍 **모니터링 및 관리**

### **서비스별 변경사항 추적**
```bash
# 특정 서비스 변경사항 확인
git log --oneline --follow backend/user-service/

# 서비스별 커밋 통계
git log --pretty=format:"%h %an %s" -- backend/user-service/
```

### **브랜치 정리**
```bash
# 병합된 브랜치 삭제
git branch --merged | grep -v main | xargs -n 1 git branch -d

# 원격 브랜치 정리
git remote prune origin
```

이제 Git 저장소 구조가 완성되었습니다! 🚀
