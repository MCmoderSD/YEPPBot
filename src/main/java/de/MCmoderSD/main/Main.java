package de.MCmoderSD.main;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.utilities.OpenAI.OpenAI;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonUtility;
import de.MCmoderSD.utilities.other.Reader;

import java.awt.HeadlessException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Main {

    // Constants
    public static final String VERSION = "1.21.5";

    // Bot Config
    public static final String BOT_CONFIG = "/config/BotConfig.json";
    public static final String CHANNEL_LIST = "/config/Channel.list";
    public static final String MYSQL_CONFIG = "/database/mySQL.json";
    public static final String HTTP_SERVER = "/config/HttpServer.json";

    // API Credentials
    public static final String OPENAI_CONFIG = "/api/ChatGPT.json";
    public static final String WEATHER_CONFIG = "/api/OpenWeatherMap.json";
    public static final String GIPHY_CONFIG = "/api/Giphy.json";

    // Dev Credentials
    public static final String DEV_CONFIG = "/config/BotConfig.json.dev";
    public static final String DEV_LIST = "/config/Channel.list.dev";
    public static final String DEV_MYSQL = "/database/dev.json";
    public static final String DEV_HTTP_SERVER = "/examples/HttpServer.json";

    // Utilities
    private final JsonUtility jsonUtility;
    private final Reader reader;

    // Associations
    private final Credentials credentials;

    // Arguments
    public static String[] arguments;
    private final HashSet<Argument> args;

    // Variables
    private BotClient botClient;
    private Frame frame;
    private MySQL mySQL;
    private OpenAI openAI;

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
        for (String arg : args)
            if (arg.startsWith("-") || arg.startsWith("/"))
                args.set(args.indexOf(arg), arg.replaceAll("/", "-").replaceAll("--", "-"));

        // Check Args
        this.args = checkArgs(args);

        // Help
        if (hasArg(Argument.HELP)) help();
        if (hasArg(Argument.VERSION)) System.out.println("Version: " + VERSION);

        // Generate Config Files
        if (hasArg(Argument.GENERATE)) generateConfigFiles();

        // Config Paths
        String botConfigPath = null;
        String channelListPath = null;
        String mysqlConfigPath = null;
        String httpServerPath = null;

        // API Paths
        String openAIConfigPath;
        String weatherConfigPath;
        String giphyConfigPath;

        // Bot Config
        if (hasArg(Argument.BOT_CONFIG)) botConfigPath = args.get(args.indexOf("-botconfig") + 1);

        // Channel List
        if (hasArg(Argument.CHANNEL_LIST)) channelListPath = args.get(args.indexOf("-channellist") + 1);

        // MySQL Config
        if (hasArg(Argument.MYSQL_CONFIG)) mysqlConfigPath = args.get(args.indexOf("-mysqlconfig") + 1);

        // Http Server
        if (hasArg(Argument.HTTP_SERVER)) httpServerPath = args.get(args.indexOf("-httpserver") + 1);

        // OpenAI Config
        if (hasArg(Argument.OPENAI_CONFIG)) openAIConfigPath = args.get(args.indexOf("-openaiconfig") + 1);
        else openAIConfigPath = OPENAI_CONFIG;

        // Weather Config
        if (hasArg(Argument.OPENWEATHERMAP_CONFIG))
            weatherConfigPath = args.get(args.indexOf("-openweathermapconfig") + 1);
        else weatherConfigPath = WEATHER_CONFIG;

        // Giphy Config
        if (hasArg(Argument.GIPHY_CONFIG)) giphyConfigPath = args.get(args.indexOf("-giphyconfig") + 1);
        else giphyConfigPath = GIPHY_CONFIG;

        // CLI Mode
        if (!hasArg(Argument.CLI)) {
            Frame tempFrame = null;
            try {
                tempFrame = new Frame(this);
            } catch (HeadlessException e) {
                System.err.println(BOLD + "No display found: " + UNBOLD + e.getMessage());
            }
            frame = tempFrame;
        }

        // Dev Mode
        if (hasArg(Argument.DEV)) {
            if (botConfigPath == null) botConfigPath = DEV_CONFIG;
            if (channelListPath == null) channelListPath = DEV_LIST;
            if (mysqlConfigPath == null) mysqlConfigPath = DEV_MYSQL;
            if (httpServerPath == null) httpServerPath = DEV_HTTP_SERVER;
        } else {
            if (botConfigPath == null) botConfigPath = BOT_CONFIG;
            if (channelListPath == null) channelListPath = CHANNEL_LIST;
            if (mysqlConfigPath == null) mysqlConfigPath = MYSQL_CONFIG;
            if (httpServerPath == null) httpServerPath = HTTP_SERVER;
        }

        // Custom Host and Port
        if (hasArg(Argument.HOST) && hasArg(Argument.PORT)) arguments = new String[]{args.get(args.indexOf("-host") + 1), args.get(args.indexOf("-port") + 1)};

        // Initialize Credentials
        credentials = new Credentials(this, botConfigPath, channelListPath, mysqlConfigPath, httpServerPath, openAIConfigPath, weatherConfigPath, giphyConfigPath);

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

    // PSVM
    public static void main(String[] args) {
        arguments = args;
        new Main(new ArrayList<>(Arrays.asList(args)));
    }

    // Generate Config Files
    private void generateConfigFiles() {

        // Files
        String[] fileNames = {"BotConfig.json", "Channel.list", "mySQL.json", "HttpServer.json", "ChatGPT.json", "OpenWeatherMap.json", "Giphy.json"};

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
    private HashSet<Argument> checkArgs(ArrayList<String> args) {

        // Variables
        HashSet<String> arguments = new HashSet<>();
        HashSet<Argument> result = new HashSet<>();

        // Format args
        for (String arg : args)
            if (arg.startsWith("-") || arg.startsWith("/")) arguments.add(arg.replaceAll("/", "-").replaceAll("-", ""));

        // Check
        Arrays.stream(Argument.values()).forEach(argument -> {
            if (argument.setHasNameOrAlias(arguments)) result.add(argument);
        });

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
                            -httpserver: Path to Http Server Config
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

    public boolean hasArg(Argument arg) {
        return args.contains(arg);
    }

    // Enum
    public enum Argument {

        // Arguments
        HELP("help", "?", "h"),
        VERSION("version", "ver", "v"),
        GENERATE("generate", "gen"),
        DEV("dev", "development", "debug", "test"),
        CLI("cli", "nogui", "console", "terminal"),
        LOG("nolog", "disablelog"),
        HOST("host"),
        PORT("port"),
        BOT_CONFIG("botconfig"),
        CHANNEL_LIST("channellist"),
        MYSQL_CONFIG("mysqlconfig"),
        HTTP_SERVER("httpserver"),
        OPENAI_CONFIG("openaiconfig"),
        OPENWEATHERMAP_CONFIG("openweathermapconfig"),
        GIPHY_CONFIG("giphyconfig");

        // Attributes
        private final String[] aliases;
        private final String name;

        // Constructor
        Argument(String name, String... aliases) {
            this.name = name;
            this.aliases = aliases;
        }

        // Check
        private boolean setHasNameOrAlias(Set<String> set) {
            if (set == null) return false;
            if (set.isEmpty()) return false;
            if (set.contains(name)) return true;
            return Arrays.stream(aliases).anyMatch(set::contains);
        }
    }
}