# ===========================
# 1. Build Stage
# ===========================
FROM gradle:8.14.3-jdk21 AS builder

WORKDIR /app

# Copy Gradle wrapper and build scripts (for caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Download dependencies (cache this layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy only sources — never IDE bin/ or local build/
COPY src src

# Build the JAR with dev profile
RUN ./gradlew clean bootJar -x test -Dspring.profiles.active=dev --no-daemon \
    && jar tf build/libs/Camel-Jpa-TN-0.0.1-SNAPSHOT.jar > /tmp/jar-contents.txt \
    && (grep -E 'BranchAddressEntity|EmployeeAddressEntity' /tmp/jar-contents.txt \
        && echo "ERROR: stale dual address entities present in jar" && exit 1 \
        || echo "OK: single AddressEntity mapping") \
    && (grep 'model/postgres/master/BranchEntity.class' /tmp/jar-contents.txt \
        && echo "ERROR: master BranchEntity still present (rename to BankBranchEntity)" && exit 1 \
        || echo "OK: no master BranchEntity class") \
    && (grep 'model/postgres/master/ExperienceEntity.class' /tmp/jar-contents.txt \
        && echo "ERROR: master ExperienceEntity still present (rename to MasterExperienceEntity)" && exit 1 \
        || echo "OK: no master ExperienceEntity class") \
    && grep -q 'model/postgres/master/BankBranchEntity.class' /tmp/jar-contents.txt \
    && grep -q 'model/postgres/company/BranchEntity.class' /tmp/jar-contents.txt

# ===========================
# 2. Runtime Stage
# ===========================
FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

# Copy only the fat jar (not *-plain.jar)
COPY --from=builder /app/build/libs/Camel-Jpa-TN-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]
