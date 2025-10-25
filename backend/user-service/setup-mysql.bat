@echo off
echo Setting up MySQL for User Service...

REM Create MySQL data directory
if not exist "mysql-data" mkdir mysql-data

echo.
echo MySQL Setup Instructions:
echo ========================
echo.
echo 1. Download MySQL from: https://dev.mysql.com/downloads/mysql/
echo 2. Install MySQL Server
echo 3. Start MySQL service
echo 4. Create database: user_service
echo.
echo OR use XAMPP:
echo 1. Download XAMPP from: https://www.apachefriends.org/
echo 2. Install and start XAMPP
echo 3. Start MySQL in XAMPP Control Panel
echo 4. Open http://localhost/phpmyadmin
echo 5. Create database: user_service
echo.

echo Database Configuration:
echo =======================
echo Host: localhost
echo Port: 3306
echo Database: user_service
echo Username: root
echo Password: (empty or your MySQL password)
echo.

echo After MySQL is set up, run: run-server.bat
echo.
pause
