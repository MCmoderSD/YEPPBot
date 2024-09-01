package de.MCmoderSD.main;

import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.main.Main.Argument;
import de.MCmoderSD.utilities.json.JsonUtility;
import de.MCmoderSD.utilities.other.Reader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Credentials {

    // Bot Credentials
    private JsonNode botConfig;
    private Set<String> channelList;
    private JsonNode mySQLConfig;

    // API Cials
    private JsonNode openAiConfig;
    private JsonNode openWeatherMapConfig;
    private JsonNode giphyConfig;

    // Constructor
    public Credentials(Main main, String botConfig, String channelList, String mySQL, String openAi, String weather, String giphy) {

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
            this.mySQLConfig = jsonUtility.load(mySQL, main.hasArg(Argument.MYSQL_CONFIG));
        } catch (Exception e) {
            System.err.println("Error loading MySQL Config: " + e.getMessage());
            System.exit(1);
        }

        // Load OpenAi Config
        try {
            this.openAiConfig = jsonUtility.load(openAi, main.hasArg(Argument.OPENAi_CONFIG));
        } catch (Exception e) {
            System.err.println("Error loading OpenAi Config: " + e.getMessage());
        }

        // Load OpenWeatherMap Config
        try {
            this.openWeatherMapConfig = jsonUtility.load(weather, main.hasArg(Argument.OPENWEATHERMAP_CONFIG));
        } catch (Exception e) {
            System.err.println("Error loading OpenWeatherMap Config: " + e.getMessage());
        }

        // Load Giphy Config
        try {
            this.giphyConfig = jsonUtility.load(giphy, main.hasArg(Argument.GIPHY_CONFIG));
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

    public JsonNode getOpenAiConfig() {
        return openAiConfig;
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

    public boolean validateOpenAiConfig() {
        return openAiConfig != null;
    }

    public boolean validateWeatherConfig() {
        return openWeatherMapConfig != null;
    }

    public boolean validateGiphyConfig() {
        return giphyConfig != null;
    }
}