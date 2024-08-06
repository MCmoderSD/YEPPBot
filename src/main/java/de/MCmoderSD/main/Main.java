package de.MCmoderSD.main;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.core.BotClient;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonUtility;
import de.MCmoderSD.utilities.other.OpenAI;
import de.MCmoderSD.utilities.other.Reader;

import java.awt.HeadlessException;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Main {

    // Constants
    public static final String VERSION = "1.21.3";
    public static final String BOT_CONFIG = "/config/BotConfig.json";
    public static final String CHANNEL_LIST = "/config/Channel.list";
    public static final String MYSQL_CONFIG = "/database/mySQL.json";
    public static final String OPENAI_CONFIG = "/api/ChatGPT.json";
    public static final String WEATHER_CONFIG = "/api/OpenWeatherMap.json";
    public static final String GIPHY_CONFIG = "/api/Giphy.json";
    public static final String HELIX_CONFIG = "/api/HelixAPI.json";
    public static final String DEV_CONFIG = "/config/BotConfig.json.dev";
    public static final String DEV_LIST = "/config/Channel.list.dev";

    // Associations
    private final Frame frame;
    private final BotClient botClient;

    // Constructor
    public Main(ArrayList<String> args) {

        // Utilities
        JsonUtility jsonUtility = new JsonUtility();
        Reader reader = new Reader();


        // Check Args
        HashMap<String, Boolean> argMap = checkArgs(args);

        // Help
        if (argMap.get("help")) help();
        if (argMap.get("version")) System.out.println("Version: " + VERSION);


        // Config Paths
        String botConfigPath;
        String channelListPath;
        String mysqlConfigPath;

        // API Paths
        String openAIConfigPath;
        String weatherConfigPath;
        String giphyConfigPath;
        String helixConfigPath;

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

        // Helix Config
        try {
            if (argMap.get("helixconfig")) {
                helixConfigPath = args.get(args.indexOf("helixconfig") + 1);
                jsonUtility.load(helixConfigPath);
            } else helixConfigPath = HELIX_CONFIG;
        } catch (RuntimeException e) {
            System.err.println(BOLD + "Helix Config missing: " + UNBOLD + e.getMessage());
            System.exit(0);
        }

        // Dev Mode
        if (argMap.get("dev")) {
            botConfigPath = DEV_CONFIG;
            channelListPath = DEV_LIST;
        } else {
            botConfigPath = BOT_CONFIG;
            channelListPath = CHANNEL_LIST;
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
        } else frame = null;

        // No Log Mode
        MySQL mySQL = new MySQL(mysqlConfigPath, frame, !argMap.get("log"));

        // Load Bot Config
        JsonNode botConfig = jsonUtility.load(botConfigPath);
        String botName = botConfig.get("botName").asText().toLowerCase();     // Get Bot Name
        String botToken = botConfig.get("botToken").asText();   // Get Bot Token
        String prefix = botConfig.get("prefix").asText();       // Get Prefix
        String[] admins = botConfig.get("admins").asText().toLowerCase().split("; ");

        // Load Channel List
        Reader reader = new Reader();
        ArrayList<String> channels = new ArrayList<>();
        for (String channel : reader.lineRead(channelListPath)) if (channel.length() > 3) channels.add(channel.replace("\n", "").replace(" ", ""));
        if (!args.contains("-dev")) {
            ArrayList<String> temp = mySQL.getActiveChannels();
            for (String channel : temp) if (!channels.contains(channel)) channels.add(channel);
        }

        // Init Bot
        botClient = new BotClient(botName, botToken, prefix, admins, channels, mySQL, openAI);
    }

    private HashMap<String, Boolean> checkArgs(ArrayList<String> args) {

        // Variables
        HashMap<String, Boolean> result = new HashMap<>();

        // Format args
        args.stream().filter(arg -> !(arg.startsWith("-") || arg.startsWith("/"))).toList().forEach(args::remove);
        for (String arg : args) {
            args.remove(arg);
            args.add(arg.replace("/", "-").replace("-", "").toLowerCase());
        }

        // Dev & CLI & Log
        result.put("dev", args.contains("dev") || args.contains("development") || args.contains("debug") || args.contains("test"));
        result.put("cli", args.contains("cli") || args.contains("nogui") || args.contains("console") || args.contains("terminal"));
        result.put("log", args.contains("nolog") || args.contains("no-log") || args.contains("disable-log") || args.contains("disablelog"));

        // Info
        result.put("help", args.contains("help") || args.contains("?"));
        result.put("version", args.contains("version") || args.contains("ver") || args.contains("v"));

        // Bot Config
        result.put("botconfig", args.contains("botconfig") || args.contains("bot-config"));
        result.put("channellist", args.contains("channellist") || args.contains("channel-list"));
        result.put("mysqlconfig", args.contains("mysqlconfig") || args.contains("mysql-config"));

        // API Config
        result.put("openaiconfig", args.contains("openaiconfig") || args.contains("openai-config") || args.contains("chatgptconfig") || args.contains("chatgpt-config"));
        result.put("weatherconfig", args.contains("weatherconfig") || args.contains("weather-config") || args.contains("weatherapi") || args.contains("weather-api"));
        result.put("giphyconfig", args.contains("giphyconfig") || args.contains("giphy-config") || args.contains("gifconfig") || args.contains("gif-config"));
        result.put("helixconfig", args.contains("helixconfig") || args.contains("helix-config") || args.contains("twitchconfig") || args.contains("twitch-config"));

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
        System.out.println("  -helixconfig: Path to Helix Config");
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
}