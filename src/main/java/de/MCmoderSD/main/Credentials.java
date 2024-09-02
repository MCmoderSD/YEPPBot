package de.MCmoderSD.main;

import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.main.Main.Argument;
import de.MCmoderSD.utilities.json.JsonUtility;
import de.MCmoderSD.utilities.other.Reader;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class Credentials {

    // Bot Credentials
    private JsonNode botConfig;
    private Set<String> channelList;
    private JsonNode mySQLConfig;
    private JsonNode httpServerConfig;

    // API Cials
    private JsonNode openAIConfig;
    private JsonNode openAIChatConfig;
    private JsonNode openAIImageConfig;
    private JsonNode openAITTSConfig;
    private JsonNode openWeatherMapConfig;
    private JsonNode giphyConfig;

    // Constructor
    public Credentials(Main main, String botConfig, String channelList, String mySQL, String httpServer, String openAI, String weather, String giphy) {

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
            if (!(main.hasArg(Argument.HOST) && main.hasArg(Argument.PORT))) this.httpServerConfig = jsonUtility.load(httpServer, main.hasArg(Argument.HTTP_SERVER));
        } catch (Exception e) {
            System.err.println("Error loading Http Server Config: " + e.getMessage());
        }

        // Load OpenAI Config
        try {
            openAIConfig = jsonUtility.load(openAI, main.hasArg(Argument.OPENAI_CONFIG));
            if (openAIConfig.has("chat")) openAIChatConfig = openAIConfig.get("chat");
            if (openAIConfig.has("image")) openAIImageConfig = openAIConfig.get("image");
            if (openAIConfig.has("speech")) openAITTSConfig = openAIConfig.get("speech");
        } catch (Exception e) {
            System.err.println("Error loading OpenAI Config: " + e.getMessage());
        }

        // Load OpenWeatherMap Config
        try {
            openWeatherMapConfig = jsonUtility.load(weather, main.hasArg(Argument.OPENWEATHERMAP_CONFIG));
        } catch (Exception e) {
            System.err.println("Error loading OpenWeatherMap Config: " + e.getMessage());
        }

        // Load Giphy Config
        try {
            giphyConfig = jsonUtility.load(giphy, main.hasArg(Argument.GIPHY_CONFIG));
        } catch (Exception e) {
            System.err.println("Error loading Giphy Config: " + e.getMessage());
        }

    }

    // Getters
    public JsonNode getBotConfig() {
        return botConfig;
    }

    public Set<String> getChannelList() {
        return channelList;
    }

    public JsonNode getMySQLConfig() {
        return mySQLConfig;
    }

    public JsonNode getHttpServerConfig() {
        return httpServerConfig;
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

    public JsonNode getOpenWeatherMapConfig() {
        return openWeatherMapConfig;
    }

    public JsonNode getGiphyConfig() {
        return giphyConfig;
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
        return httpServerConfig != null;
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

    public boolean validateWeatherConfig() {
        return openWeatherMapConfig != null;
    }

    public boolean validateGiphyConfig() {
        return giphyConfig != null;
    }
}