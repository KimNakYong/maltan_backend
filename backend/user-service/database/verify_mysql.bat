@echo off
echo ========================================
echo MySQL user_service 데이터베이스 확인
echo ========================================
echo.

REM MySQL 설치 확인
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] MySQL이 설치되어 있지 않습니다.
    echo.
    echo MySQL 설치 방법:
    echo 1. https://dev.mysql.com/downloads/mysql/ 에서 MySQL 다운로드
    echo 2. 또는 https://www.apachefriends.org/ 에서 XAMPP 다운로드
    echo.
    pause
    exit /b 1
)

echo [INFO] MySQL이 설치되어 있습니다.
echo.

REM MySQL 서비스 확인
net start | findstr /i mysql >nul
if %errorlevel% neq 0 (
    echo [WARNING] MySQL 서비스가 실행되지 않았습니다.
    echo MySQL 서비스를 시작하세요.
    echo.
    echo XAMPP 사용 시:
    echo 1. XAMPP Control Panel 실행
    echo 2. MySQL Start 버튼 클릭
    echo.
    pause
    exit /b 1
)

echo [INFO] MySQL 서비스가 실행 중입니다.
echo.

REM 데이터베이스 확인
echo [INFO] user_service 데이터베이스 확인 중...
mysql -u root -p -e "SHOW DATABASES;" 2>nul | findstr /i user_service
if %errorlevel% neq 0 (
    echo [WARNING] user_service 데이터베이스가 존재하지 않습니다.
    echo.
    echo 데이터베이스를 생성하시겠습니까? (Y/N)
    set /p choice=
    if /i "%choice%"=="Y" (
        echo [INFO] 데이터베이스 생성 중...
        mysql -u root -p < quick_setup_mysql.sql
        if %errorlevel% equ 0 (
            echo [SUCCESS] user_service 데이터베이스가 성공적으로 생성되었습니다!
        ) else (
            echo [ERROR] 데이터베이스 생성에 실패했습니다.
        )
    )
) else (
    echo [SUCCESS] user_service 데이터베이스가 존재합니다.
    echo.
    echo [INFO] 테이블 확인 중...
    mysql -u root -p -e "USE user_service; SHOW TABLES;" 2>nul | findstr /i users
    if %errorlevel% equ 0 (
        echo [SUCCESS] users 테이블이 존재합니다.
        echo.
        echo [INFO] 데이터 확인:
        mysql -u root -p -e "USE user_service; SELECT COUNT(*) as user_count FROM users;"
    ) else (
        echo [WARNING] users 테이블이 존재하지 않습니다.
        echo quick_setup_mysql.sql을 실행하여 테이블을 생성하세요.
    )
)

echo.
echo [INFO] XShell에서 확인하는 방법:
echo 1. XShell에서 MySQL 서버에 접속
echo 2. SHOW DATABASES; 실행
echo 3. USE user_service; 실행
echo 4. SHOW TABLES; 실행
echo 5. SELECT * FROM users; 실행
echo.

pause
