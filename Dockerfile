# Gunakan image Java 17 dan Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy semua file ke container
COPY . .
  
# Build aplikasi
RUN mvn clean package -DskipTests

# Image kedua untuk menjalankan aplikasi
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Port aplikasi (ubah jika perlu)
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
