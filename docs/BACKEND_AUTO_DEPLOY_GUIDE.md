# 백엔드 자동 배포 가이드

## 📋 개요

GitHub Actions를 사용하여 백엔드 서비스를 Ubuntu 서버에 자동으로 배포합니다.

## 🏗️ 배포 아키텍처

```
GitHub Repository (maltan-backend)
         ↓
   GitHub Actions
         ↓
Self-Hosted Runner (Ubuntu Server)
         ↓
  Docker Containers
```

## ✅ 현재 상태

### 설정 완료된 서비스

각 서비스는 이미 GitHub Actions 워크플로우가 설정되어 있습니다:

- ✅ **User Service** (`.github/workflows/deploy-user.yml`)
- ✅ **Place Service** (`.github/workflows/deploy-place.yml`)
- ✅ **Community Service** (`.github/workflows/deploy-community.yml`)
- ✅ **Gateway Service** (`.github/workflows/deploy-gateway.yml`)

### 동작 방식

1. **Path Filtering**: 각 서비스 폴더의 변경사항만 해당 서비스를 배포
   ```yaml
   on:
     push:
       branches:
         - main
       paths:
         - 'backend/user-service/**'
   ```

2. **Self-Hosted Runner**: Ubuntu 서버에서 직접 실행
   - 네트워크 접근 용이
   - SSH 설정 불필요
   - 빠른 배포 속도

3. **배포 프로세스**:
   ```
   코드 체크아웃
        ↓
   Maven 빌드
        ↓
   Docker 이미지 빌드
        ↓
   컨테이너 재시작
        ↓
   Health Check
   ```

## 🚀 배포 테스트

### 1. User Service 배포 테스트

```bash
# Windows에서
cd F:\3project\maltan-backend
cd backend\user-service

# 코드 수정 (예: README.md)
echo "# User Service - Updated" > README.md
git add .
git commit -m "test: user service deploy"
git push origin main
```

**GitHub Actions에서 확인**:
- https://github.com/YOUR_USERNAME/maltan-backend/actions
- `Deploy User Service` 워크플로우가 실행됨

### 2. Community Service 배포 테스트

```bash
# Community Service 수정
cd backend\community-service
echo "# Community Service - Updated" >> README.md
git add .
git commit -m "test: community service deploy"
git push origin main
```

### 3. 여러 서비스 동시 수정

여러 서비스를 동시에 수정하면 각각의 워크플로우가 병렬로 실행됩니다:

```bash
# User Service 수정
echo "Updated" >> backend/user-service/README.md

# Community Service 수정
echo "Updated" >> backend/community-service/README.md

git add .
git commit -m "test: multiple services deploy"
git push origin main
```

→ `deploy-user.yml`과 `deploy-community.yml`이 동시에 실행됨

## 📊 배포 확인

### 1. GitHub Actions에서 확인

```
Repository → Actions 탭
→ 실행 중인 워크플로우 확인
→ 로그 확인
```

### 2. Ubuntu 서버에서 확인

```bash
# SSH로 접속
ssh youruser@10.0.2.15

# Docker 컨테이너 상태 확인
docker ps

# 로그 확인
docker logs user-service
docker logs community-service
docker logs place-service
docker logs gateway-service

# 이미지 빌드 시간 확인
docker images | grep -E "user-service|community-service|place-service|gateway-service"
```

### 3. Health Check

```bash
# User Service (8081)
curl http://10.0.2.15:8081/api/users/health

# Place Service (8082)
curl http://10.0.2.15:8082/api/places/health

# Community Service (8083)
curl http://10.0.2.15:8083/api/community/health

# Gateway (8080)
curl http://10.0.2.15:8080/health
```

## 🔧 워크플로우 상세

### 공통 단계

모든 서비스의 워크플로우는 다음 단계를 따릅니다:

```yaml
jobs:
  deploy:
    runs-on: self-hosted
    
    steps:
      # 1. 코드 체크아웃
      - name: Checkout code
        uses: actions/checkout@v3

      # 2. Java 설정
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      # 3. Maven 빌드
      - name: Build with Maven
        run: |
          cd backend/user-service
          mvn clean package -DskipTests

      # 4. Docker 이미지 빌드
      - name: Build Docker image
        run: |
          cd backend/user-service
          docker build -t user-service:latest .

      # 5. 컨테이너 재시작
      - name: Deploy container
        run: |
          docker stop user-service || true
          docker rm user-service || true
          docker run -d \
            --name user-service \
            --network maltan-network \
            -p 8081:8081 \
            -e SPRING_DATASOURCE_URL="jdbc:postgresql://10.0.2.15:5432/user_db" \
            user-service:latest

      # 6. Health Check
      - name: Health check
        run: |
          sleep 10
          curl -f http://localhost:8081/health || exit 1
```

## 🛠️ 트러블슈팅

### 1. 워크플로우가 실행되지 않음

**원인**: Path filter가 해당 서비스를 감지하지 못함

**해결**:
```bash
# 해당 서비스 폴더 내에서 파일 수정 확인
cd backend/user-service
git status

# 변경사항이 해당 경로에 있는지 확인
```

### 2. Maven 빌드 실패

**원인**: 의존성 문제 또는 코드 오류

**해결**:
```bash
# 로컬에서 먼저 빌드 테스트
cd backend/user-service
mvn clean package

# 오류 수정 후 재푸시
```

### 3. Docker 이미지 빌드 실패

**원인**: Dockerfile 오류 또는 리소스 부족

**해결**:
```bash
# Ubuntu 서버에서 수동 빌드 테스트
cd ~/maltan-project/maltan_backend/user-service
docker build -t user-service:test .

# 디스크 공간 확인
df -h

# 사용하지 않는 이미지 정리
docker image prune -a
```

### 4. 컨테이너 시작 실패

**원인**: 포트 충돌, 환경변수 오류, DB 연결 실패

**해결**:
```bash
# 기존 컨테이너 확인
docker ps -a

# 로그 확인
docker logs user-service

# 수동 재시작 테스트
docker stop user-service
docker rm user-service
docker run -d --name user-service \
  --network maltan-network \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://10.0.2.15:5432/user_db" \
  -e SPRING_DATASOURCE_USERNAME="user_user" \
  -e SPRING_DATASOURCE_PASSWORD="User@2025!" \
  user-service:latest

# 로그 실시간 확인
docker logs -f user-service
```

### 5. Self-Hosted Runner 오프라인

**원인**: Runner 서비스가 중지됨

**해결**:
```bash
# Ubuntu 서버에서
cd /home/github-runner/actions-runner

# 상태 확인
sudo systemctl status actions.runner.*

# 재시작
sudo systemctl restart actions.runner.*

# 또는 수동 시작
sudo ./svc.sh start
```

## 📈 모니터링

### GitHub Actions 알림 설정

1. Repository → Settings → Notifications
2. "Actions" 체크
3. 빌드 실패 시 이메일 알림

### Slack 통합 (선택사항)

워크플로우에 Slack 알림 추가:

```yaml
- name: Slack Notification
  if: always()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    text: 'User Service deployment completed'
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

## 🔄 롤백 전략

### 1. 이전 이미지로 롤백

```bash
# 이미지 목록 확인
docker images user-service

# 이전 이미지로 롤백
docker stop user-service
docker rm user-service
docker run -d --name user-service \
  --network maltan-network \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL="..." \
  user-service:previous-tag
```

### 2. 이전 커밋으로 롤백

```bash
# Git 히스토리 확인
git log --oneline backend/user-service/

# 이전 커밋으로 리셋
git revert <commit-hash>
git push origin main
```

### 3. 수동 배포

```bash
# Ubuntu 서버에서
cd ~/maltan-project/maltan_backend
git pull origin main

cd user-service
mvn clean package -DskipTests
docker build -t user-service:latest .

docker stop user-service
docker rm user-service
docker run -d --name user-service \
  --network maltan-network \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL="..." \
  user-service:latest
```

## 📚 참고 문서

- [CI/CD 설정 가이드](CI_CD_SETUP_GUIDE.md)
- [Self-Hosted Runner 가이드](SELF_HOSTED_RUNNER_GUIDE.md)
- [Monorepo CI/CD 가이드](MONOREPO_CI_CD_GUIDE.md)

## ✅ 체크리스트

배포 전 확인사항:

- [ ] 로컬에서 빌드 테스트 완료 (`mvn clean package`)
- [ ] 단위 테스트 통과
- [ ] Self-Hosted Runner 정상 동작 확인
- [ ] 데이터베이스 마이그레이션 완료 (필요시)
- [ ] 환경변수 설정 확인
- [ ] 롤백 계획 수립

---

**이제 자동 배포가 준비되었습니다!** 🚀

각 서비스 폴더를 수정하고 `main` 브랜치에 푸시하면 자동으로 배포됩니다.

