# Multi-stage build for Spring Boot application
FROM openjdk:19-jdk-alpine AS build

# Install required packages
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Runtime stage
FROM openjdk:19-jdk-alpine

# Install tesseract for OCR
RUN apk add --no-cache tesseract-ocr tesseract-ocr-data-eng tesseract-ocr-data-kor

# Create app directory
RUN mkdir -p /app/logs

# Copy built jar from build stage
COPY --from=build /app/build/libs/*.jar /app/app.jar

# Set working directory
WORKDIR /app

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 