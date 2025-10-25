@echo off
echo ========================================
echo 통합 사용자 데이터베이스 설정 스크립트
echo ========================================
echo.

REM MySQL 설치 확인
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] MySQL이 설치되어 있지 않습니다.
    echo.
    echo MySQL 설치 방법:
    echo 1. https://dev.mysql.com/downloads/mysql/ 에서 MySQL 다운로드
    echo 2. MySQL Server 설치
    echo 3. MySQL 서비스 시작
    echo.
    echo 또는 XAMPP 사용:
    echo 1. https://www.apachefriends.org/ 에서 XAMPP 다운로드
    echo 2. XAMPP 설치 후 MySQL 시작
    echo.
    pause
    exit /b 1
)

echo [INFO] MySQL이 설치되어 있습니다.
echo.

REM MySQL 서비스 시작 확인
net start | findstr /i mysql >nul
if %errorlevel% neq 0 (
    echo [WARNING] MySQL 서비스가 실행되지 않았습니다.
    echo MySQL 서비스를 시작하세요.
    echo.
    echo Windows 서비스에서 MySQL 시작:
    echo 1. services.msc 실행
    echo 2. MySQL 서비스 찾기
    echo 3. 시작 버튼 클릭
    echo.
    pause
    exit /b 1
)

echo [INFO] MySQL 서비스가 실행 중입니다.
echo.

REM 기존 데이터베이스 삭제 (선택사항)
echo [INFO] 기존 userdb 데이터베이스를 삭제합니다...
mysql -u root -p -e "DROP DATABASE IF EXISTS userdb;"

REM 데이터베이스 생성
echo [INFO] 통합 userdb 데이터베이스를 생성합니다...
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS userdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

if %errorlevel% neq 0 (
    echo [ERROR] 데이터베이스 생성에 실패했습니다.
    echo MySQL root 비밀번호를 확인하세요.
    pause
    exit /b 1
)

echo [INFO] 데이터베이스가 성공적으로 생성되었습니다.
echo.

REM 테이블 생성
echo [INFO] 통합 테이블을 생성합니다...
mysql -u root -p userdb < create_unified_userdb.sql

if %errorlevel% neq 0 (
    echo [ERROR] 테이블 생성에 실패했습니다.
    pause
    exit /b 1
)

echo [SUCCESS] 모든 설정이 완료되었습니다!
echo.
echo 통합 데이터베이스 정보:
echo - 데이터베이스: userdb
echo - 테이블: users (통합 테이블)
echo - 선호지역: JSON 형태로 저장
echo - 호스트: localhost
echo - 포트: 3306
echo - 사용자: root
echo.
echo 이제 Spring Boot 서버를 실행할 수 있습니다.
echo.
pause
