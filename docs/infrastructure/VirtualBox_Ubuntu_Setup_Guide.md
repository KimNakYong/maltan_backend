# VirtualBox Ubuntu 환경 설정 가이드

## 1. VirtualBox 설치 및 설정

### 1.1 VirtualBox 다운로드 및 설치
1. **VirtualBox 다운로드**
   - 공식 사이트: https://www.virtualbox.org/
   - Windows용 VirtualBox 7.0.x 다운로드
   - 관리자 권한으로 설치

2. **VirtualBox Extension Pack 설치**
   - VirtualBox 다운로드 페이지에서 Extension Pack 다운로드
   - VirtualBox 실행 → File → Preferences → Extensions → Add Package
   - Extension Pack 설치 (USB 3.0, RDP 등 지원)

### 1.2 가상머신 생성
1. **새 가상머신 생성**
   - VirtualBox 실행 → "New" 클릭
   - Name: `Ubuntu-MSA-Development`
   - Type: Linux
   - Version: Ubuntu (64-bit)

2. **메모리 할당**
   - RAM: **4GB** (MSA 개발을 위한 최적 메모리)
   - 리소스 제한을 고려한 효율적 할당

3. **하드디스크 설정**
   - Create a virtual hard disk now
   - VDI (VirtualBox Disk Image)
   - Dynamically allocated
   - **30GB** (개발 환경에 적합한 크기)

### 1.3 가상머신 설정 최적화
1. **시스템 설정**
   - Settings → System → Motherboard
   - Base Memory: 4096 MB
   - Boot Order: Hard Disk, Optical
   - Enable I/O APIC 체크

2. **프로세서 설정**
   - Settings → System → Processor
   - Processors: **2개** (호스트 CPU 코어의 적절한 할당)
   - Enable PAE/NX 체크

3. **디스플레이 설정**
   - Settings → Display
   - Video Memory: 128 MB
   - Enable 3D Acceleration 체크

## 2. Ubuntu 22.04 LTS 설치

### 2.1 Ubuntu ISO 다운로드
1. **Ubuntu 22.04 LTS 다운로드**
   - 공식 사이트: https://ubuntu.com/download/desktop
   - Ubuntu 22.04.3 LTS (64-bit) 다운로드
   - 파일 크기: 약 4.7GB

### 2.2 가상머신에 Ubuntu 설치
1. **ISO 마운트**
   - 가상머신 선택 → Settings → Storage
   - Controller: IDE → Empty → Attributes → CD/DVD Device
   - Ubuntu ISO 파일 선택

2. **Ubuntu 설치 시작**
   - 가상머신 시작
   - "Try or Install Ubuntu" 선택
   - 설치 과정 진행

3. **설치 옵션**
   - **Normal installation** 선택
   - **Install third-party software** 체크
   - **Erase disk and install Ubuntu** 선택
   - 사용자 계정 생성:
     - Your name: `maltan`
     - Username: `maltan`
     - Password: `1234`

### 2.3 설치 후 설정
1. **Guest Additions 설치**
   - VirtualBox 메뉴 → Devices → Insert Guest Additions CD image
   - 터미널에서:
   ```bash
   sudo apt update
   sudo apt install build-essential dkms linux-headers-$(uname -r)
   sudo mount /dev/cdrom /mnt
   sudo /mnt/VBoxLinuxAdditions.run
   sudo reboot
   ```

2. **공유 폴더 설정**
   - VirtualBox 메뉴 → Devices → Shared Folders
   - Machine Folders → Add Shared Folder
   - Folder Path: `F:\3project` (Windows 호스트 경로)
   - Folder Name: `maltan-project`
   - Auto-mount 체크

## 3. Ubuntu 기본 설정

### 3.1 시스템 업데이트
```bash
# 시스템 업데이트
sudo apt update && sudo apt upgrade -y

# 필수 패키지 설치
sudo apt install -y curl wget git vim nano htop tree unzip
```

### 3.2 네트워크 설정
```bash
# 네트워크 인터페이스 확인
ip addr show

# 호스트와 게스트 간 네트워크 확인
ping google.com
```

### 3.3 SSH 서버 설치 (선택사항)
```bash
# SSH 서버 설치
sudo apt install openssh-server -y

# SSH 서비스 시작
sudo systemctl start ssh
sudo systemctl enable ssh

# SSH 포트 확인
sudo netstat -tlnp | grep :22
```

## 4. 개발 도구 설치

### 4.1 Java 17 설치
```bash
# OpenJDK 17 설치
sudo apt install openjdk-17-jdk -y

# Java 버전 확인
java -version
javac -version

# JAVA_HOME 설정
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

### 4.2 Docker 설치
```bash
# Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# Docker Compose 설치
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 설치 확인
docker --version
docker-compose --version
```

### 4.3 Node.js 설치 (프론트엔드용)
```bash
# Node.js 18.x 설치
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# npm 버전 확인
node --version
npm --version
```

### 4.4 Git 설정
```bash
# Git 설정
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# SSH 키 생성 (GitHub 연동용)
ssh-keygen -t rsa -b 4096 -C "your.email@example.com"
cat ~/.ssh/id_rsa.pub
```

## 5. 포트 포워딩 설정

### 5.1 VirtualBox 포트 포워딩
1. **가상머신 설정**
   - 가상머신 종료 후 Settings → Network
   - Adapter 1 → Advanced → Port Forwarding

2. **포트 포워딩 규칙 추가**
   ```
   Name: SSH
   Protocol: TCP
   Host IP: 127.0.0.1
   Host Port: 2222
   Guest IP: (비워둠)
   Guest Port: 22

   Name: Gateway
   Protocol: TCP
   Host IP: 127.0.0.1
   Host Port: 8080
   Guest IP: (비워둠)
   Guest Port: 8080

   Name: User Service
   Protocol: TCP
   Host IP: 127.0.0.1
   Host Port: 8081
   Guest IP: (비워둠)
   Guest Port: 8081

   Name: Place Service
   Protocol: TCP
   Host IP: 127.0.0.1
   Host Port: 8082
   Guest IP: (비워둠)
   Guest Port: 8082

   Name: Recommendation Service
   Protocol: TCP
   Host IP: 127.0.0.1
   Host Port: 8083
   Guest IP: (비워둠)
   Guest Port: 8083

   Name: Community Service
   Protocol: TCP
   Host IP: 127.0.0.1
   Host Port: 8084
   Guest IP: (비워둠)
   Guest Port: 8084

   Name: Frontend
   Protocol: TCP
   Host IP: 127.0.0.1
   Host Port: 3000
   Guest IP: (비워둠)
   Guest Port: 3000
   ```

## 6. 프로젝트 디렉토리 설정

### 6.1 프로젝트 폴더 생성
```bash
# 홈 디렉토리에 프로젝트 폴더 생성
mkdir -p ~/maltan-project
cd ~/maltan-project

# 공유 폴더 마운트 확인
ls /media/sf_maltan-project/

# 프로젝트 구조 생성
mkdir -p backend/{user-service,place-service,recommendation-service,community-service,gateway-service}
mkdir -p frontend
mkdir -p docker
mkdir -p docs
```

### 6.2 Git 저장소 초기화
```bash
# Git 저장소 초기화
git init

# .gitignore 파일 생성
cat > .gitignore << EOF
# IDE
.idea/
.vscode/
*.iml

# Build
target/
build/
dist/

# Dependencies
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

## 7. 개발 환경 테스트

### 7.1 Docker 테스트
```bash
# Docker 실행 테스트
docker run hello-world

# Docker Compose 테스트
cat > docker-compose.test.yml << EOF
version: '3.8'
services:
  test:
    image: hello-world
EOF

docker-compose -f docker-compose.test.yml up
```

### 7.2 Java 테스트
```bash
# 간단한 Java 프로그램 테스트
cat > HelloWorld.java << EOF
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, MSA World!");
    }
}
EOF

javac HelloWorld.java
java HelloWorld
```

### 7.3 Node.js 테스트
```bash
# 간단한 Node.js 프로그램 테스트
cat > hello.js << EOF
console.log('Hello, MSA Frontend!');
EOF

node hello.js
```

## 8. 다음 단계

환경 설정이 완료되면 다음 단계로 진행:
1. **Docker 기반 MSA 인프라 구축**
2. **데이터베이스 설정 (PostgreSQL, Redis)**
3. **첫 번째 마이크로서비스 개발 시작**

## 문제 해결

### 일반적인 문제들
1. **메모리 부족**: 가상머신 메모리를 8GB로 증가
2. **네트워크 연결 안됨**: NAT 네트워크 설정 확인
3. **공유 폴더 접근 안됨**: Guest Additions 재설치
4. **포트 포워딩 안됨**: 가상머신 재시작 후 확인

### 유용한 명령어
```bash
# 시스템 정보 확인
uname -a
free -h
df -h

# 네트워크 상태 확인
ip addr show
netstat -tlnp

# Docker 상태 확인
docker ps
docker images

# 서비스 상태 확인
systemctl status docker
systemctl status ssh
```

이제 VirtualBox Ubuntu 환경이 준비되었습니다! 다음 단계로 진행하시겠어요?
