package de.MCmoderSD.main;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashSet;

import static de.MCmoderSD.main.Main.jsonUtility;
import static de.MCmoderSD.main.Main.terminal;
import static de.MCmoderSD.utilities.other.Util.readAllLines;


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
    private boolean astrology;
    private boolean openWeatherMap;
    private boolean giphy;

    // Constructor
    public Credentials(String botConfig, String channelList, String mySQL, String httpsServer, String apiKeys, String openAI) {

        // Load Bot Config
        try {
            this.botConfig = jsonUtility.load(botConfig, terminal.hasArg(Terminal.Argument.BOT_CONFIG));
        } catch (Exception e) {
            System.err.println("Error loading Bot Config: " + e.getMessage());
            System.exit(1);
        }

        // Load Channel List
        try {
            this.channelList = new HashSet<>(readAllLines(channelList, terminal.hasArg(Terminal.Argument.CHANNEL_LIST)));
        } catch (Exception e) {
            System.err.println("Error loading Channel List: " + e.getMessage());
        }

        // Load MySQL Config
        try {
            this.mySQLConfig = jsonUtility.load(mySQL, terminal.hasArg(Terminal.Argument.MYSQL_CONFIG));
        } catch (Exception e) {
            System.err.println("Error loading MySQL Config: " + e.getMessage());
        }

        // Load HTTPS Server Config
        try {
            this.httpsServerConfig = jsonUtility.load(httpsServer, terminal.hasArg(Terminal.Argument.HTTPS_SERVER));
        } catch (Exception e) {
            System.err.println("Error loading HTTPS Server Config: " + e.getMessage());
        }

        // Load API Config
        try {

            // Load JSON
            apiConfig = jsonUtility.load(apiKeys, terminal.hasArg(Terminal.Argument.API_CONFIG));

            // Check for APIs
            astrology = apiConfig.has("astrology");
            openWeatherMap = apiConfig.has("openWeatherMap");
            giphy = apiConfig.has("giphy");

        } catch (Exception e) {
            System.err.println("Error loading API Config: " + e.getMessage());
        }

        // Load OpenAI Config
        try {

            // Load JSON
            openAIConfig = jsonUtility.load(openAI, terminal.hasArg(Terminal.Argument.OPENAI_CONFIG));

            // Load OpenAI Sub Configs
            if (openAIConfig.has("chat")) openAIChatConfig = openAIConfig.get("chat");
            if (openAIConfig.has("image")) openAIImageConfig = openAIConfig.get("image");
            if (openAIConfig.has("speech")) openAITTSConfig = openAIConfig.get("speech");
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
        return openAIConfig != null;
    }

    public boolean hasOpenAIChat() {
        return openAIChatConfig != null;
    }

    public boolean hasOpenAIImage() {
        return openAIImageConfig != null;
    }

    public boolean hasOpenAITTS() {
        return openAITTSConfig != null;
    }
}