package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Wiki {

    // Constructor
    public Wiki(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // About
        String[] name = {"wiki", "wikipedia", "summarize", "zusammenfassung"};
        String description = "Sucht auf Wikipedia nach einem Thema und gibt eine Zusammenfassung zurück. Verwendung: " + commandHandler.getPrefix() + "wiki <Thema>";


        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String channel = getChannel(event);

                // Query Wikipedia
                String topic = processArgs(args);
                while (topic.charAt(topic.length() - 1) == ' ') topic = topic.trim(); // Remove leading spaces
                String summary;
                try {
                    summary = trimMessage(getWikipediaSummary(topic)); // Get Wikipedia summary

                    // Send summary
                    //todo: make work for summaries longer than 1.000 characters
                    if (summary.length() <= 500) chat.sendMessage(channel, summary); // Send summary
                    else {
                        while (summary.length() > 500) {
                            var endOfSentence = summary.lastIndexOf('.', 500);
                            if (endOfSentence == -1) endOfSentence = summary.lastIndexOf(' ', 500);
                            if (endOfSentence == -1) endOfSentence = 500;

                            // Send message and log response
                            String response = summary.substring(0, endOfSentence + 1);
                            chat.sendMessage(event.getChannel().getName(), response);
                            mySQL.logResponse(event, getCommand(), processArgs(args), response);

                            // Update summary
                            summary = summary.substring(endOfSentence + 1);
                        }
                    }
                } catch (IOException e) {
                    chat.sendMessage(channel, "Fehler beim Abrufen des Wikipedia-Artikels: " + e.getMessage()); // Send error message
                }
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