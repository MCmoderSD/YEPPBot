package de.MCmoderSD.main;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonNode;
import de.MCmoderSD.utilities.json.JsonUtility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

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
        String[] channels = null;
         try {
             ArrayList<String> lines = new ArrayList<>();
             ArrayList<String> names = new ArrayList<>();
             InputStream inputStream = getClass().getResourceAsStream("/config/Channel.list");
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