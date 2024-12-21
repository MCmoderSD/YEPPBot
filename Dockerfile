FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the JAR file
COPY target/YEPPBot.jar /app/YEPPBot.jar

# Debug: List the contents of the current working directory
RUN echo "Contents of /app:" && ls -l /app

# Debug: Check file type of the JAR file
RUN echo "File type of YEPPBot.jar:" && file /app/YEPPBot.jar

# Debug: Show Java version
RUN echo "Java version in container:" && java -version

# Debug: Verify permissions of the JAR file
RUN echo "Permissions of YEPPBot.jar:" && ls -l /app/YEPPBot.jar

# Debug: Verify if the JAR file is executable (in case of permission issues)
RUN echo "Checking if YEPPBot.jar is executable:" && chmod +x /app/YEPPBot.jar && ls -l /app/YEPPBot.jar

# Debug: Check for installed packages and environment info
RUN echo "Installed packages and environment info:" && apk info && env

# Expose the port (if needed)
EXPOSE 443

# Start the application (main entry point)
ENTRYPOINT ["java", "-jar", "YEPPBot.jar", "-docker"]
