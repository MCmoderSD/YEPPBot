package de.MCmoderSD.main;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.main.Main.Argument;
import de.MCmoderSD.utilities.other.Reader;

import de.MCmoderSD.json.JsonUtility;

import java.util.HashSet;

@SuppressWarnings("unused")
public class Credentials {

    // Bot Credentials
    private JsonNode botConfig;
    private HashSet<String> channelList;
    private JsonNode mySQLConfig;
    private JsonNode httpsServerConfig;

    // API Credentials
    private JsonNode apiConfig;
    private JsonNode openAIConfig;
    private JsonNode openAIChatConfig;
    private JsonNode openAIImageConfig;
    private JsonNode openAITTSConfig;

    // APIs Provided
    private boolean openAI;
    private boolean astrology;
    private boolean openWeatherMap;
    private boolean giphy;

    // Constructor
    public Credentials(Main main, String botConfig, String channelList, String mySQL, String httpsServer, String apiKeys, String openAI) {

        // Get Utilities
        JsonUtility jsonUtility = main.getJsonUtility();
        Reader reader = main.getReader();

        // Load Bot Config
        try {
            this.botConfig = jsonUtility.load(botConfig, main.hasArg(Argument.BOT_CONFIG));
        } catch (Exception e) {
            System.err.println("Error loading Bot Config: " + e.getMessage());
            System.exit(1);
        }

        // Load Channel List
        try {
            this.channelList = new HashSet<>(reader.lineRead(channelList, main.hasArg(Argument.CHANNEL_LIST)));
        } catch (Exception e) {
            System.err.println("Error loading Channel List: " + e.getMessage());
        }

        // Load MySQL Config
        try {
            mySQLConfig = jsonUtility.load(mySQL, main.hasArg(Argument.MYSQL_CONFIG));
        } catch (Exception e) {
            System.err.println("Error loading MySQL Config: " + e.getMessage());
            System.exit(1);
        }

        // Load Http Server Config
        try {
            if (!(main.hasArg(Argument.HOST) && main.hasArg(Argument.PORT))) this.httpsServerConfig = jsonUtility.load(httpsServer, main.hasArg(Argument.HTTPS_SERVER));
        } catch (Exception e) {
            System.err.println("Error loading Http Server Config: " + e.getMessage());
        }

        // Load API Config
        try {
            apiConfig = jsonUtility.load(apiKeys, main.hasArg(Argument.API_CONFIG));
            if (apiConfig.has("astrology")) this.astrology = true;
            if (apiConfig.has("openWeatherMap")) this.openWeatherMap = true;
            if (apiConfig.has("giphy")) this.giphy = true;
        } catch (Exception e) {
            System.err.println("Error loading API Config: " + e.getMessage());
        }

        // Load OpenAI Config
        try {
            openAIConfig = jsonUtility.load(openAI, main.hasArg(Argument.OPENAI_CONFIG));
            if (openAIConfig.has("chat")) openAIChatConfig = openAIConfig.get("chat");
            if (openAIConfig.has("image")) openAIImageConfig = openAIConfig.get("image");
            if (openAIConfig.has("speech")) openAITTSConfig = openAIConfig.get("speech");
            this.openAI = true;
        } catch (Exception e) {
            System.err.println("Error loading OpenAI Config: " + e.getMessage());
        }
    }

    // Getters
    public JsonNode getBotConfig() {
        return botConfig;
    }

    public HashSet<String> getChannelList() {
        return channelList;
    }

    public JsonNode getMySQLConfig() {
        return mySQLConfig;
    }

    public JsonNode getHttpsServerConfig() {
        return httpsServerConfig;
    }

    public JsonNode getAPIConfig() {
        return apiConfig;
    }

    public JsonNode getOpenAIConfig() {
        return openAIConfig;
    }

    public JsonNode getOpenAIChatConfig() {
        return openAIChatConfig;
    }

    public JsonNode getOpenAIImageConfig() {
        return openAIImageConfig;
    }

    public JsonNode getOpenAITTSConfig() {
        return openAITTSConfig;
    }

    // Validations
    public boolean validateBotConfig() {
        return botConfig != null;
    }

    public boolean validateChannelList() {
        return channelList != null && !channelList.isEmpty();
    }

    public boolean validateMySQLConfig() {
        return mySQLConfig != null;
    }

    public boolean validateHttpServerConfig() {
        return httpsServerConfig != null;
    }

    public boolean validateAPIConfig() {
        return apiConfig != null;
    }

    public boolean validateOpenAIConfig() {
        return openAIConfig != null;
    }

    public boolean validateOpenAIChatConfig() {
        return openAIChatConfig != null;
    }

    public boolean validateOpenAIImageConfig() {
        return openAIImageConfig != null;
    }

    public boolean validateOpenAITTSConfig() {
        return openAITTSConfig != null;
    }

    // APIs Provided
    public boolean hasAstrology() {
        return astrology;
    }

    public boolean hasOpenWeatherMap() {
        return openWeatherMap;
    }

    public boolean hasGiphy() {
        return giphy;
    }

    public boolean hasOpenAI() {
        return openAI;
    }
}