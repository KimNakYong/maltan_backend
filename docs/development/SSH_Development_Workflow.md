# SSH 기반 개발 워크플로우 가이드

## 🏗️ **개발 환경 구조**

### **전체 아키텍처**
```
개발자 A (로컬)          개발자 B (로컬)
        ↓                        ↓
    SSH 접속                SSH 접속
        ↓                        ↓
    VirtualBox Ubuntu (공유 개발 서버)
                 ↓
    Docker 컨테이너들 (MSA 서비스들)
```

### **개발 워크플로우**
```
1. 로컬에서 코딩 (IDE: VS Code, IntelliJ 등)
2. SSH로 Ubuntu 서버에 접속
3. 코드를 서버에 업로드/동기화
4. Docker 컨테이너에서 테스트
5. Git으로 버전 관리
```

## 🔧 **SSH 접속 설정**

### **1. VirtualBox 포트 포워딩 설정**
```
VirtualBox 설정 → Network → Port Forwarding
Name: SSH
Protocol: TCP
Host IP: (비워둠)
Host Port: 2222
Guest IP: 10.0.2.15
Guest Port: 22
```

### **2. Ubuntu SSH 서버 설정**
```bash
# Ubuntu에서 SSH 서버 설치
sudo apt update
sudo apt install openssh-server -y

# SSH 서비스 시작
sudo systemctl start ssh
sudo systemctl enable ssh

# SSH 포트 확인
sudo netstat -tlnp | grep :22
```

### **3. 개발자 계정 생성**
```bash
# 개발자 A 계정 생성
sudo adduser developer-a
sudo usermod -aG sudo developer-a
sudo usermod -aG docker developer-a

# 개발자 B 계정 생성
sudo adduser developer-b
sudo usermod -aG sudo developer-b
sudo usermod -aG docker developer-b

# SSH 키 설정 (각 개발자별)
sudo mkdir -p /home/developer-a/.ssh
sudo mkdir -p /home/developer-b/.ssh
```

## 💻 **로컬 개발 환경 설정**

### **1. SSH 클라이언트 설정**
```bash
# Windows (PowerShell)
ssh developer-a@localhost -p 2222

# macOS/Linux
ssh developer-a@localhost -p 2222
```

### **2. VS Code SSH 확장 설정**
```json
// .vscode/settings.json
{
    "remote.SSH.remotePlatform": {
        "ubuntu-server": "linux"
    },
    "remote.SSH.configFile": "~/.ssh/config"
}
```

### **3. SSH Config 설정**
```bash
# ~/.ssh/config
Host maltan-dev
    HostName localhost
    Port 2222
    User developer-a
    IdentityFile ~/.ssh/id_rsa
```

## 🚀 **개발 워크플로우**

### **방법 1: 로컬 코딩 + SSH 업로드**

#### **1단계: 로컬에서 코딩**
```bash
# 로컬에서 프로젝트 클론
git clone <repository-url>
cd maltan-project

# 로컬에서 개발 (IDE 사용)
# - VS Code, IntelliJ 등으로 코딩
# - 로컬에서 문법 검사, 자동완성 등 활용
```

#### **2단계: SSH로 서버 접속**
```bash
# SSH 접속
ssh developer-a@localhost -p 2222

# 서버에서 프로젝트 디렉토리로 이동
cd ~/maltan-project
```

#### **3단계: 코드 동기화**
```bash
# 방법 A: Git을 통한 동기화
git add .
git commit -m "feat: implement user authentication"
git push origin feature/user-service-auth

# 서버에서 최신 코드 받기
git pull origin feature/user-service-auth
```

#### **4단계: Docker에서 테스트**
```bash
# 서버에서 Docker 서비스 시작
cd ~/maltan-project/docker
docker-compose up user-service

# 테스트 실행
curl http://localhost:8081/actuator/health
```

### **방법 2: VS Code Remote SSH (권장)**

#### **1단계: VS Code Remote SSH 설정**
```bash
# VS Code에서 Remote SSH 확장 설치
# Ctrl+Shift+P → "Remote-SSH: Connect to Host"
# Host: maltan-dev (SSH config에서 설정한 호스트)
```

#### **2단계: 원격 개발**
```bash
# VS Code에서 원격 서버에 직접 접속
# - 로컬 IDE 기능을 서버에서 사용
# - 파일 편집, 디버깅, 터미널 등 모든 기능 사용
# - Git 관리도 원격에서 직접 수행
```

#### **3단계: Docker 테스트**
```bash
# VS Code 터미널에서 Docker 명령 실행
docker-compose up user-service
docker-compose logs user-service
```

## 🔄 **코드 동기화 방법**

### **방법 1: Git 기반 동기화 (권장)**
```bash
# 로컬에서 개발
# 1. 로컬에서 코딩
# 2. Git 커밋
git add backend/user-service/
git commit -m "feat: implement user authentication"
git push origin feature/user-service-auth

# 서버에서 테스트
# 3. SSH 접속
ssh developer-a@localhost -p 2222
# 4. 최신 코드 받기
git pull origin feature/user-service-auth
# 5. Docker 테스트
docker-compose up user-service
```

### **방법 2: rsync 기반 동기화**
```bash
# 로컬에서 서버로 파일 동기화
rsync -avz --exclude 'node_modules' --exclude 'target' \
  ./maltan-project/ developer-a@localhost:~/maltan-project/

# 서버에서 로컬로 파일 동기화
rsync -avz developer-a@localhost:~/maltan-project/ ./maltan-project/
```

### **방법 3: VS Code Remote SSH (가장 효율적)**
```bash
# VS Code에서 원격 서버에 직접 접속
# - 로컬 IDE 기능을 서버에서 사용
# - 파일 편집, 디버깅, 터미널 등 모든 기능 사용
# - Git 관리도 원격에서 직접 수행
# - Docker 테스트도 원격에서 직접 수행
```

## 🐳 **Docker 테스트 워크플로우**

### **개발 단계별 테스트**
```bash
# 1단계: 기본 인프라 테스트
docker-compose up postgres redis

# 2단계: 특정 서비스 테스트
docker-compose up user-service

# 3단계: 전체 서비스 테스트
docker-compose up -d

# 4단계: 서비스 상태 확인
docker-compose ps
docker-compose logs user-service
```

### **서비스별 테스트**
```bash
# User Service 테스트
docker-compose up user-service
curl http://localhost:8081/actuator/health

# Place Service 테스트
docker-compose up place-service
curl http://localhost:8082/actuator/health

# 전체 서비스 테스트
docker-compose up -d
curl http://localhost:8080/actuator/health
```

## 👥 **2명 개발자 협업**

### **개발자 역할 분담**
```
개발자 A (SSH: developer-a):
├── User Service 개발
├── Place Service 개발
├── Gateway Service 개발
└── Backend API 최적화

개발자 B (SSH: developer-b):
├── Recommendation Service 개발
├── Community Service 개발
├── Frontend 개발
└── 모니터링 설정
```

### **협업 워크플로우**
```bash
# 1. 각자 브랜치에서 개발
git checkout -b feature/user-service-auth
# 개발 작업...

# 2. Git으로 코드 공유
git add backend/user-service/
git commit -m "feat(user-service): implement JWT authentication"
git push origin feature/user-service-auth

# 3. 다른 개발자가 코드 받기
git checkout feature/user-service-auth
git pull origin feature/user-service-auth

# 4. 통합 테스트
docker-compose up -d
```

## 🔧 **개발 환경 최적화**

### **VS Code Remote SSH 설정**
```json
// .vscode/settings.json
{
    "remote.SSH.remotePlatform": {
        "ubuntu-server": "linux"
    },
    "remote.SSH.configFile": "~/.ssh/config",
    "remote.SSH.showLoginTerminal": true,
    "remote.SSH.enableDynamicForwarding": true
}
```

### **SSH Config 최적화**
```bash
# ~/.ssh/config
Host maltan-dev
    HostName localhost
    Port 2222
    User developer-a
    IdentityFile ~/.ssh/id_rsa
    ServerAliveInterval 60
    ServerAliveCountMax 3
    TCPKeepAlive yes
```

## 📊 **모니터링 및 디버깅**

### **서비스 상태 확인**
```bash
# 전체 서비스 상태
docker-compose ps

# 특정 서비스 로그
docker-compose logs user-service

# 리소스 사용량
docker stats

# 서비스 헬스체크
curl http://localhost:8081/actuator/health
```

### **디버깅 방법**
```bash
# 서비스 로그 실시간 확인
docker-compose logs -f user-service

# 서비스 재시작
docker-compose restart user-service

# 특정 서비스만 시작
docker-compose up user-service
```

## 🎯 **권장 개발 워크플로우**

### **1. VS Code Remote SSH (가장 권장)**
```
로컬 VS Code → SSH 접속 → 원격 서버에서 직접 개발
├── 로컬 IDE 기능 활용
├── 원격 서버에서 코딩
├── Docker 테스트
└── Git 관리
```

### **2. 로컬 코딩 + Git 동기화**
```
로컬 IDE → 코딩 → Git 커밋 → SSH 접속 → Git 풀 → Docker 테스트
├── 로컬 IDE 기능 활용
├── Git으로 코드 동기화
├── SSH로 서버 접속
└── Docker 테스트
```

### **3. 하이브리드 방식**
```
로컬 IDE → 코딩 → SSH 접속 → 원격에서 테스트
├── 로컬에서 코딩
├── SSH로 서버 접속
├── 원격에서 테스트
└── Git으로 버전 관리
```

## 🚀 **다음 단계**

개발 워크플로우가 설정되었으니:

1. **SSH 접속 설정**
2. **VS Code Remote SSH 설정**
3. **첫 번째 서비스 개발 시작**
4. **Docker 테스트 환경 구축**

이제 2명의 개발자가 효율적으로 협업할 수 있는 환경이 준비되었습니다! 🚀
