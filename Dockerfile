# ─────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Cache Maven dependencies before copying source (layer cache optimization)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build JAR (skip tests for CI/CD pipeline; tests run separately)
COPY src ./src
RUN mvn package -DskipTests -B

# ─────────────────────────────────────────────
# Stage 2: Runtime
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the fat JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Use non-root user
USER appuser

EXPOSE 8080

# JVM tuned for low-memory container (matches docker-compose memory limit: 700m)
# -XX:MaxRAMPercentage limits heap to 70% of container memory (~490m)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=70.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
