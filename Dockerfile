FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY /*.jar /app/YEPPBot.jar
EXPOSE 443
ENTRYPOINT ["java", "-jar", "/app/YEPPBot.jar", "-docker"]