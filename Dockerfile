FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/YEPPBot.jar /app/YEPPBot.jar
EXPOSE 443
ENTRYPOINT ["java", "-jar", "YEPPBot.jar", "-docker"]