# Use OpenJDK 24 as base image
FROM openjdk:24-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built JAR file
COPY build/libs/auth-service-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/auth/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
