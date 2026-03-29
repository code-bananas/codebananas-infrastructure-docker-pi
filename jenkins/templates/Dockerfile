# Multi-stage build for Spring Boot (Maven or Gradle)
# ── Stage 1: build the JAR ────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy dependency manifests first for layer caching
COPY pom.xml* mvnw* ./
COPY .mvn/ .mvn/
# Uncomment these two lines instead if using Gradle:
# COPY build.gradle* settings.gradle* gradlew* ./
# COPY gradle/ gradle/

# Download dependencies (cached as long as pom/build files don't change)
RUN if [ -f mvnw ]; then chmod +x mvnw && ./mvnw dependency:go-offline -q; \
    else mvn dependency:go-offline -q; fi

COPY src/ src/
RUN if [ -f mvnw ]; then ./mvnw clean package -DskipTests; \
    else mvn clean package -DskipTests; fi

# ── Stage 2: minimal runtime image ───────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=builder /build/target/*.jar app.jar
# Uncomment for Gradle:
# COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
