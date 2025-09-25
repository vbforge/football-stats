FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy binary code into current dir
COPY target/football-stats-0.0.1-SNAPSHOT.jar .

# Expose port
EXPOSE 8080

# Run application
CMD ["java", "-jar", "football-stats-0.0.1-SNAPSHOT.jar"]