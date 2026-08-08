# Base Image JRE 25
FROM eclipse-temurin:25-jre-alpine

# Unprivileged runtime user
RUN addgroup -g 1000 -S app && adduser -u 1000 -S -G app app

# Set Up
WORKDIR /app
COPY --chown=app:app app.jar /app/app.jar

# Writable directory for generated certificates
RUN mkdir -p /app/certificates && chown -R app:app /app
USER app

# Expose Ports
EXPOSE 80/tcp
EXPOSE 420/tcp

# Run Application
ENTRYPOINT ["java", "--sun-misc-unsafe-memory-access=allow", "-jar", "/app/app.jar", "-c", "/app/config.json"]