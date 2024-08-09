package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.main.Credentials;
import de.MCmoderSD.objects.TwitchMessageEvent;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Scanner;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Gif {

    // Attributes
    private final String url;
    private final String apiKey;
    private final String query;

    // Constructor
    public Gif(BotClient botClient, MessageHandler messageHandler, Credentials credentials) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "gif <Thema>";

        // About
        String[] name = {"gif", "giphy", "gify"};
        String description = "Sendet ein GIF zu einem bestimmten Thema. " + syntax;

        // Load API key
        JsonNode config = credentials.getGiphyConfig();

        // Init Attributes
        url = config.get("url").asText();
        apiKey = config.get("api_key").asText();
        query = config.get("query").asText();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check arguments
                String topic = trimMessage(convertToAscii(processArgs(args)));

                // Send Message
                botClient.respond(event, getCommand(), trimMessage(gif(topic)));
            }
        });
    }

    // Get GIF
    private String gif(String topic) {
        try {
            URI uri = new URI(this.url + apiKey + query + topic);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            var responseCode = conn.getResponseCode();
            if (responseCode != 200) return "Error code: " + responseCode;
            Scanner scannerResponse = new Scanner(conn.getInputStream());
            StringBuilder responseBody = new StringBuilder();
            while (scannerResponse.hasNext()) responseBody.append(scannerResponse.nextLine());
            scannerResponse.close();
            JSONObject jsonResponse = new JSONObject(responseBody.toString());
            JSONObject data = jsonResponse.getJSONObject("data");
            return data.getString("url");
        } catch (URISyntaxException | IOException e) {
            System.err.println(e.getMessage());
            return "An Error occurred. While trying to get a GIF.";
        }
    }
}