@echo off
echo Starting User Service...

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Java is not installed or not in PATH
    pause
    exit /b 1
)

REM Create data directory if it doesn't exist
if not exist "data" mkdir data

REM Set environment variables
set SPRING_PROFILES_ACTIVE=dev
set SERVER_PORT=8081

echo Starting Spring Boot application...
echo Server will be available at: http://localhost:8081
echo H2 Console will be available at: http://localhost:8081/h2-console
echo.
echo Press Ctrl+C to stop the server
echo.

REM Try to run with Maven if available
mvn spring-boot:run >nul 2>&1
if %errorlevel% equ 0 (
    echo Server started with Maven
    goto :end
)

REM Try to run with Gradle if available
gradlew.bat bootRun >nul 2>&1
if %errorlevel% equ 0 (
    echo Server started with Gradle
    goto :end
)

REM If neither Maven nor Gradle is available, show instructions
echo.
echo Neither Maven nor Gradle is available.
echo Please install Maven or Gradle to run the server.
echo.
echo Alternative: Use an IDE like IntelliJ IDEA or Eclipse to run the application.
echo.

:end
pause
