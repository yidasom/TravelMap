# Gradle build
# FROM gradle:8.14-jdk21 AS builder
# WORKDIR /app
# COPY build.gradle settings.gradle ./
# COPY gradle ./gradle
# COPY src ./src
# RUN gradle build -x test
#
# FROM openjdk:21-slim-buster
# WORKDIR /app
# COPY --from=builder /app/build/libs/*.jar app.jar
# EXPOSE 8080
# CMD ["java", "-jar", "app.jar"]

# FROM openjdk:21-slim-buster
# WORKDIR /app
# COPY --from=build /app/build/libs/*.jar app.jar
# EXPOSE 8080
# CMD ["java", "-jar", "app.jar"]

FROM gradle:8.14-jdk21 AS build
WORKDIR /app
COPY --chown=gradle:gradle backend/ /app
RUN ./gradlew clean build -x test


FROM openjdk:21-slim-buster
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]