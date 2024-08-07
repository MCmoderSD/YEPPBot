package de.MCmoderSD.main;


import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.core.BotClient;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonUtility;
import de.MCmoderSD.utilities.other.OpenAI;
import de.MCmoderSD.utilities.other.Reader;

import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Main {

    // Constants
    public static final String VERSION = "1.21.3";

    // Bot Config
    public static final String BOT_CONFIG = "/config/BotConfig.json";
    public static final String CHANNEL_LIST = "/config/Channel.list";
    public static final String MYSQL_CONFIG = "/database/mySQL.json";

    // API Credentials
    public static final String OPENAI_CONFIG = "/api/ChatGPT.json";
    public static final String WEATHER_CONFIG = "/api/OpenWeatherMap.json";
    public static final String GIPHY_CONFIG = "/api/Giphy.json";

    // Dev Credentials
    public static final String DEV_CONFIG = "/config/BotConfig.json.dev";
    public static final String DEV_LIST = "/config/Channel.list.dev";
    public static final String DEV_MYSQL = "/database/dev.json";

    // Utilities
    private final JsonUtility jsonUtility;
    private final Reader reader;

    // Associations
    private final Credentials credentials;

    private BotClient botClient;
    private Frame frame;
    private MySQL mySQL;
    private OpenAI openAI;

    // Variables
    private final HashMap<String, Boolean> argMap;

    // Constructor
    public Main(ArrayList<String> args) {

        // Utilities
        jsonUtility = new JsonUtility();
        reader = new Reader();

        // Check Args
        argMap = checkArgs(args);

        // Help
        if (argMap.get("help")) help();
        if (argMap.get("version")) System.out.println("Version: " + VERSION);

        // Config Paths
        String botConfigPath = null;
        String channelListPath = null;
        String mysqlConfigPath = null;

        // API Paths
        String openAIConfigPath = null;
        String weatherConfigPath = null;
        String giphyConfigPath = null;

        // Bot Config
        try {
            if (argMap.get("botconfig")) {
                botConfigPath = args.get(args.indexOf("botconfig") + 1);
                jsonUtility.load(botConfigPath);
            } else botConfigPath = BOT_CONFIG;
        } catch (RuntimeException e) {
            System.err.println(BOLD + "Bot Config missing: " + UNBOLD + e.getMessage());
            System.exit(0);
        }

        // Channel List
        try {
            if (argMap.get("channellist")) {
                channelListPath = args.get(args.indexOf("channellist") + 1);
                reader.lineRead(channelListPath);
            } else channelListPath = CHANNEL_LIST;
        } catch (RuntimeException e) {
            System.err.println(BOLD + "Channel List missing: " + UNBOLD + e.getMessage());
            System.exit(0);
        }

        // MySQL Config
        try {
            if (argMap.get("mysqlconfig")) {
                mysqlConfigPath = args.get(args.indexOf("mysqlconfig") + 1);
                jsonUtility.load(mysqlConfigPath);
            } else mysqlConfigPath = MYSQL_CONFIG;
        } catch (RuntimeException e) {
            System.err.println(BOLD + "MySQL Config missing: " + UNBOLD + e.getMessage());
            System.exit(0);
        }

        // OpenAI Config
        try {
            if (argMap.get("openaiconfig")) {
                openAIConfigPath = args.get(args.indexOf("openaiconfig") + 1);
                jsonUtility.load(openAIConfigPath);
            } else openAIConfigPath = OPENAI_CONFIG;
        } catch (RuntimeException e) {
            System.err.println(BOLD + "OpenAI Config missing: " + UNBOLD + e.getMessage());
            System.exit(0);
        }

        // Weather Config
        try {
            if (argMap.get("weatherconfig")) {
                weatherConfigPath = args.get(args.indexOf("weatherconfig") + 1);
                jsonUtility.load(weatherConfigPath);
            } else weatherConfigPath = WEATHER_CONFIG;
        } catch (RuntimeException e) {
            System.err.println(BOLD + "Weather Config missing: " + UNBOLD + e.getMessage());
            System.exit(0);
        }

        // Giphy Config
        try {
            if (argMap.get("giphyconfig")) {
                giphyConfigPath = args.get(args.indexOf("giphyconfig") + 1);
                jsonUtility.load(giphyConfigPath);
            } else giphyConfigPath = GIPHY_CONFIG;
        } catch (RuntimeException e) {
            System.err.println(BOLD + "Giphy Config missing: " + UNBOLD + e.getMessage());
            System.exit(0);
        }

        // CLI Mode
        if (!argMap.get("cli")) {
            Frame tempFrame = null;
            try {
                tempFrame = new Frame(this);
            } catch (HeadlessException e) {
                System.err.println(BOLD + "No display found: " + UNBOLD + e.getMessage());
            }
            frame = tempFrame;
        }

        // Dev Mode
        if (argMap.get("dev")) {
            botConfigPath = DEV_CONFIG;
            channelListPath = DEV_LIST;
            mysqlConfigPath = DEV_MYSQL;
        } else {
            if (botConfigPath == null) botConfigPath = BOT_CONFIG;
            if (channelListPath == null) channelListPath = CHANNEL_LIST;
            if (mysqlConfigPath == null) mysqlConfigPath = MYSQL_CONFIG;
        }

        // Initialize Credentials
        credentials = new Credentials(this, botConfigPath, channelListPath, mysqlConfigPath, openAIConfigPath, weatherConfigPath, giphyConfigPath, DEV_CONFIG, DEV_LIST);

        // Initialize OpenAI
        if (credentials.validateOpenAIConfig()) openAI = new OpenAI(credentials.getOpenAIConfig());
        else System.err.println(BOLD + "OpenAI Config missing: " + UNBOLD + "OpenAI will not be available");

        // Initialize MySQL
        if (credentials.validateMySQLConfig()) mySQL = new MySQL(this);
        else {
            System.err.println(BOLD + "MySQL Config missing: Stopping Bot" + UNBOLD);
            System.exit(0);
        }

        // Initialize Bot Client
        if (credentials.validateBotConfig()) botClient = new BotClient(this);
        else {
            System.err.println(BOLD + "Bot Config missing: Stopping Bot" + UNBOLD);
            System.exit(0);
        }
    }

    private HashMap<String, Boolean> checkArgs(ArrayList<String> args) {

        // Variables
        ArrayList<String> arguments = new ArrayList<>();
        HashMap<String, Boolean> result = new HashMap<>();

        // Format args
        for (String arg : args) if (arg.startsWith("-") || arg.startsWith("/")) arguments.add(arg.replace("/", "-").replace("-", "").toLowerCase());

        // Dev & CLI & Log
        result.put("dev", arguments.contains("dev") || arguments.contains("development") || arguments.contains("debug") || arguments.contains("test"));
        result.put("cli", arguments.contains("nogui") || arguments.contains("no-gui") || arguments.contains("console") || arguments.contains("terminal"));
        result.put("log", !(arguments.contains("nolog") || arguments.contains("no-log") || arguments.contains("disable-log") || arguments.contains("disablelog")));

        // Info
        result.put("help", arguments.contains("help") || arguments.contains("?"));
        result.put("version", arguments.contains("version") || arguments.contains("ver") || arguments.contains("v"));

        // Bot Config
        result.put("botconfig", arguments.contains("botconfig") || arguments.contains("bot-config"));
        result.put("channellist", arguments.contains("channellist") || arguments.contains("channel-list"));
        result.put("mysqlconfig", arguments.contains("mysqlconfig") || arguments.contains("mysql-config"));

        // API Config
        result.put("openaiconfig", arguments.contains("openaiconfig") || arguments.contains("openai-config") || arguments.contains("chatgptconfig") || arguments.contains("chatgpt-config"));
        result.put("weatherconfig", arguments.contains("weatherconfig") || arguments.contains("weather-config") || arguments.contains("weatherapi") || arguments.contains("weather-api"));
        result.put("giphyconfig", arguments.contains("giphyconfig") || arguments.contains("giphy-config") || arguments.contains("gifconfig") || arguments.contains("gif-config"));

        // Return
        return result;
    }

    private void help() {
        // Info
        System.out.println("\n Info:");
        System.out.println("  -help: Show Help");
        System.out.println("  -version: Show Version");

        // Modes
        System.out.println("\n Modes:");
        System.out.println("  -dev: Development Mode");
        System.out.println("  -cli: CLI Mode (No GUI)");
        System.out.println("  -nolog: Disable Logging");

        // Bot Config
        System.out.println("\n Bot Config:");
        System.out.println("  -botconfig: Path to Bot Config");
        System.out.println("  -channellist: Path to Channel List");
        System.out.println("  -mysqlconfig: Path to MySQL Config");

        // API Config
        System.out.println("\n API Config:");
        System.out.println("  -openaiconfig: Path to OpenAI Config");
        System.out.println("  -weatherconfig: Path to Weather Config");
        System.out.println("  -giphyconfig: Path to Giphy Config");
        System.exit(0);
    }

    // PSVM
    public static void main(String[] args) {
        new Main(new ArrayList<>(Arrays.asList(args)));
    }

    // Getter
    public BotClient getBotClient() {
        return botClient;
    }

    public Frame getFrame() {
        return frame;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public OpenAI getOpenAI() {
        return openAI;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public JsonUtility getJsonUtility() {
        return jsonUtility;
    }

    public Reader getReader() {
        return reader;
    }

    public boolean getArg(String arg) {
        return argMap.get(arg);
    }
}