package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import org.json.JSONObject;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonUtility;

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
    private final boolean isNull;

    // Constructor
    public Gif(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // Syntax
        String syntax = "Syntax: " + commandHandler.getPrefix() + "gif <Thema>";

        // About
        String[] name = {"gif", "giphy", "gify"};
        String description = "Sendet ein GIF zu einem bestimmten Thema. " + syntax;

        // Load API key
        JsonUtility jsonUtility = new JsonUtility();
        JsonNode config = jsonUtility.load("/api/Giphy.json");

        // Init Attributes
        isNull = config == null;
        url = isNull ? null : config.get("url").asText();
        apiKey = isNull ? null : config.get("api_key").asText();
        query = isNull ? null : config.get("query").asText();
        if (isNull) System.err.println(BOLD + "Giphy API missing" + UNBOLD);

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                if (isNull) return;

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
            var responseCode = conn.getResponseCode();
            if (responseCode != 200) return "Error code: " + responseCode;
            Scanner scannerResponse = new Scanner(url.openStream());
            StringBuilder responseBody = new StringBuilder();
            while (scannerResponse.hasNext()) responseBody.append(scannerResponse.nextLine());
            scannerResponse.close();
            JSONObject jsonResponse = new JSONObject(responseBody.toString());
            JSONObject data = jsonResponse.getJSONObject("data");
            return data.getString("url");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return "An Error occurred. While trying to get a GIF.";
        }
    }
}