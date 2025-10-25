# 멀티 스테이지 빌드를 사용한 Dockerfile

# 1단계: 빌드 스테이지
FROM openjdk:17-jdk-slim AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Maven Wrapper와 pom.xml 복사
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# 의존성 다운로드 (캐시 최적화)
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드
RUN ./mvnw clean package -DskipTests

# 2단계: 실행 스테이지
FROM openjdk:17-jre-slim

# 필요한 패키지 설치
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 작업 디렉토리 설정
WORKDIR /app

# 업로드 디렉토리 생성
RUN mkdir -p /app/uploads && chmod 755 /app/uploads

# 빌드된 JAR 파일 복사
COPY --from=builder /app/target/*.jar app.jar

# 애플리케이션 실행을 위한 사용자 생성
RUN addgroup --system spring && adduser --system spring --ingroup spring
RUN chown -R spring:spring /app
USER spring:spring

# 포트 노출
EXPOSE 8082

# 환경 변수 설정
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker

# 헬스 체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/api/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]