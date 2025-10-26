#!/bin/bash

# Docker를 사용한 MySQL 설치 및 user_service 데이터베이스 생성
# 실행 방법: chmod +x ubuntu_docker_setup.sh && ./ubuntu_docker_setup.sh

echo "========================================"
echo "Docker MySQL 설치 및 user_service 데이터베이스 생성"
echo "========================================"
echo

# 1. Docker 설치 확인
echo "[1/6] Docker 설치 확인 중..."
if ! command -v docker &> /dev/null; then
    echo "Docker가 설치되어 있지 않습니다. 설치 중..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    echo "Docker 설치 완료. 시스템을 재부팅하거나 로그아웃 후 다시 로그인하세요."
    exit 1
fi

# 2. Docker Compose 설치 확인
echo "[2/6] Docker Compose 설치 확인 중..."
if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose 설치 중..."
    sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

# 3. Docker Compose 파일 생성
echo "[3/6] Docker Compose 파일 생성 중..."
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: user_service-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: user_service
      MYSQL_USER: user_service
      MYSQL_PASSWORD: user_servicepassword
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./ubuntu_mysql_manual.sql:/docker-entrypoint-initdb.d/init.sql
    command: --default-authentication-plugin=mysql_native_password

  phpmyadmin:
    image: phpmyadmin/phpmyadmin
    container_name: user_service-phpmyadmin
    restart: always
    environment:
      PMA_HOST: mysql
      PMA_PORT: 3306
      PMA_USER: root
      PMA_PASSWORD: rootpassword
    ports:
      - "8080:80"
    depends_on:
      - mysql

volumes:
  mysql_data:
EOF

# 4. Docker 컨테이너 실행
echo "[4/6] MySQL Docker 컨테이너 실행 중..."
docker-compose up -d

# 5. 컨테이너 상태 확인
echo "[5/6] 컨테이너 상태 확인 중..."
sleep 10
docker-compose ps

# 6. 데이터베이스 연결 테스트
echo "[6/6] 데이터베이스 연결 테스트 중..."
sleep 5
docker exec -it user_service-mysql mysql -u root -prootpassword -e "USE user_service; SHOW TABLES;"

echo
echo "========================================"
echo "Docker MySQL 설치 완료!"
echo "========================================"
echo
echo "접속 정보:"
echo "- MySQL 호스트: localhost"
echo "- MySQL 포트: 3306"
echo "- 데이터베이스: user_service"
echo "- 사용자: root"
echo "- 비밀번호: rootpassword"
echo
echo "phpMyAdmin:"
echo "- URL: http://localhost:8080"
echo "- 사용자: root"
echo "- 비밀번호: rootpassword"
echo
echo "MySQL 접속 명령어:"
echo "docker exec -it user_service-mysql mysql -u root -prootpassword"
echo
echo "Spring Boot application.yml 설정:"
echo "spring:"
echo "  datasource:"
echo "    url: jdbc:mysql://localhost:3306/user_service?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
echo "    username: root"
echo "    password: rootpassword"
echo "    driver-class-name: com.mysql.cj.jdbc.Driver"
echo
echo "컨테이너 관리 명령어:"
echo "- 시작: docker-compose up -d"
echo "- 중지: docker-compose down"
echo "- 로그 확인: docker-compose logs mysql"
echo "- 재시작: docker-compose restart mysql"
