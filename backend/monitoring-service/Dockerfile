FROM maven:3.9.5-eclipse-temurin-17-alpine AS build

WORKDIR /app

# pom.xml 복사 및 의존성 다운로드
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 소스 코드 복사 및 빌드
COPY src ./src
RUN mvn clean package -DskipTests

# 실행 단계
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/target/app.jar app.jar

# Docker 소켓 접근을 위한 설정
RUN apk add --no-cache docker-cli

# 포트 노출
EXPOSE 8085

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

