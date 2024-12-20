FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
EXPOSE 443
ENTRYPOINT ["java", "-jar", "YEPPBot.jar", "-cli", "-ni", "-botconfig", "/config/botconfig.json", "-channellist", "/config/channellist.json", "-mysql", "/config/mysql.json", "-httpsserver", "/config/httpsserver.json", "-api", "/config/api.json", "-openai", "/config/openaiconfig.json"]