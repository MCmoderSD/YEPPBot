# Base Image JRE 25
FROM eclipse-temurin:25-jre-alpine

# Set Up
WORKDIR /app
COPY /YEPPBot.jar /app/YEPPBot.jar

# Expose Ports
EXPOSE 420

# Run Application
ENTRYPOINT ["java", "-jar", "/app/YEPPBot.jar", "-c", "/app/config.json"]