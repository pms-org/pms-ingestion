FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the app runs on (default Spring Boot port)
EXPOSE 8081

# Run the application with Docker profile
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]