package de.MCmoderSD.main;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.enums.Argument;
import de.MCmoderSD.openai.core.OpenAI;
import de.MCmoderSD.utilities.database.SQL;

import de.MCmoderSD.json.JsonUtility;
import java.awt.HeadlessException;

import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Format.*;

public class Main {

    // Constants
    public static final String VERSION = "1.24.2";

    // Bot Config
    public static final String BOT_CONFIG = "/config/bot.json";
    public static final String CHANNEL_LIST = "/config/channels.txt";
    public static final String SQL_CONFIG = "/database/sql.json";
    public static final String SERVER_CONFIG = "/config/server.json";

    // API Credentials
    public static final String API_CONFIG = "/api/api.json";
    public static final String OPENAI_CONFIG = "/api/openai.json";

    // Dev Credentials
    public static final String DEV_CONFIG = "/config/devConfig.json";
    public static final String DEV_LIST = "/config/devList.txt";
    public static final String DEV_SQL = "/database/dev.json";
    public static final String DEV_SERVER = "/config/devServer.json";

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
        String sqlConfigPath = null;
        String serverPath = null;

        // API Paths
        String apiKeysPath;
        String openAIConfigPath;

        // Convert Args to List
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));

        if (!terminal.hasArg(Argument.CONTAINER)) {

            // Bot Config
            if (terminal.hasArg(Argument.BOT_CONFIG)) botConfigPath = Argument.BOT_CONFIG.getConfig(arguments);

            // Channel List
            if (terminal.hasArg(Argument.CHANNEL_LIST)) channelListPath = Argument.CHANNEL_LIST.getConfig(arguments);

            // SQL Config
            if (terminal.hasArg(Argument.SQL_CONFIG)) sqlConfigPath = Argument.SQL_CONFIG.getConfig(arguments);

            // Server Config
            if (terminal.hasArg(Argument.SERVER_CONFIG)) serverPath = Argument.SERVER_CONFIG.getConfig(arguments);

            // API Config
            if (terminal.hasArg(Argument.API_CONFIG)) apiKeysPath = Argument.API_CONFIG.getConfig(arguments);
            else apiKeysPath = API_CONFIG;

            // OpenAI Config
            if (terminal.hasArg(Argument.OPENAI_CONFIG)) openAIConfigPath = Argument.OPENAI_CONFIG.getConfig(arguments);
            else openAIConfigPath = OPENAI_CONFIG;

        } else {
            String workdir = "/app";
            botConfigPath = workdir + "/config/bot.json";
            channelListPath = workdir + "/config/channels.txt";
            sqlConfigPath = workdir + "/config/sql.json";
            serverPath = workdir + "/config/server.json";
            apiKeysPath = workdir + "/config/api.json";
            openAIConfigPath = workdir + "/config/openai.json";
        }

        // Instances
        SQL sql;
        Frame frame = null;
        OpenAI openAI = null;

        // CLI Mode
        if (!terminal.hasArg(Argument.CLI) && !terminal.hasArg(Argument.CONTAINER)) {

            // Try to create Frame
            try {
                frame = new Frame();
            } catch (HeadlessException e) {
                System.err.println(BOLD + "No display found: " + UNBOLD + e.getMessage());
            }
        }

        // Dev Mode
        if (terminal.hasArg(Argument.DEV)) {
            if (botConfigPath == null) botConfigPath = DEV_CONFIG;
            if (channelListPath == null) channelListPath = DEV_LIST;
            if (sqlConfigPath == null) sqlConfigPath = DEV_SQL;
            if (serverPath == null) serverPath = DEV_SERVER;
        } else {
            if (botConfigPath == null) botConfigPath = BOT_CONFIG;
            if (channelListPath == null) channelListPath = CHANNEL_LIST;
            if (sqlConfigPath == null) sqlConfigPath = SQL_CONFIG;
            if (serverPath == null) serverPath = SERVER_CONFIG;
        }

        // Initialize Credentials
        Credentials credentials = new Credentials(botConfigPath, channelListPath, sqlConfigPath, serverPath, apiKeysPath, openAIConfigPath);

        // Initialize OpenAI
        if (credentials.validateOpenAIConfig()) openAI = new OpenAI(credentials.getOpenAIConfig());
        else System.err.println(BOLD + "OpenAI Config missing: " + UNBOLD + "OpenAI will not be available");

        // Initialize SQL
        if (credentials.validateSQLConfig()) sql = new SQL(credentials.SQLConfig());
        else throw new RuntimeException(BOLD + "SQL Config missing: Stopping Bot" + UNBOLD);

        // Initialize Bot Client
        if (credentials.validateBotConfig()) botClient = new BotClient(credentials, sql, frame, openAI);
        else throw new RuntimeException(BOLD + "Bot Config missing: Stopping Bot" + UNBOLD);
    }
}