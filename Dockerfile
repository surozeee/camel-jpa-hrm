# ===========================
# 1. Build Stage
# ===========================
FROM gradle:8.14.3-jdk21 AS builder

# Set work directory
WORKDIR /app

# Copy Gradle wrapper and build scripts (for caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Download dependencies (cache this layer)
RUN ./gradlew dependencies --no-daemon || return 0

# Copy rest of the source code
COPY . .

# Build the JAR with dev profile
RUN ./gradlew bootJar -x test -Dspring.profiles.active=dev --no-daemon

# ===========================
# 2. Runtime Stage
# ===========================
FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

# Copy only the JAR
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port (Spring Boot default)
EXPOSE 9000

# Run the JAR with dev profile by default
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]
