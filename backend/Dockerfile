# FROM gradle:8.14-jdk21 AS build
# WORKDIR /app
# COPY --chown=gradle:gradle backend/ /app
# RUN ./gradlew clean build -x test
#
# FROM openjdk:21-slim-buster
# WORKDIR /app
# COPY --from=build /app/build/libs/*.jar app.jar
# EXPOSE 8080
# CMD ["java", "-jar", "app.jar"]

FROM openjdk:21-slim-buster
WORKDIR /app
COPY backend/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
