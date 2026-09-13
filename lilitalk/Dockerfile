# Build stage
FROM gradle:9.7.1-jdk25 AS build

WORKDIR /app
COPY . .

# Gradle 빌드 실행
RUN gradle clean build -x test --no-daemon

# Runtime stage
FROM amazoncorretto:25-alpine-jdk

WORKDIR /app

# 애플리케이션 JAR 복사
COPY --from=build /app/chat-application/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]