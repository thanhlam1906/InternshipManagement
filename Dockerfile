# Build stage
FROM eclipse-temurin:26-jdk AS builder
WORKDIR /app
COPY backend/gradlew backend/build.gradle backend/settings.gradle ./
COPY backend/gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY backend/src src
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

# Run stage
FROM eclipse-temurin:26-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
