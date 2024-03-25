package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.json.JsonNode;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.other.OpenAI;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Wiki {

    // Attributes
    private final int maxTokens;
    private final double temperature;

    // Constructor
    public Wiki(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat, OpenAI openAI, String botName) {

        // About
        String[] name = {"wiki", "wikipedia", "summarize", "zusammenfassung"};
        String description = "Sucht auf Wikipedia nach einem Thema und gibt eine Zusammenfassung zurück. Verwendung: " + commandHandler.getPrefix() + "wiki <Thema>";

        // Load Config
        JsonNode config = openAI.getConfig();
        maxTokens = config.get("maxTokens").asInt();
        temperature = 0;

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String channel = getChannel(event);

                // Query Wikipedia
                String topic = processArgs(args);

                String response;

                try {

                    // Get Wikipedia summary
                    String summary = trimMessage(getWikipediaSummary(topic)); // Get Wikipedia summary

                    // Check if summary is too long
                    if (summary.length() <= 500) response = summary;
                    else response = trimMessage(openAI.prompt(botName, "Please summarize the following text using the original language used in the text. Answer only in 500 or less chars", summary, maxTokens, temperature));

                } catch (IOException e) {
                    response = trimMessage("Fehler beim Abrufen des Wikipedia-Artikels: " + e.getMessage());
                }

                // Send message and log response
                chat.sendMessage(channel, response);

                // Log response
                mySQL.logResponse(event, getCommand(), topic, response);
            }
        });
    }

    // Get Wikipedia summary
    private String getWikipediaSummary(String topic) throws IOException {
        String apiUrl = "https://de.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=true&explaintext=true&redirects=true&titles=" + URLEncoder.encode(topic, StandardCharsets.UTF_8);
        Document doc = Jsoup.connect(apiUrl).ignoreContentType(true).get();
        JSONObject json = new JSONObject(doc.text());
        JSONObject pages = json.getJSONObject("query").getJSONObject("pages");
        String firstPageKey = pages.keys().next();
        JSONObject page = pages.getJSONObject(firstPageKey);
        if (page.has("extract")) return page.getString("extract");
        else throw new IOException("Keine Zusammenfassung für dieses Thema gefunden.");
    }
}