# Place Service Dockerfile
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 애플리케이션 JAR 파일 복사
COPY target/place-service-1.0.0.jar app.jar

# 업로드 디렉토리 생성
RUN mkdir -p /app/uploads

# 포트 노출
EXPOSE 8082

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/api/health || exit 1

# 애플리케이션 실행 (docker 프로파일 활성화 + headless 모드)
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-Dspring.profiles.active=docker", "-jar", "app.jar"]