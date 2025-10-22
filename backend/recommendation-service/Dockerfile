# Recommendation Service Dockerfile
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 애플리케이션 JAR 파일 복사
COPY target/recommendation-service-1.0.0.jar app.jar

# 포트 노출
EXPOSE 8083

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
