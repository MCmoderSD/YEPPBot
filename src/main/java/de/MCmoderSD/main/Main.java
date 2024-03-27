package de.MCmoderSD.main;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.core.BotClient;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonNode;
import de.MCmoderSD.utilities.json.JsonUtility;
import de.MCmoderSD.utilities.other.OpenAI;
import de.MCmoderSD.utilities.other.Reader;

import java.util.ArrayList;

public class Main {

    // Constants
    private static final String BOT_CONFIG = "/config/BotConfig.json";
    private static final String CHANNEL_LIST = "/config/Channel.list";
    private static final String MYSQL_CONFIG = "/database/mySQL.json";
    private static final String OPENAI_CONFIG = "/api/ChatGPT.json";
    private static final String DEV_CONFIG = "/config/BotConfig.json.dev";
    private static final String DEV_LIST = "/config/Channel.list.dev";

    // Associations
    private final BotClient botClient;

    // Constructor
    public Main(ArrayList<String> args) {
        JsonUtility jsonUtility = new JsonUtility();

        String botConfigPath;
        String channelListPath;

        // Check if Dev Mode
        if (args.contains("-dev")) {
            botConfigPath = DEV_CONFIG;
            channelListPath = DEV_LIST;
        } else {
            botConfigPath = BOT_CONFIG;
            channelListPath = CHANNEL_LIST;
        }

        // CLI check
        Frame frame = null;
        if (!(args.contains("-cli") || args.contains("-nogui"))) frame = new Frame(this);

        // Logging check
        MySQL mySQL = new MySQL(jsonUtility.load(MYSQL_CONFIG), frame, args.contains("-nolog"));

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
        if (!args.contains("-dev")) channels.addAll(mySQL.getActiveChannels());

        // Load OpenAI Config
        OpenAI openAI = new OpenAI(jsonUtility.load(OPENAI_CONFIG));

        // Init Bot
        botClient = new BotClient(botName, botToken, prefix, admins, channels, mySQL, openAI);
    }

    // PSVM
    public static void main(String[] args) {
        ArrayList<String> arguments = new ArrayList<>();
        for (String arg : args) if (arg.startsWith("-")) arguments.add(arg);
        new Main(arguments);
    }

    // Getter
    public BotClient getBotClient() {
        return botClient;
    }
}