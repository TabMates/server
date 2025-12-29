# Multi-stage Dockerfile for Spring Boot Kotlin application

# Stage 1: Build stage
FROM gradle:8.14-jdk21 AS build

# Set working directory
WORKDIR /app

# Copy gradle configuration files
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle gradle/
COPY build-logic build-logic/

# Make gradlew executable
RUN chmod +x gradlew

# Copy all module build files first for better caching
COPY app/build.gradle.kts app/
COPY common/build.gradle.kts common/

# Download dependencies (this layer will be cached unless dependencies change)
RUN ./gradlew dependencies --no-daemon || return 0

# Copy all source code
# We copy the entire module structure to handle this
COPY app/src app/src
COPY common/src common/src

# Build the application
RUN ./gradlew :app:bootJar --no-daemon -x test

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install required packages for better security, debugging, and health checks
RUN apk add --no-cache \
    tzdata \
    wget

# Create a non-root user to run the application
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose the application port
EXPOSE 8080

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

