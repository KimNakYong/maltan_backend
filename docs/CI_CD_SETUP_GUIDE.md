# CI/CD 설정 가이드 (GitHub Actions)

## 📋 개요

GitHub Actions를 사용하여 코드를 push할 때 자동으로 Ubuntu 서버에 배포합니다.

---

## 🔧 1. Ubuntu 서버 설정

### SSH 접속 확인

```bash
# Ubuntu 서버에서 SSH 서비스 확인
sudo systemctl status ssh

# SSH 포트 확인 (기본: 22)
sudo netstat -tlnp | grep ssh
```

### Git 설치 확인

```bash
git --version

# 설치되어 있지 않다면
sudo apt update
sudo apt install -y git
```

### Maven 설치

```bash
# Maven 설치
sudo apt update
sudo apt install -y maven

# 버전 확인
mvn --version
```

### Docker 네트워크 생성 (없다면)

```bash
# maltan-network 생성
docker network create maltan-network || echo "Network already exists"

# 네트워크 확인
docker network ls
```

---

## 🔐 2. GitHub Secrets 설정

GitHub 저장소의 **Settings → Secrets and variables → Actions → New repository secret**에서 다음 Secrets를 추가합니다:

### 필수 Secrets

| Secret 이름 | 값 | 설명 |
|------------|-----|------|
| `SERVER_HOST` | `10.0.2.15` | Ubuntu 서버 IP 주소 |
| `SERVER_USER` | `root` | Ubuntu 서버 사용자명 |
| `SERVER_PASSWORD` | `your_password` | Ubuntu 서버 비밀번호 |
| `SERVER_PORT` | `22` | SSH 포트 (기본 22) |

### Secrets 추가 방법

1. GitHub 저장소로 이동
2. **Settings** 클릭
3. **Secrets and variables** → **Actions** 클릭
4. **New repository secret** 클릭
5. 위 표의 각 항목을 하나씩 추가

---

## 📁 3. Ubuntu 서버에 프로젝트 디렉토리 생성

Ubuntu 서버에서 다음 명령어 실행:

```bash
# 프로젝트 디렉토리 생성
mkdir -p ~/maltan-project/maltan_backend

# 디렉토리로 이동
cd ~/maltan-project/maltan_backend

# Git 클론 (GitHub 저장소 생성 후)
git clone https://github.com/KimNakYong/maltan_community_service.git community-service

# 디렉토리 확인
cd community-service
ls -la
```

### Git 인증 설정 (필요한 경우)

```bash
# Git 사용자 설정
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# GitHub Personal Access Token 사용 (권장)
# Settings → Developer settings → Personal access tokens → Generate new token
# repo 권한 부여

# 또는 SSH 키 설정
ssh-keygen -t ed25519 -C "your.email@example.com"
cat ~/.ssh/id_ed25519.pub
# 출력된 공개키를 GitHub Settings → SSH keys에 추가
```

---

## 🚀 4. GitHub Actions Workflow 파일

### 메인 배포 워크플로우 (`.github/workflows/deploy.yml`)

main 브랜치에 push할 때 자동으로 배포합니다.

**주요 단계:**
1. 코드 체크아웃
2. SSH로 Ubuntu 서버 접속
3. Git pull
4. Maven 빌드
5. Docker 이미지 빌드
6. 기존 컨테이너 중지/삭제
7. 새 컨테이너 실행
8. 헬스체크

### 빌드 테스트 워크플로우 (`.github/workflows/build-test.yml`)

Pull Request 또는 feature 브랜치에 push할 때 빌드 테스트만 수행합니다.

---

## 📝 5. 사용 방법

### 로컬에서 개발 후 배포

```bash
# 로컬에서 코드 수정 후
git add .
git commit -m "feat: 새로운 기능 추가"
git push origin main

# GitHub Actions가 자동으로 실행됨
# GitHub 저장소의 Actions 탭에서 진행 상황 확인 가능
```

### 배포 상태 확인

#### GitHub에서 확인
1. GitHub 저장소의 **Actions** 탭 클릭
2. 최근 워크플로우 실행 확인
3. 로그 확인

#### Ubuntu 서버에서 확인

```bash
# 컨테이너 상태 확인
docker ps | grep community-service

# 로그 확인
docker logs -f community-service

# 헬스체크
curl http://localhost:8083/actuator/health

# 서비스 테스트
curl http://localhost:8083/api/community/posts
```

---

## 🔄 6. 수동 배포 (필요 시)

GitHub Actions UI에서 수동으로 워크플로우를 실행할 수 있습니다:

1. **Actions** 탭 → **Deploy Community Service to Ubuntu Server** 선택
2. **Run workflow** 클릭
3. 브랜치 선택 (main)
4. **Run workflow** 버튼 클릭

---

## 🐛 7. 트러블슈팅

### 문제 1: SSH 연결 실패

```bash
# Ubuntu 서버에서 SSH 서비스 재시작
sudo systemctl restart ssh

# 방화벽 확인
sudo ufw status
sudo ufw allow 22/tcp
```

### 문제 2: Git pull 실패 (권한 문제)

```bash
# Ubuntu 서버에서
cd ~/maltan-project/maltan_backend/community-service

# 원격 저장소 URL 확인
git remote -v

# HTTPS로 변경 (Personal Access Token 사용)
git remote set-url origin https://YOUR_TOKEN@github.com/KimNakYong/maltan_community_service.git

# 또는 SSH로 변경
git remote set-url origin git@github.com:KimNakYong/maltan_community_service.git
```

### 문제 3: Maven 빌드 실패

```bash
# Ubuntu 서버에서 수동 빌드 테스트
cd ~/maltan-project/maltan_backend/community-service
mvn clean package -DskipTests

# 로그 확인
tail -f target/surefire-reports/*.txt
```

### 문제 4: Docker 컨테이너 실행 실패

```bash
# 기존 컨테이너 완전 제거
docker stop community-service
docker rm community-service

# 이미지 재빌드
docker build -t community-service:latest .

# 네트워크 확인
docker network ls
docker network inspect maltan-network

# 수동 실행 테스트
docker run -it --rm \
  -e DB_HOST=10.0.2.15 \
  -e DB_NAME=community_db \
  community-service:latest
```

### 문제 5: 포트 충돌

```bash
# 8083 포트 사용 확인
sudo netstat -tlnp | grep 8083

# 사용 중인 프로세스 종료
sudo kill -9 <PID>
```

---

## 📊 8. 모니터링

### GitHub Actions 알림 설정

1. **Settings** → **Notifications**
2. **GitHub Actions** 섹션에서 알림 활성화
3. 실패 시 이메일 알림 받기

### 로그 모니터링

```bash
# Ubuntu 서버에서 실시간 로그 확인
docker logs -f community-service

# 최근 100줄만 보기
docker logs --tail 100 community-service

# 타임스탬프와 함께 보기
docker logs -t community-service
```

---

## 🔒 9. 보안 강화 (선택사항)

### SSH Key 기반 인증 (비밀번호 대신)

#### GitHub Actions에서 SSH Key 사용

1. Ubuntu 서버에서 SSH 키 생성:
```bash
ssh-keygen -t ed25519 -C "github-actions"
cat ~/.ssh/id_ed25519.pub >> ~/.ssh/authorized_keys
cat ~/.ssh/id_ed25519  # Private Key 복사
```

2. GitHub Secrets에 추가:
   - `SERVER_SSH_KEY`: Private Key 내용

3. Workflow 파일 수정:
```yaml
- name: Deploy to Ubuntu Server
  uses: appleboy/ssh-action@master
  with:
    host: ${{ secrets.SERVER_HOST }}
    username: ${{ secrets.SERVER_USER }}
    key: ${{ secrets.SERVER_SSH_KEY }}  # password 대신 key 사용
    port: ${{ secrets.SERVER_PORT }}
    script: |
      # ... 배포 스크립트
```

---

## 📈 10. 향후 개선 사항

### 1. 블루-그린 배포

```yaml
# 무중단 배포를 위한 블루-그린 전략
- Run container with different name
- Health check
- Switch nginx upstream
- Remove old container
```

### 2. 데이터베이스 마이그레이션

```yaml
# Flyway 또는 Liquibase 통합
- name: Run DB migrations
  run: mvn flyway:migrate
```

### 3. 롤백 기능

```yaml
# 배포 실패 시 이전 버전으로 롤백
- name: Rollback on failure
  if: failure()
  run: |
    docker stop community-service
    docker run -d --name community-service community-service:previous
```

### 4. Slack/Discord 알림

```yaml
- name: Notify Slack
  if: always()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

---

## ✅ 11. 체크리스트

배포 전 확인사항:

- [ ] GitHub Secrets 모두 설정
- [ ] Ubuntu 서버에 Git, Maven, Docker 설치
- [ ] PostgreSQL 실행 중
- [ ] Docker 네트워크 생성
- [ ] Ubuntu 서버에 프로젝트 디렉토리 클론
- [ ] `.github/workflows/deploy.yml` 파일 생성
- [ ] main 브랜치에 push

배포 후 확인사항:

- [ ] GitHub Actions 워크플로우 성공
- [ ] Docker 컨테이너 실행 중 (`docker ps`)
- [ ] 헬스체크 성공 (`curl http://localhost:8083/actuator/health`)
- [ ] API 응답 확인 (`curl http://localhost:8083/api/community/posts`)
- [ ] 로그 확인 (`docker logs community-service`)

---

**작성일**: 2025-10-24  
**버전**: 1.0.0

