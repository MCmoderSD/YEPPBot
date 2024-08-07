package de.MCmoderSD.main;

import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.utilities.json.JsonUtility;
import de.MCmoderSD.utilities.other.Reader;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class Credentials {

    // Bot Credentials
    private JsonNode BotConfig;
    private ArrayList<String> ChannelList;
    private JsonNode MySQLConfig;

    // API Credentials
    private JsonNode OpenAIConfig;
    private JsonNode WeatherConfig;
    private JsonNode GiphyConfig;

    // Dev Credentials
    private JsonNode DevConfig;
    private ArrayList<String> DevList;

    // Constructor
    public Credentials(Main main, String botConfig, String channelList, String mySQL, String openAI, String weather, String giphy, String devConfig, String devList) {

        // Get Utilities
        JsonUtility jsonUtility = main.getJsonUtility();
        Reader reader = main.getReader();

        // Load Credentials
        try {
            BotConfig = jsonUtility.load(botConfig);
            ChannelList = reader.lineRead(channelList);
            MySQLConfig = jsonUtility.load(mySQL);

            OpenAIConfig = jsonUtility.load(openAI);
            WeatherConfig = jsonUtility.load(weather);
            GiphyConfig = jsonUtility.load(giphy);

            DevConfig = jsonUtility.load(devConfig);
            DevList = reader.lineRead(devList);
        } catch (Exception e) {
            System.err.println("Error loading credentials: " + e.getMessage());
        }
    }

    // Getters
    public JsonNode getBotConfig() {
        return BotConfig;
    }

    public ArrayList<String> getChannelList() {
        return ChannelList;
    }

    public JsonNode getMySQLConfig() {
        return MySQLConfig;
    }

    public JsonNode getOpenAIConfig() {
        return OpenAIConfig;
    }

    public JsonNode getWeatherConfig() {
        return WeatherConfig;
    }

    public JsonNode getGiphyConfig() {
        return GiphyConfig;
    }

    public JsonNode getDevConfig() {
        return DevConfig;
    }

    public ArrayList<String> getDevList() {
        return DevList;
    }

    // validateations
    public boolean validateBotConfig() {
        return BotConfig != null;
    }

    public boolean validateChannelList() {
        return ChannelList != null && !ChannelList.isEmpty();
    }

    public boolean validateMySQLConfig() {
        return MySQLConfig != null;
    }

    public boolean validateOpenAIConfig() {
        return OpenAIConfig != null;
    }

    public boolean validateWeatherConfig() {
        return WeatherConfig != null;
    }

    public boolean validateGiphyConfig() {
        return GiphyConfig != null;
    }

    public boolean validateDevConfig() {
        return DevConfig != null;
    }

    public boolean validateDevList() {
        return DevList != null && !DevList.isEmpty();
        }
}
