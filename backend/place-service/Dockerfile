# 가장 안정적인 Dockerfile - 공식 OpenJDK 이미지 사용

# 1단계: 빌드 스테이지
FROM openjdk:17-jdk AS builder

# Maven 설치
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# 작업 디렉토리 설정
WORKDIR /app

# pom.xml과 소스 코드 복사
COPY pom.xml .
COPY src ./src

# Maven으로 JAR 파일 빌드
RUN mvn clean package -DskipTests -B

# 2단계: 실행 스테이지
FROM openjdk:17-jre

# 작업 디렉토리 설정
WORKDIR /app

# 필요한 패키지 설치 (curl for health check)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 업로드 디렉토리 생성
RUN mkdir -p /app/uploads

# 빌드 스테이지에서 JAR 파일 복사
COPY --from=builder /app/target/place-service-1.0.0.jar app.jar

# 포트 노출
EXPOSE 8082

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/api/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]