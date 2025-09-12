# FROM --platform=linux/amd64 openjdk:21-slim-buster
FROM openjdk:21-slim-buster
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
# ENV JAVA_OPTS=""

# 메인 실행
# CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
CMD ["sh"]