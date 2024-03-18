package de.MCmoderSD.main;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.core.BotClient;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonNode;
import de.MCmoderSD.utilities.json.JsonUtility;
import de.MCmoderSD.utilities.other.OpenAI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

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

        // Check if logging is disabled
        MySQL mySQL;
        if (args.contains("-nolog")) mySQL = new MySQL(frame);
        else mySQL = new MySQL(jsonUtility.load(MYSQL_CONFIG), frame);

        // Load Bot Config
        JsonNode botConfig = jsonUtility.load(botConfigPath);

        String botName = botConfig.get("botName").asText();     // Get Bot Name
        String botToken = botConfig.get("botToken").asText();   // Get Bot Token
        String prefix = botConfig.get("prefix").asText();       // Get Prefix
        String[] admins = botConfig.get("admins").asText().toLowerCase().split("; ");

        // Load Channel List
        String[] channels = null;
        try {
            ArrayList<String> lines = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();
            InputStream inputStream = getClass().getResourceAsStream(channelListPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) lines.add(line);
            for (String name : lines) if (name.length() > 3) names.add(name.replace("\n", "").replace(" ", ""));
            channels = new String[names.size()];
            names.toArray(channels);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        if (channels == null) throw new IllegalArgumentException("Channel List is empty!");

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