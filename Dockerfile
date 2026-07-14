# ============================================================
# Stage 1 — Build Frontend (React + Vite)
# ============================================================
FROM node:22-alpine AS frontend-builder
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

# ============================================================
# Stage 2 — Build Backend (Spring Boot + Gradle)
# ============================================================
FROM eclipse-temurin:26-jdk AS backend-builder
WORKDIR /app
COPY backend/gradlew backend/build.gradle backend/settings.gradle ./
COPY backend/gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY backend/src src
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

# ============================================================
# Stage 3 — Runtime (JRE + Frontend static files)
# ============================================================
FROM eclipse-temurin:26-jre
WORKDIR /app

# Copy backend JAR
COPY --from=backend-builder /app/build/libs/*.jar app.jar

# Copy frontend build into Spring Boot's static resource location
COPY --from=frontend-builder /frontend/dist /app/static

EXPOSE 10000

# Serve frontend static files from /app/static (mounted from frontend build stage)
# spa.static-locations tells SpaWebConfig where to find frontend files
# PORT is set by Render automatically
ENTRYPOINT ["sh", "-c", "java -Xmx256m -Xss512k -Dserver.port=${PORT:-8080} -Dapp.spa.static-locations=file:/app/static/ -jar app.jar"]
