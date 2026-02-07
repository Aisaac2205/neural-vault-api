# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port Railway will use
EXPOSE 8080

# Railway provides DATABASE_URL environment variable
# Spring Boot will automatically use it if configured properly
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
