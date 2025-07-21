# Stage 1: Build
FROM maven:3.8.5-openjdk-17-slim AS build
COPY . /app
WORKDIR /app
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM openjdk:17-jdk-slim
COPY --from=build /app/target/*.jar app.jar
EXPOSE 4000
ENTRYPOINT ["java", "-jar", "app.jar"]

