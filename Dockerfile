
# Base Image JRE 21
FROM eclipse-temurin:21-jre-alpine

# Set Up
WORKDIR /app
COPY /YEPPBot.jar /app/YEPPBot.jar

# Expose Ports
EXPOSE 420

# Run Application
ENTRYPOINT ["java", "-jar", "/app/YEPPBot.jar", "-c", "/app/config.json"]