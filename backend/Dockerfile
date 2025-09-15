# FROM --platform=linux/amd64 openjdk:21-slim-buster
FROM openjdk:21-slim-buster
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]