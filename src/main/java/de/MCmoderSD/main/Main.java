package de.MCmoderSD.main;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonUtility;
import de.MCmoderSD.utilities.other.OpenAI;
import de.MCmoderSD.utilities.other.Reader;

import java.awt.HeadlessException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Main {

    // Constants
    public static final String VERSION = "1.21.1";

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

        // Format Args
        args.replaceAll(String::toLowerCase);
        args.removeAll(Collections.singleton(""));
        args.removeAll(Collections.singleton(" "));
        args.removeAll(Collections.singleton(null));
        for (String arg : args) if (arg.startsWith("-") || arg.startsWith("/")) args.set(args.indexOf(arg), arg.replaceAll("/", "-").replaceAll("--", "-"));

        // Check Args
        argMap = checkArgs(args);

        // Help
        if (argMap.get("help")) help();
        if (argMap.get("version")) System.out.println("Version: " + VERSION);

        // Generate Config Files
        if (argMap.get("generate")) generateConfigFiles();

        // Config Paths
        String botConfigPath = null;
        String channelListPath = null;
        String mysqlConfigPath = null;

        // API Paths
        String openAIConfigPath;
        String weatherConfigPath;
        String giphyConfigPath;

        // Bot Config
        if (argMap.get("botconfig")) botConfigPath = args.get(args.indexOf("-botconfig") + 1);

        // Channel List
        if (argMap.get("channellist")) channelListPath = args.get(args.indexOf("-channellist") + 1);

        // MySQL Config
        if (argMap.get("mysqlconfig")) mysqlConfigPath = args.get(args.indexOf("-mysqlconfig") + 1);

        // OpenAI Config
        if (argMap.get("openaiconfig")) openAIConfigPath = args.get(args.indexOf("-openaiconfig") + 1);
        else openAIConfigPath = OPENAI_CONFIG;

        // Weather Config
        if (argMap.get("openweathermapconfig")) weatherConfigPath = args.get(args.indexOf("-openweathermapconfig") + 1);
        else weatherConfigPath = WEATHER_CONFIG;

        // Giphy Config
        if (argMap.get("giphyconfig")) giphyConfigPath = args.get(args.indexOf("-giphyconfig") + 1);
        else giphyConfigPath = GIPHY_CONFIG;

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
            if (botConfigPath == null) botConfigPath = DEV_CONFIG;
            if (channelListPath == null) channelListPath = DEV_LIST;
            if (mysqlConfigPath == null) mysqlConfigPath = DEV_MYSQL;
        } else {
            if (botConfigPath == null) botConfigPath = BOT_CONFIG;
            if (channelListPath == null) channelListPath = CHANNEL_LIST;
            if (mysqlConfigPath == null) mysqlConfigPath = MYSQL_CONFIG;
        }

        // Initialize Credentials
        credentials = new Credentials(this, botConfigPath, channelListPath, mysqlConfigPath, openAIConfigPath, weatherConfigPath, giphyConfigPath);

        // Initialize OpenAI
        if (credentials.validateOpenAIConfig()) openAI = new OpenAI(credentials.getOpenAIConfig());
        else System.err.println(BOLD + "OpenAI Config missing: " + UNBOLD + "OpenAI will not be available");

        // Initialize MySQL
        if (credentials.validateMySQLConfig()) mySQL = new MySQL(this);
        else {
            System.err.println(BOLD + "MySQL Config missing: Stopping Bot" + UNBOLD);
            System.exit(1);
        }

        // Initialize Bot Client
        if (credentials.validateBotConfig()) botClient = new BotClient(this);
        else {
            System.err.println(BOLD + "Bot Config missing: Stopping Bot" + UNBOLD);
            System.exit(1);
        }
    }

    // Generate Config Files
    private void generateConfigFiles() {

        // Files
        String[] fileNames = {"BotConfig.json", "Channel.list", "mySQL.json", "ChatGPT.json", "OpenWeatherMap.json", "Giphy.json"};

        for (String fileName : fileNames) {

            // Input Stream
            InputStream inputStream = getClass().getResourceAsStream("/examples/" + fileName);

            assert inputStream != null; // Check

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            BufferedWriter bufferedWriter;

            try {

                // Create
                bufferedWriter = new BufferedWriter(new FileWriter(fileName));

                // Write
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }

                // Close
                bufferedReader.close();
                bufferedWriter.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Exit
        System.out.println("Config Files generated");
        System.exit(0);
    }

    // Check Args
    private HashMap<String, Boolean> checkArgs(ArrayList<String> args) {

        // Variables
        ArrayList<String> arguments = new ArrayList<>();
        HashMap<String, Boolean> result = new HashMap<>();

        // Format args
        for (String arg : args) if (arg.startsWith("-") || arg.startsWith("/")) arguments.add(arg.replaceAll("/", "-").replaceAll("-", ""));

        // Dev & CLI & Log
        result.put("dev", listContainsEither(arguments, "dev", "development", "debug", "test"));
        result.put("cli", listContainsEither(arguments, "nogui", "no-gui", "console", "terminal", "cli"));
        result.put("log", !listContainsEither(arguments, "nolog", "no-log", "disable-log", "disablelog"));

        // Info
        result.put("help", listContainsEither(arguments, "help", "?"));
        result.put("version", listContainsEither(arguments, "version", "ver", "v"));
        result.put("generate", listContainsEither(arguments, "generate", "gen"));

        // Bot Config
        result.put("botconfig", listContainsEither(arguments, "botconfig"));
        result.put("channellist", listContainsEither(arguments, "channellist"));
        result.put("mysqlconfig", listContainsEither(arguments, "mysqlconfig"));

        // API Config
        result.put("openaiconfig", listContainsEither(arguments, "openaiconfig"));
        result.put("openweathermapconfig", listContainsEither(arguments, "openweathermapconfig"));
        result.put("giphyconfig", listContainsEither(arguments, "giphyconfig"));

        // Return
        return result;
    }

    // Help
    private void help() {
        // Info
        System.out.println(
        """
        \n
        Info:
            -help: Show Help
            -version: Show Version
        """);

        // Modes
        System.out.println(
        """ 
        Modes:
            -dev: Development Mode
            -cli: CLI Mode (No GUI)
            -nolog: Disable Logging
        """);

        // Generate Config Files
        System.out.println(
        """ 
        Generate:
            -generate: Generate Config Files
            -gen: Generate Config Files
        """);

        // Bot Config
        System.out.println(
        """
        Bot Config:
            -botconfig: Path to Bot Config
            -channellist: Path to Channel List
            -mysqlconfig: Path to MySQL Config
        """);

        // API Config
        System.out.println(
        """
        API Config:
            -openaiconfig: Path to OpenAI Config
            -openweathermap: Path to Weather Config
            -giphyconfig: Path to Giphy Config
        """);

        // Exit
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

    public boolean hasArg(String arg) {
        return argMap.get(arg);
    }
}