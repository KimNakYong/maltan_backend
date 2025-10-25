# 우분투 MySQL userdb와 유저서비스 연결 가이드

## 🔗 연결 단계별 가이드

### 1. MySQL 서비스 상태 확인
```bash
# MySQL 서비스 상태 확인
sudo systemctl status mysql

# MySQL 서비스 시작 (중지된 경우)
sudo systemctl start mysql

# MySQL 서비스 자동 시작 설정
sudo systemctl enable mysql
```

### 2. MySQL 접속 및 데이터베이스 확인
```bash
# MySQL 접속
mysql -u root -p

# 데이터베이스 목록 확인
SHOW DATABASES;

# userdb 사용
USE userdb;

# 테이블 목록 확인
SHOW TABLES;

# users 테이블 구조 확인
DESCRIBE users;

# 데이터 확인
SELECT * FROM users;
```

### 3. Spring Boot 설정 파일 수정

#### application.yml 설정
```yaml
server:
  port: 8081
  servlet:
    context-path: /api

spring:
  application:
    name: user-service
  
  # MySQL 데이터베이스 연결 설정
  datasource:
    url: jdbc:mysql://localhost:3306/userdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
    username: root
    password: your_mysql_password  # 실제 MySQL 비밀번호로 변경
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  # JPA 설정
  jpa:
    hibernate:
      ddl-auto: update  # 테이블 자동 생성/업데이트
    show-sql: true      # SQL 쿼리 로그 출력
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
  
  # Spring Security 설정
  security:
    user:
      name: admin
      password: admin123

# 로깅 설정
logging:
  level:
    com.example.userservice: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

# Actuator 설정
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

### 4. Maven 의존성 확인

#### pom.xml에 MySQL 의존성이 있는지 확인
```xml
<dependencies>
    <!-- MySQL Connector -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- H2 Database (테스트용) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### 5. 서버 실행 및 테스트

#### 서버 실행
```bash
# 프로젝트 디렉토리로 이동
cd /path/to/your/user-service

# Maven으로 서버 실행
mvn spring-boot:run

# 또는 JAR 파일로 실행
mvn clean package
java -jar target/user-service-1.0.0.jar
```

#### API 테스트
```bash
# 헬스 체크
curl http://localhost:8081/api/users/health

# 회원가입 테스트
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "username": "테스트사용자",
    "phone": "010-1234-5678",
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

# 사용자 조회 테스트
curl http://localhost:8081/api/users/1
```

### 6. 연결 문제 해결

#### MySQL 연결 오류
```bash
# MySQL 서비스 상태 확인
sudo systemctl status mysql

# MySQL 로그 확인
sudo tail -f /var/log/mysql/error.log

# MySQL 재시작
sudo systemctl restart mysql
```

#### 방화벽 설정
```bash
# MySQL 포트 열기
sudo ufw allow 3306

# 방화벽 상태 확인
sudo ufw status
```

#### 권한 문제
```bash
# MySQL 사용자 권한 확인
mysql -u root -p -e "SELECT user, host FROM mysql.user;"

# 새 사용자 생성 (선택사항)
mysql -u root -p -e "CREATE USER 'userdb'@'localhost' IDENTIFIED BY 'password';"
mysql -u root -p -e "GRANT ALL PRIVILEGES ON userdb.* TO 'userdb'@'localhost';"
```

### 7. 데이터베이스 연결 확인

#### Spring Boot 로그에서 확인
```
# 성공적인 연결 로그
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

#### MySQL에서 연결 확인
```sql
-- 현재 연결된 세션 확인
SHOW PROCESSLIST;

-- 데이터베이스 사용량 확인
SELECT 
    table_schema as 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) as 'Size (MB)'
FROM information_schema.tables 
WHERE table_schema = 'userdb'
GROUP BY table_schema;
```

### 8. 성능 최적화

#### MySQL 설정 최적화
```bash
# MySQL 설정 파일 수정
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf

# 다음 설정 추가
[mysqld]
innodb_buffer_pool_size = 128M
max_connections = 100
query_cache_size = 16M
```

#### 인덱스 확인
```sql
-- 인덱스 확인
SHOW INDEX FROM users;

-- 쿼리 성능 확인
EXPLAIN SELECT * FROM users WHERE username = 'admin';
```

### 9. 백업 및 복원

#### 데이터베이스 백업
```bash
# 전체 데이터베이스 백업
mysqldump -u root -p userdb > userdb_backup.sql

# 특정 테이블만 백업
mysqldump -u root -p userdb users > users_backup.sql
```

#### 데이터베이스 복원
```bash
# 백업에서 복원
mysql -u root -p userdb < userdb_backup.sql
```

### 10. 모니터링

#### 시스템 리소스 확인
```bash
# MySQL 프로세스 확인
ps aux | grep mysql

# MySQL 메모리 사용량 확인
mysql -u root -p -e "SHOW STATUS LIKE 'Innodb_buffer_pool_pages%';"

# 연결 수 확인
mysql -u root -p -e "SHOW STATUS LIKE 'Threads_connected';"
```

## 🎯 완료 확인 체크리스트

- [ ] MySQL 서비스 실행 중
- [ ] userdb 데이터베이스 존재
- [ ] users 테이블 생성됨
- [ ] Spring Boot application.yml 설정 완료
- [ ] 서버 실행 성공
- [ ] API 테스트 성공
- [ ] 데이터베이스 연결 확인
- [ ] 로그에 오류 없음

## 🚀 다음 단계

1. **프론트엔드 연결** - CORS 설정 확인
2. **보안 강화** - JWT 토큰 인증
3. **로드 밸런싱** - 다중 서버 구성
4. **모니터링** - 로그 및 메트릭 수집
