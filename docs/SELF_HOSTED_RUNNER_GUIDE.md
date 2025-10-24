# Self-Hosted Runner 설정 가이드

## 📋 문제 상황

VirtualBox NAT 환경에서는 GitHub Actions가 외부에서 Ubuntu 서버에 SSH 접속할 수 없습니다.

**해결책**: Ubuntu 서버에 GitHub Actions Runner를 설치하여 **내부에서 실행**

---

## 🚀 Self-Hosted Runner 설정

### 1. GitHub에서 Runner 추가

1. https://github.com/KimNakYong/maltan_backend/settings/actions/runners 접속
2. **New self-hosted runner** 클릭
3. **Linux** 선택
4. 아래 명령어들이 표시됩니다 (복사해서 사용)

### 2. Ubuntu 서버에 Runner 설치

Ubuntu 서버에서 실행:

```bash
# Runner 디렉토리 생성
mkdir -p ~/actions-runner
cd ~/actions-runner

# Runner 다운로드 (GitHub에서 제공하는 최신 버전 URL 사용)
curl -o actions-runner-linux-x64-2.311.0.tar.gz -L https://github.com/actions/runner/releases/download/v2.311.0/actions-runner-linux-x64-2.311.0.tar.gz

# 압축 해제
tar xzf ./actions-runner-linux-x64-2.311.0.tar.gz

# 설정 실행 (GitHub에서 제공하는 토큰 사용)
./config.sh --url https://github.com/KimNakYong/maltan_backend --token AOGLEVMWXMRWATTQQACRLUDI7OQZ2

# 프롬프트가 나오면:
# Enter the name of the runner group to add this runner to: [press Enter for default]
# Enter the name of runner: [press Enter for maltan-VirtualBox]
# Enter any additional labels (ex. label-1,label-2): [press Enter]
# Enter name of work folder: [press Enter for _work]

# Runner를 서비스로 설치
sudo ./svc.sh install

# Runner 시작
sudo ./svc.sh start

# 상태 확인
sudo ./svc.sh status
```

### 3. Runner 자동 시작 설정

```bash
# 부팅 시 자동 시작
sudo systemctl enable actions.runner.KimNakYong-maltan_backend.maltan-VirtualBox.service

# 서비스 상태 확인
sudo systemctl status actions.runner.KimNakYong-maltan_backend.maltan-VirtualBox.service
```

---

## ✏️ 워크플로우 파일 수정

Self-Hosted Runner를 사용하도록 워크플로우 파일을 수정합니다.

### Before (SSH 방식 - 작동 안 함)
```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest  # GitHub 서버에서 실행
    
    steps:
      - uses: appleboy/ssh-action@master  # SSH로 접속 시도 → 실패
```

### After (Self-Hosted Runner - 작동함)
```yaml
jobs:
  deploy:
    runs-on: self-hosted  # Ubuntu 서버에서 직접 실행
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Build and Deploy
        run: |
          # 이미 Ubuntu 서버 안에 있으므로 바로 실행
          cd $GITHUB_WORKSPACE
          # 빌드 및 배포 명령어...
```

---

## 📝 수정된 워크플로우 예시

### Community Service 배포

```yaml
name: Deploy Community Service

on:
  push:
    branches:
      - main
    paths:
      - 'backend/community-service/**'
      - '.github/workflows/deploy-community.yml'
  workflow_dispatch:

jobs:
  deploy:
    runs-on: self-hosted  # ✅ Self-hosted runner 사용
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Build Community Service
        working-directory: backend/community-service
        run: |
          mvn clean package -DskipTests
      
      - name: Build Docker Image
        working-directory: backend/community-service
        run: |
          docker build -t community-service:latest .
      
      - name: Stop Old Container
        run: |
          docker stop community-service || true
          docker rm community-service || true
      
      - name: Run New Container
        run: |
          docker run -d \
            --name community-service \
            --network maltan-network \
            -p 8083:8083 \
            -e DB_HOST=10.0.2.15 \
            -e DB_PORT=5432 \
            -e DB_NAME=community_db \
            -e DB_USERNAME=community_user \
            -e DB_PASSWORD=Community@2025! \
            -e REDIS_HOST=10.0.2.15 \
            -e REDIS_PORT=6379 \
            -e SERVER_PORT=8083 \
            community-service:latest
      
      - name: Health Check
        run: |
          sleep 10
          docker ps | grep community-service
          docker logs --tail 50 community-service
          curl -f http://localhost:8083/actuator/health || echo "Health check failed"
```

---

## 🎯 장점

### Self-Hosted Runner

✅ **VirtualBox NAT 문제 해결**
- 외부에서 접속할 필요 없음
- Ubuntu 서버 내부에서 직접 실행

✅ **빠른 배포**
- 네트워크 지연 없음
- 로컬 Docker 이미지 사용

✅ **보안**
- SSH 포트 열 필요 없음
- GitHub Secrets 필요 없음 (로컬 실행)

✅ **유연성**
- 모든 로컬 리소스 직접 접근
- Docker, PostgreSQL 등 바로 사용

---

## 🔒 보안 고려사항

### Runner 전용 계정 생성 (권장)

```bash
# runner 전용 사용자 생성
sudo adduser github-runner
sudo usermod -aG docker github-runner

# runner 재설치 (github-runner 계정으로)
su - github-runner
mkdir -p ~/actions-runner
cd ~/actions-runner
# ... runner 설정 ...
```

### 제한된 권한 설정

```bash
# sudoers 파일 편집
sudo visudo

# 다음 줄 추가 (비밀번호 없이 docker 명령만 허용)
github-runner ALL=(ALL) NOPASSWD: /usr/bin/docker
```

---

## 🐛 트러블슈팅

### 문제 1: Runner가 시작되지 않음

```bash
# 로그 확인
sudo journalctl -u actions.runner.*.service -f

# Runner 재시작
sudo ./svc.sh stop
sudo ./svc.sh start
```

### 문제 2: Docker 권한 오류

```bash
# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# 로그아웃 후 다시 로그인
exit
# SSH 재접속
```

### 문제 3: Maven 빌드 실패

```bash
# Java 및 Maven 버전 확인
java --version
mvn --version

# Java 17 설치 (필요시)
sudo apt install openjdk-17-jdk
```

---

## 📊 모니터링

### Runner 상태 확인

GitHub에서:
- Settings → Actions → Runners
- Runner 상태: **Active** (초록색)

Ubuntu에서:
```bash
# 서비스 상태
sudo systemctl status actions.runner.*.service

# 실행 중인 job 확인
ps aux | grep Runner.Listener
```

---

## 🔄 Runner 업데이트

```bash
cd ~/actions-runner

# Runner 중지
sudo ./svc.sh stop

# 업데이트
./config.sh remove
# 새 버전 다운로드 및 설정
# ./config.sh --url ... --token ...

# 재시작
sudo ./svc.sh install
sudo ./svc.sh start
```

---

## ✅ 체크리스트

- [ ] GitHub에서 Runner 토큰 생성
- [ ] Ubuntu 서버에 Runner 설치
- [ ] Runner 서비스로 등록 및 시작
- [ ] GitHub에서 Runner 상태 Active 확인
- [ ] 워크플로우 파일에서 `runs-on: self-hosted` 사용
- [ ] 테스트 배포 실행
- [ ] 컨테이너 정상 실행 확인

---

**작성일**: 2025-10-24  
**버전**: 1.0.0

