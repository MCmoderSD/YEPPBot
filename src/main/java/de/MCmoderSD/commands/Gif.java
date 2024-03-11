package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonNode;
import de.MCmoderSD.utilities.json.JsonUtility;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Gif {

    // Attributes
    private final String url;
    private final String apiKey;
    private final String query;

    // Constructor
    public Gif(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // About
        String[] name = {"gif", "giphy", "gify"};
        String description = "Sendet ein GIF zu einem bestimmten Thema. Verwendung: " + commandHandler.getPrefix() + "gif <Thema>";

        // Load API key
        JsonUtility jsonUtility = new JsonUtility();
        JsonNode config = jsonUtility.load("/api/Giphy.json");
        url = config.get("url").asText();
        apiKey = config.get("api_key").asText();
        query = config.get("query").asText();

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Check arguments
                String topic = trimMessage(processArgs(args));

                // Send message
                String response = trimMessage(gif(topic));
                chat.sendMessage(getChannel(event), response);

                // Log response
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }

    // Get GIF
    private String gif(String topic) {
        try {
            URL url = new URL(this.url + apiKey + query + topic);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) return "Fehler: " + responseCode;
            Scanner scannerResponse = new Scanner(url.openStream());
            StringBuilder responseBody = new StringBuilder();
            while (scannerResponse.hasNext()) responseBody.append(scannerResponse.nextLine());
            scannerResponse.close();
            JSONObject jsonResponse = new JSONObject(responseBody.toString());
            JSONObject data = jsonResponse.getJSONObject("data");
            return data.getString("url");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return "Fehler beim Abrufen des GIFs.";
        }
    }
}