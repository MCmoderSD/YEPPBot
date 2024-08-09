package de.MCmoderSD.main;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.utilities.json.JsonUtility;
import de.MCmoderSD.utilities.other.Reader;

import java.util.ArrayList;

public class Credentials {

    // Bot Credentials
    private JsonNode botConfig;
    private ArrayList<String> channelList;
    private JsonNode mySQLConfig;

    // API Cials
    private JsonNode openAIConfig;
    private JsonNode openWeatherMapConfig;
    private JsonNode giphyConfig;

    // Constructor
    public Credentials(Main main, String botConfig, String channelList, String mySQL, String openAI, String weather, String giphy) {

        // Get Utilities
        JsonUtility jsonUtility = main.getJsonUtility();
        Reader reader = main.getReader();

        // Load Bot Config
        try {
            this.botConfig = jsonUtility.load(botConfig, main.hasArg("botconfig"));
        } catch (Exception e) {
            System.err.println("Error loading Bot Config: " + e.getMessage());
            System.exit(1);
        }

        // Load Channel List
        try {
            this.channelList = reader.lineRead(channelList, main.hasArg("channellist"));
        } catch (Exception e) {
            System.err.println("Error loading Channel List: " + e.getMessage());
        }

        // Load MySQL Config
        try {
            this.mySQLConfig = jsonUtility.load(mySQL, main.hasArg("mysqlconfig"));
        } catch (Exception e) {
            System.err.println("Error loading MySQL Config: " + e.getMessage());
            System.exit(1);
        }

        // Load OpenAI Config
        try {
            this.openAIConfig = jsonUtility.load(openAI, main.hasArg("openaiconfig"));
        } catch (Exception e) {
            System.err.println("Error loading OpenAI Config: " + e.getMessage());
        }

        // Load OpenWeatherMap Config
        try {
            this.openWeatherMapConfig = jsonUtility.load(weather, main.hasArg("openweathermapconfig"));
        } catch (Exception e) {
            System.err.println("Error loading OpenWeatherMap Config: " + e.getMessage());
        }

        // Load Giphy Config
        try {
            this.giphyConfig = jsonUtility.load(giphy, main.hasArg("giphyconfig"));
        } catch (Exception e) {
            System.err.println("Error loading Giphy Config: " + e.getMessage());
        }

    }

    // Getters
    public JsonNode getBotConfig() {
        return botConfig;
    }

    public ArrayList<String> getChannelList() {
        return channelList;
    }

    public JsonNode getMySQLConfig() {
        return mySQLConfig;
    }

    public JsonNode getOpenAIConfig() {
        return openAIConfig;
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

    public boolean validateOpenAIConfig() {
        return openAIConfig != null;
    }

    public boolean validateWeatherConfig() {
        return openWeatherMapConfig != null;
    }

    public boolean validateGiphyConfig() {
        return giphyConfig != null;
    }
}