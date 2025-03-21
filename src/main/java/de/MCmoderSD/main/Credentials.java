package de.MCmoderSD.main;

import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.enums.Argument;

import java.util.HashSet;

import static de.MCmoderSD.main.Main.jsonUtility;
import static de.MCmoderSD.main.Main.terminal;
import static de.MCmoderSD.utilities.other.Util.readAllLines;


@SuppressWarnings("unused")
public class Credentials {

    // Bot Credentials
    private JsonNode botConfig;
    private HashSet<String> channelList;
    private JsonNode sqlConfig;
    private JsonNode serverConfig;

    // API Credentials
    private JsonNode apiConfig;
    private JsonNode openAIConfig;

    // APIs Provided
    private boolean astrology;
    private boolean openWeatherMap;
    private boolean giphy;
    private boolean riot;

    // Constructor
    public Credentials(String botConfig, String channelList, String sql, String httpsServer, String apiKeys, String openAI) {

        // Load Bot Config
        try {
            this.botConfig = jsonUtility.load(botConfig, terminal.hasArg(Argument.BOT_CONFIG) || terminal.hasArg(Argument.CONTAINER));
        } catch (Exception e) {
            System.err.println("Error loading Bot Config: " + e.getMessage());
            System.exit(1);
        }

        // Load Channel List
        try {
            this.channelList = new HashSet<>(readAllLines(channelList, terminal.hasArg(Argument.CHANNEL_LIST) || terminal.hasArg(Argument.CONTAINER)));
        } catch (Exception e) {
            System.err.println("Error loading Channel List: " + e.getMessage());
            System.err.println("Creating empty Channel List...");
            this.channelList = new HashSet<>();
        }

        // Load SQL Config
        try {
            this.sqlConfig = jsonUtility.load(sql, terminal.hasArg(Argument.SQL_CONFIG) || terminal.hasArg(Argument.CONTAINER));
        } catch (Exception e) {
            System.err.println("Error loading SQL Config: " + e.getMessage());
        }

        // Load Server Config
        try {
            this.serverConfig = jsonUtility.load(httpsServer, terminal.hasArg(Argument.SERVER_CONFIG) || terminal.hasArg(Argument.CONTAINER));
        } catch (Exception e) {
            System.err.println("Error loading Server Config: " + e.getMessage());
        }

        // Load API Config
        try {

            // Load JSON
            apiConfig = jsonUtility.load(apiKeys, terminal.hasArg(Argument.API_CONFIG) || terminal.hasArg(Argument.CONTAINER));

            // Check for APIs
            astrology = apiConfig.has("astrology");
            openWeatherMap = apiConfig.has("openWeatherMap");
            giphy = apiConfig.has("giphy");
            riot = apiConfig.has("riot");

        } catch (Exception e) {
            System.err.println("Error loading API Config: " + e.getMessage());
        }

        // Load OpenAI Config
        try {

            // Load JSON
            openAIConfig = jsonUtility.load(openAI, terminal.hasArg(Argument.OPENAI_CONFIG) || terminal.hasArg(Argument.CONTAINER));
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

    public JsonNode SQLConfig() {
        return sqlConfig;
    }

    public JsonNode getServerConfig() {
        return serverConfig;
    }

    public JsonNode getAPIConfig() {
        return apiConfig;
    }

    public JsonNode getOpenAIConfig() {
        return openAIConfig;
    }

    // Validations
    public boolean validateBotConfig() {
        return botConfig != null;
    }

    public boolean validateChannelList() {
        return channelList != null && !channelList.isEmpty();
    }

    public boolean validateSQLConfig() {
        return sqlConfig != null;
    }

    public boolean validateServerConfig() {
        return serverConfig != null;
    }

    public boolean validateOpenAIConfig() {
        return openAIConfig != null;
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

    public boolean hasRiot() {
        return riot;
    }

    public boolean hasOpenAI() {
        return openAIConfig != null;
    }
}