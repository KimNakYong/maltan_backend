@echo off
echo ========================================
echo MySQL 설치 및 userdb 데이터베이스 생성
echo ========================================
echo.

echo [INFO] MySQL 설치 방법:
echo.
echo 방법 1: MySQL 공식 사이트에서 다운로드
echo 1. https://dev.mysql.com/downloads/mysql/ 접속
echo 2. MySQL Community Server 다운로드
echo 3. 설치 후 MySQL 서비스 시작
echo.
echo 방법 2: XAMPP 사용 (권장)
echo 1. https://www.apachefriends.org/ 접속
echo 2. XAMPP 다운로드 및 설치
echo 3. XAMPP Control Panel에서 MySQL 시작
echo.

echo [INFO] MySQL 설치 후 다음 명령어를 실행하세요:
echo.
echo 1. MySQL 서비스 시작 확인:
echo    net start | findstr /i mysql
echo.
echo 2. MySQL 접속:
echo    mysql -u root -p
echo.
echo 3. 데이터베이스 생성:
echo    CREATE DATABASE userdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
echo.
echo 4. 통합 테이블 생성:
echo    mysql -u root -p userdb ^< create_unified_userdb.sql
echo.
echo 5. 데이터베이스 확인:
echo    SHOW DATABASES;
echo    USE userdb;
echo    SHOW TABLES;
echo    SELECT * FROM users;
echo.

pause
