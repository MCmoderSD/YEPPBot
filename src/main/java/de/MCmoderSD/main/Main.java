package de.MCmoderSD.main;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonNode;
import de.MCmoderSD.utilities.json.JsonUtility;

import java.util.Arrays;

public class Main {

    // Constructor
    public Main(String... args) {
        JsonUtility jsonUtility = new JsonUtility();

        // Load Bot Config
        JsonNode botConfig = jsonUtility.load("/config/BotConfig.json");

        String botName = botConfig.get("botName").asText();     // Get Bot Name
        String botToken = botConfig.get("botToken").asText();   // Get Bot Token
        String prefix = botConfig.get("prefix").asText();       // Get Prefix
        String[] admins = botConfig.get("admins").asText().split("; ");

        // Load Channel List
        JsonNode channelList = jsonUtility.load("/config/ChannelList.json");
        String[] channels = new String[channelList.getSize()];
        for (int i = 0; i < channelList.getSize(); i++) channels[i] = channelList.get("#" + i).asText();

        // Load MySQL Config
        MySQL mySQL = new MySQL(jsonUtility.load("/database/mySQL.json"));

        // Init Bot
        new BotClient(botName, botToken, prefix, admins, channels, mySQL);

        // CLI check
        if (args.length > 0 && (args[0].equals("-cli") || args[0].equals("-nogui"))) return;

        // Init GUI
        new Frame();
    }

    public static void main(String[] args) {
        new Main(args);
    }
}