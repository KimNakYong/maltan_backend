# User Service Database

회원가입 시스템을 위한 MySQL 데이터베이스 설정 파일들입니다.

## 📁 파일 구조

```
database/
├── 01_create_database.sql      # 데이터베이스 스키마 생성
├── 02_insert_sample_data.sql   # 샘플 데이터 삽입
├── 03_database_setup_guide.md # 설정 가이드
├── 04_docker_setup.sql        # Docker용 초기 설정
├── docker-compose.yml         # Docker Compose 설정
└── README.md                  # 이 파일
```

## 🚀 빠른 시작

### 방법 1: Docker 사용 (권장)

```bash
# 데이터베이스 디렉토리로 이동
cd database

# Docker Compose로 MySQL 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f mysql
```

**접속 정보:**
- MySQL: `localhost:3306`
- phpMyAdmin: `http://localhost:8080`
- 사용자: `root` / 비밀번호: `rootpassword`

### 방법 2: 로컬 MySQL 사용

```bash
# MySQL 접속
mysql -u root -p

# 스키마 실행
source 01_create_database.sql;

# 샘플 데이터 삽입
source 02_insert_sample_data.sql;
```

## 🗄️ 데이터베이스 구조

### 주요 테이블

1. **users** - 사용자 기본 정보
   - id, username, email, password, name, phone_number
   - role, is_enabled, created_at, updated_at

2. **preferred_regions** - 사용자 선호 지역
   - id, user_id, city, city_name, district, district_name
   - priority (1-3), created_at, updated_at

3. **user_sessions** - 사용자 세션 (선택사항)
   - id, user_id, session_token, expires_at, created_at

4. **login_history** - 로그인 이력 (선택사항)
   - id, user_id, login_ip, user_agent, login_at, success

## 🔧 애플리케이션 설정

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_service?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: rootpassword  # Docker 사용시
    # username: user_service
    # password: user_service_password  # 전용 사용자 사용시
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 📊 샘플 데이터

### 기본 사용자
- **관리자**: admin@example.com / admin123
- **홍길동**: hong@example.com / password123
- **김철수**: kim@example.com / password123
- **이영희**: lee@example.com / password123
- **박민수**: park@example.com / password123

### 선호 지역 예시
- 홍길동: 강남구(1순위), 마포구(2순위)
- 김철수: 해운대구(1순위)
- 이영희: 종로구(1순위), 중구(2순위), 용산구(3순위)

## 🔍 유용한 쿼리

### 사용자 및 선호 지역 조회
```sql
SELECT 
    u.username,
    u.name,
    u.email,
    pr.city_name,
    pr.district_name,
    pr.priority
FROM users u
LEFT JOIN preferred_regions pr ON u.id = pr.user_id
ORDER BY u.username, pr.priority;
```

### 지역별 사용자 통계
```sql
SELECT 
    pr.city_name,
    pr.district_name,
    COUNT(DISTINCT u.id) as user_count
FROM preferred_regions pr
JOIN users u ON pr.user_id = u.id
GROUP BY pr.city_name, pr.district_name
ORDER BY user_count DESC;
```

## 🛠️ 트러블슈팅

### Docker 관련
```bash
# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs mysql

# 컨테이너 재시작
docker-compose restart mysql

# 데이터 초기화
docker-compose down -v
docker-compose up -d
```

### 연결 문제
```bash
# 포트 확인
netstat -an | grep 3306

# 방화벽 확인 (Windows)
netsh advfirewall firewall show rule name="MySQL"

# 방화벽 확인 (Linux)
sudo ufw status
```

## 📈 성능 최적화

### 인덱스 확인
```sql
SHOW INDEX FROM users;
SHOW INDEX FROM preferred_regions;
```

### 쿼리 성능 분석
```sql
EXPLAIN SELECT * FROM users u 
JOIN preferred_regions pr ON u.id = pr.user_id 
WHERE u.is_enabled = TRUE;
```

## 🔒 보안 설정

### 전용 사용자 생성
```sql
CREATE USER 'user_service'@'%' IDENTIFIED BY 'secure_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON user_service.* TO 'user_service'@'%';
FLUSH PRIVILEGES;
```

## 📝 백업 및 복원

### 백업
```bash
# Docker 컨테이너에서 백업
docker exec user-service-mysql mysqldump -u root -prootpassword user_service > backup.sql
```

### 복원
```bash
# Docker 컨테이너에서 복원
docker exec -i user-service-mysql mysql -u root -prootpassword user_service < backup.sql
```

## 🎯 API 테스트

### 회원가입 테스트
```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "password123",
    "username": "새사용자",
    "phone": "010-9999-9999",
    "preferredRegions": [
      {
        "city": "seoul",
        "cityName": "서울특별시",
        "district": "gangnam",
        "districtName": "강남구",
        "priority": 1
      }
    ]
  }'
```

### 사용자 조회 테스트
```bash
curl http://localhost:8081/api/users/1
```

### 헬스 체크
```bash
curl http://localhost:8081/api/users/health
```
