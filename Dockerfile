# 1. 베이스 이미지로 OpenJDK 17 사용
FROM openjdk:17-jdk-slim

# 2. 작업 디렉터리 설정
WORKDIR /app


COPY gradle /app/gradle
COPY gradlew /app/gradlew
COPY build.gradle /app/build.gradle
COPY settings.gradle /app/settings.gradle
COPY src /app/src
RUN chmod +x gradlew
# 4. Gradle을 통해 프로젝트 빌드
RUN ./gradlew build -x test --no-daemon

# 5. 생성된 JAR 파일을 실행 가능하도록 설정
EXPOSE 8080
CMD ["java", "-jar", "build/libs/sometimes-0.0.1-SNAPSHOT.jar"]