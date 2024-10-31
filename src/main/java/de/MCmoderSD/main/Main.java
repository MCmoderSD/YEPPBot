package de.MCmoderSD.main;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.utilities.database.MySQL;

import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.OpenAI.OpenAI;

import java.awt.HeadlessException;

import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Format.*;

public class Main {

    // Constants
    public static final String VERSION = "1.22.0";

    // Bot Config
    public static final String BOT_CONFIG = "/config/BotConfig.json";
    public static final String CHANNEL_LIST = "/config/Channel.list";
    public static final String MYSQL_CONFIG = "/database/mySQL.json";
    public static final String HTTPS_SERVER = "/config/httpsServer.json";

    // API Credentials
    public static final String API_CONFIG = "/api/apiKeys.json";
    public static final String OPENAI_CONFIG = "/api/ChatGPT.json";

    // Dev Credentials
    public static final String DEV_CONFIG = "/config/DevConfig.json";
    public static final String DEV_LIST = "/config/Dev.list";
    public static final String DEV_MYSQL = "/database/dev.json";
    public static final String DEV_HTTPS_SERVER = "/config/DevHttpsServer.json";

    // Instances
    public static Terminal terminal;
    public static BotClient botClient;
    public static JsonUtility jsonUtility;

    // Main
    public static void main(String[] args) {

        // Initialize static instances
        terminal = new Terminal(args);
        jsonUtility = new JsonUtility();

        // Initialize Config Paths
        String botConfigPath = null;
        String channelListPath = null;
        String mysqlConfigPath = null;
        String httpsServerPath = null;

        // API Paths
        String apiKeysPath;
        String openAIConfigPath;

        // Convert Args to List
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));

        // Bot Config
        if (terminal.hasArg(Terminal.Argument.BOT_CONFIG)) botConfigPath = arguments.get(arguments.indexOf("-botconfig") + 1);

        // Channel List
        if (terminal.hasArg(Terminal.Argument.CHANNEL_LIST)) channelListPath = arguments.get(arguments.indexOf("-channellist") + 1);

        // MySQL Config
        if (terminal.hasArg(Terminal.Argument.MYSQL_CONFIG)) mysqlConfigPath = arguments.get(arguments.indexOf("-mysqlconfig") + 1);

        // Https Server
        if (terminal.hasArg(Terminal.Argument.HTTPS_SERVER)) httpsServerPath = arguments.get(arguments.indexOf("-httpsserver") + 1);

        // API Config
        if (terminal.hasArg(Terminal.Argument.API_CONFIG)) apiKeysPath = arguments.get(arguments.indexOf("-apiconfig") + 1);
        else apiKeysPath = API_CONFIG;

        // OpenAI Config
        if (terminal.hasArg(Terminal.Argument.OPENAI_CONFIG)) openAIConfigPath = arguments.get(arguments.indexOf("-openaiconfig") + 1);
        else openAIConfigPath = OPENAI_CONFIG;

        // Instances
        MySQL mySQL;
        Frame frame = null;
        OpenAI openAI = null;

        // CLI Mode
        if (!terminal.hasArg(Terminal.Argument.CLI)) {

            // Try to create Frame
            try {
                frame = new Frame();
            } catch (HeadlessException e) {
                System.err.println(BOLD + "No display found: " + UNBOLD + e.getMessage());
            }
        }

        // Dev Mode
        if (terminal.hasArg(Terminal.Argument.DEV)) {
            if (botConfigPath == null) botConfigPath = DEV_CONFIG;
            if (channelListPath == null) channelListPath = DEV_LIST;
            if (mysqlConfigPath == null) mysqlConfigPath = DEV_MYSQL;
            if (httpsServerPath == null) httpsServerPath = DEV_HTTPS_SERVER;
        } else {
            if (botConfigPath == null) botConfigPath = BOT_CONFIG;
            if (channelListPath == null) channelListPath = CHANNEL_LIST;
            if (mysqlConfigPath == null) mysqlConfigPath = MYSQL_CONFIG;
            if (httpsServerPath == null) httpsServerPath = HTTPS_SERVER;
        }

        // Initialize Credentials
        Credentials credentials = new Credentials(botConfigPath, channelListPath, mysqlConfigPath, httpsServerPath, apiKeysPath, openAIConfigPath);

        // Initialize OpenAI
        if (credentials.validateOpenAIConfig()) openAI = new OpenAI(credentials.getOpenAIConfig());
        else System.err.println(BOLD + "OpenAI Config missing: " + UNBOLD + "OpenAI will not be available");

        // Initialize MySQL
        if (credentials.validateMySQLConfig()) mySQL = new MySQL(credentials.getMySQLConfig());
        else throw new RuntimeException(BOLD + "MySQL Config missing: Stopping Bot" + UNBOLD);

        // Initialize Bot Client
        if (credentials.validateBotConfig()) botClient = new BotClient(credentials, mySQL, frame, openAI);
        else throw new RuntimeException(BOLD + "Bot Config missing: Stopping Bot" + UNBOLD);
    }
}