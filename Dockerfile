FROM openjdk:17-jdk-slim

WORKDIR /app
COPY build/libs/hhplus-e-commerce.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
