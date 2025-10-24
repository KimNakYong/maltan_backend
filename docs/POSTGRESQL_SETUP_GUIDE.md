# PostgreSQL 설치 및 설정 가이드 (Ubuntu Server)

## 📋 개요

Docker 외부에 PostgreSQL을 설치하여 커뮤니티 서비스 및 향후 다른 서비스의 데이터베이스로 사용합니다.

---

## 🔧 1. PostgreSQL 설치

### Ubuntu Server에서 실행

```bash
# 패키지 목록 업데이트
sudo apt update

# PostgreSQL 15 설치
sudo apt install -y postgresql-15 postgresql-contrib-15

# PostgreSQL 버전 확인
psql --version
# 출력: psql (PostgreSQL) 15.x
```

---

## 🚀 2. PostgreSQL 서비스 관리

```bash
# PostgreSQL 시작
sudo systemctl start postgresql

# 부팅 시 자동 시작 설정
sudo systemctl enable postgresql

# 서비스 상태 확인
sudo systemctl status postgresql

# PostgreSQL 재시작
sudo systemctl restart postgresql

# PostgreSQL 중지
sudo systemctl stop postgresql
```

---

## 🗄️ 3. 데이터베이스 및 사용자 생성

### Community Service용 데이터베이스

```bash
# postgres 사용자로 전환
sudo -u postgres psql

# PostgreSQL 콘솔에서 실행:
```

```sql
-- 커뮤니티 서비스용 데이터베이스 생성
CREATE DATABASE community_db
    WITH 
    ENCODING = 'UTF8'
    LC_COLLATE = 'ko_KR.UTF-8'
    LC_CTYPE = 'ko_KR.UTF-8'
    TEMPLATE = template0;

-- 커뮤니티 서비스용 사용자 생성
CREATE USER community_user WITH ENCRYPTED PASSWORD 'Community@2025!';

-- 권한 부여
GRANT ALL PRIVILEGES ON DATABASE community_db TO community_user;

-- 커넥션 연결 확인
\c community_db

-- 스키마 권한 부여
GRANT ALL ON SCHEMA public TO community_user;

-- 테이블 생성 권한 부여
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO community_user;

-- 나중을 위한 다른 서비스 DB (선택사항)
CREATE DATABASE user_db WITH ENCODING = 'UTF8' LC_COLLATE = 'ko_KR.UTF-8' LC_CTYPE = 'ko_KR.UTF-8' TEMPLATE = template0;
CREATE DATABASE place_db WITH ENCODING = 'UTF8' LC_COLLATE = 'ko_KR.UTF-8' LC_CTYPE = 'ko_KR.UTF-8' TEMPLATE = template0;

CREATE USER user_service WITH ENCRYPTED PASSWORD 'UserService@2025!';
CREATE USER place_service WITH ENCRYPTED PASSWORD 'PlaceService@2025!';

GRANT ALL PRIVILEGES ON DATABASE user_db TO user_service;
GRANT ALL PRIVILEGES ON DATABASE place_db TO place_service;

-- 데이터베이스 목록 확인
\l

-- 종료
\q
```

---

## 🔐 4. 외부 접속 설정 (Docker 컨테이너 접속 허용)

### postgresql.conf 수정

```bash
# 설정 파일 위치 확인
sudo -u postgres psql -c "SHOW config_file;"
# 보통: /etc/postgresql/15/main/postgresql.conf

# 설정 파일 편집
sudo vim /etc/postgresql/15/main/postgresql.conf
```

다음 내용 수정:
```conf
# 모든 네트워크 인터페이스에서 접속 허용
listen_addresses = '*'

# 최대 연결 수 (필요에 따라 조정)
max_connections = 100

# 공유 버퍼 (시스템 RAM의 25% 권장)
shared_buffers = 256MB

# 효율적인 캐시 크기 (시스템 RAM의 50-75%)
effective_cache_size = 1GB

# 로그 설정
logging_collector = on
log_directory = 'log'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'
log_statement = 'all'  # 개발: all, 운영: ddl 또는 mod
```

### pg_hba.conf 수정 (접근 제어)

```bash
sudo vim /etc/postgresql/15/main/pg_hba.conf
```

파일 끝에 추가:
```conf
# Docker 네트워크에서 접속 허용
# TYPE  DATABASE        USER            ADDRESS                 METHOD

# Docker 네트워크 (172.17.0.0/16, 172.18.0.0/16)
host    community_db    community_user  172.17.0.0/16           md5
host    community_db    community_user  172.18.0.0/16           md5

# 로컬 접속 (개발용)
host    community_db    community_user  127.0.0.1/32            md5
host    community_db    community_user  ::1/128                 md5

# 같은 서버의 모든 IP에서 접속 허용 (선택사항)
host    all             all             10.0.2.0/24             md5

# 향후 다른 서비스용
host    user_db         user_service    172.17.0.0/16           md5
host    user_db         user_service    172.18.0.0/16           md5
host    place_db        place_service   172.17.0.0/16           md5
host    place_db        place_service   172.18.0.0/16           md5
```

### PostgreSQL 재시작

```bash
sudo systemctl restart postgresql

# 로그 확인
sudo tail -f /var/log/postgresql/postgresql-15-main.log
```

---

## 🧪 5. 연결 테스트

### 로컬에서 테스트

```bash
# community_db 접속
psql -h localhost -U community_user -d community_db

# 비밀번호 입력: Community@2025!

# 연결 확인
\conninfo
# 출력: You are connected to database "community_db" as user "community_user" via socket in "/var/run/postgresql" at port "5432".

# 테이블 목록 확인
\dt

# 종료
\q
```

### Docker 컨테이너에서 테스트

```bash
# PostgreSQL 클라이언트가 있는 임시 컨테이너 실행
docker run -it --rm postgres:15-alpine psql -h 172.18.0.1 -U community_user -d community_db

# 또는 Ubuntu 서버의 IP 사용
docker run -it --rm postgres:15-alpine psql -h 10.0.2.15 -U community_user -d community_db
```

---

## 📊 6. 초기 스키마 생성

### 스키마 SQL 파일 생성

```bash
# 스키마 파일 생성
sudo vim /tmp/community_schema.sql
```

```sql
-- 게시글 테이블
CREATE TABLE IF NOT EXISTS posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    
    region_si VARCHAR(50) NOT NULL,
    region_gu VARCHAR(50),
    region_dong VARCHAR(50),
    
    is_recruitment BOOLEAN DEFAULT FALSE,
    recruitment_max INTEGER,
    recruitment_current INTEGER DEFAULT 0,
    recruitment_deadline TIMESTAMP,
    event_date TIMESTAMP,
    event_location VARCHAR(200),
    
    view_count INTEGER DEFAULT 0,
    like_count INTEGER DEFAULT 0,
    dislike_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    
    is_deleted BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 게시글 이미지 테이블
CREATE TABLE IF NOT EXISTS post_images (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    image_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- 댓글 테이블
CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_comment_id BIGINT,
    content TEXT NOT NULL,
    
    like_count INTEGER DEFAULT 0,
    dislike_count INTEGER DEFAULT 0,
    
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE
);

-- 게시글 추천/비추천 테이블
CREATE TABLE IF NOT EXISTS post_votes (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    vote_type VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    UNIQUE (post_id, user_id)
);

-- 댓글 추천/비추천 테이블
CREATE TABLE IF NOT EXISTS comment_votes (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    vote_type VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    UNIQUE (comment_id, user_id)
);

-- 모집 참여자 테이블
CREATE TABLE IF NOT EXISTS recruitment_participants (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'JOINED',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    UNIQUE (post_id, user_id)
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id);
CREATE INDEX IF NOT EXISTS idx_posts_category ON posts(category);
CREATE INDEX IF NOT EXISTS idx_posts_region ON posts(region_si, region_gu, region_dong);
CREATE INDEX IF NOT EXISTS idx_posts_recruitment ON posts(is_recruitment, recruitment_deadline);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_list ON posts(is_deleted, created_at DESC, category, region_si);

CREATE INDEX IF NOT EXISTS idx_post_images_post_id ON post_images(post_id);

CREATE INDEX IF NOT EXISTS idx_comments_post_id ON comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent_id ON comments(parent_comment_id);

CREATE INDEX IF NOT EXISTS idx_post_votes_post_user ON post_votes(post_id, user_id);
CREATE INDEX IF NOT EXISTS idx_comment_votes_comment_user ON comment_votes(comment_id, user_id);

CREATE INDEX IF NOT EXISTS idx_recruitment_post_status ON recruitment_participants(post_id, status);
CREATE INDEX IF NOT EXISTS idx_recruitment_user_id ON recruitment_participants(user_id);
```

### 스키마 적용

```bash
# 스키마 적용
psql -h localhost -U community_user -d community_db -f /tmp/community_schema.sql

# 테이블 생성 확인
psql -h localhost -U community_user -d community_db -c "\dt"
```

---

## 🔍 7. 모니터링 및 관리

### 데이터베이스 크기 확인

```sql
-- 데이터베이스별 크기
SELECT 
    pg_database.datname,
    pg_size_pretty(pg_database_size(pg_database.datname)) AS size
FROM pg_database
ORDER BY pg_database_size(pg_database.datname) DESC;

-- 테이블별 크기
SELECT 
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### 현재 연결 확인

```sql
SELECT 
    datname,
    usename,
    client_addr,
    state,
    query
FROM pg_stat_activity
WHERE datname = 'community_db';
```

### 슬로우 쿼리 로그

```bash
# postgresql.conf에 추가
sudo vim /etc/postgresql/15/main/postgresql.conf
```

```conf
# 1초 이상 걸리는 쿼리 로깅
log_min_duration_statement = 1000

# 쿼리 실행 계획 로깅
auto_explain.log_min_duration = 1000
```

---

## 🔄 8. 백업 및 복구

### 백업 스크립트 생성

```bash
# 백업 디렉토리 생성
sudo mkdir -p /var/backups/postgresql
sudo chown postgres:postgres /var/backups/postgresql

# 백업 스크립트 생성
sudo nano /usr/local/bin/backup_community_db.sh
```

```bash
#!/bin/bash
BACKUP_DIR="/var/backups/postgresql"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
DB_NAME="community_db"

# 백업 수행
sudo -u postgres pg_dump $DB_NAME | gzip > "$BACKUP_DIR/${DB_NAME}_$TIMESTAMP.sql.gz"

# 7일 이상 된 백업 삭제
find $BACKUP_DIR -name "${DB_NAME}_*.sql.gz" -mtime +7 -delete

echo "Backup completed: ${DB_NAME}_$TIMESTAMP.sql.gz"
```

```bash
# 실행 권한 부여
sudo chmod +x /usr/local/bin/backup_community_db.sh

# 매일 새벽 2시에 자동 백업 (cron)
sudo crontab -e
```

cron에 추가:
```
0 2 * * * /usr/local/bin/backup_community_db.sh >> /var/log/postgresql_backup.log 2>&1
```

### 복구

```bash
# 백업 파일 복구
gunzip -c /var/backups/postgresql/community_db_20251024_020000.sql.gz | \
    psql -h localhost -U community_user -d community_db
```

---

## 🛡️ 9. 보안 강화

### 비밀번호 정책

```sql
-- 비밀번호 변경
ALTER USER community_user WITH PASSWORD 'NewStrongPassword@2025!';

-- 비밀번호 만료 설정 (90일)
ALTER USER community_user VALID UNTIL '2026-01-22';
```

### SSL/TLS 활성화 (선택사항)

```bash
# 자체 서명 인증서 생성
sudo -u postgres openssl req -new -x509 -days 365 -nodes -text \
    -out /var/lib/postgresql/15/main/server.crt \
    -keyout /var/lib/postgresql/15/main/server.key

sudo chmod 600 /var/lib/postgresql/15/main/server.key
sudo chown postgres:postgres /var/lib/postgresql/15/main/server.*

# postgresql.conf에서 SSL 활성화
sudo nano /etc/postgresql/15/main/postgresql.conf
```

```conf
ssl = on
ssl_cert_file = '/var/lib/postgresql/15/main/server.crt'
ssl_key_file = '/var/lib/postgresql/15/main/server.key'
```

---

## 📈 10. 성능 튜닝

### shared_buffers 조정

```conf
# 시스템 RAM의 25% 권장
# RAM 4GB인 경우:
shared_buffers = 1GB
```

### work_mem 조정

```conf
# 복잡한 쿼리용 메모리 (정렬, 해시 조인)
work_mem = 16MB
```

### effective_cache_size

```conf
# 시스템 RAM의 50-75%
# RAM 4GB인 경우:
effective_cache_size = 3GB
```

### 연결 풀링 (PgBouncer 설치 - 선택사항)

```bash
sudo apt install pgbouncer

# 설정 파일 편집
sudo nano /etc/pgbouncer/pgbouncer.ini
```

```ini
[databases]
community_db = host=localhost port=5432 dbname=community_db

[pgbouncer]
listen_port = 6432
listen_addr = *
auth_type = md5
auth_file = /etc/pgbouncer/userlist.txt
pool_mode = transaction
max_client_conn = 100
default_pool_size = 20
```

---

## ✅ 11. 확인 체크리스트

- [ ] PostgreSQL 15 설치 완료
- [ ] 서비스 자동 시작 설정
- [ ] community_db 데이터베이스 생성
- [ ] community_user 사용자 생성 및 권한 부여
- [ ] 외부 접속 설정 (postgresql.conf, pg_hba.conf)
- [ ] Docker 네트워크에서 접속 테스트
- [ ] 초기 스키마 생성
- [ ] 백업 스크립트 설정
- [ ] 모니터링 설정

---

## 🚀 12. Spring Boot 연결 설정

### application.yml (Community Service)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://10.0.2.15:5432/community_db
    username: community_user
    password: Community@2025!
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: validate  # 운영: validate, 개발: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        default_batch_fetch_size: 100
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
```

### Docker Compose (Community Service)

```yaml
services:
  community-service:
    build: ./backend/community-service
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://10.0.2.15:5432/community_db
      - SPRING_DATASOURCE_USERNAME=community_user
      - SPRING_DATASOURCE_PASSWORD=Community@2025!
    networks:
      - maltan-network
    depends_on:
      - gateway-service

networks:
  maltan-network:
    driver: bridge
```

---

**작성일**: 2025-10-24  
**PostgreSQL 버전**: 15.x  
**대상 OS**: Ubuntu 22.04 LTS

