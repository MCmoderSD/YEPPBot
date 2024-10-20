package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;

import de.MCmoderSD.OpenAI.OpenAI;
import de.MCmoderSD.OpenAI.modules.Chat;

import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.jsoup.nodes.Document;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Wiki {

    // Constants
    private final String errorRetrievingWeatherData;
    private final String noSummaryFoundForThisTopic;

    // Attributes
    private final double temperature;
    private final int maxTokens;
    private final double topP;
    private final double frequencyPenalty;
    private final double presencePenalty;

    // Constructor
    public Wiki(BotClient botClient, MessageHandler messageHandler, OpenAI openAI) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "wiki <Thema>";

        // About
        String[] name = {"wiki", "wikipedia", "summarize", "zusammenfassung"};
        String description = "Sucht auf Wikipedia nach einem Thema und gibt eine Zusammenfassung zurück. " + syntax;

        // Constants
        errorRetrievingWeatherData = "Fehler beim Abrufen der Wikipedia-Zusammenfassung:";
        noSummaryFoundForThisTopic = "Keine Zusammenfassung für dieses Thema gefunden.";

        // Get Chat Module and Config
        Chat chat = openAI.getChat();
        JsonNode config = chat.getConfig();

        // Get Parameters
        temperature = 0;
        maxTokens = config.get("maxTokens").asInt();
        topP = config.get("topP").asDouble();
        frequencyPenalty = config.get("frequencyPenalty").asDouble();
        presencePenalty = config.get("presencePenalty").asDouble();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Attributes
                String response;

                // Check for topic
                if (args.isEmpty()) response = syntax;
                else {

                    // Query Wikipedia
                    String topic = processArgs(args);
                    try {

                        // Get Wikipedia summary
                        String summary = trimMessage(getWikipediaSummary(topic)); // Get Wikipedia summary

                        // Check if summary is too long
                        if (summary.length() <= 500) response = summary;
                        else response = trimMessage(chat.prompt(botClient.getBotName(), "Please summarize the following text using the original language used in the text. Answer only in 500 or less chars", summary, temperature, maxTokens, topP, frequencyPenalty, presencePenalty));

                    } catch (IOException e) {
                        response = trimMessage(String.format("%s %s", errorRetrievingWeatherData, e.getMessage()));
                    }
                }

                // Send Message
                botClient.respond(event, getCommand(), response);
            }
        });
    }

    // Get Wikipedia summary
    private String getWikipediaSummary(String topic) throws IOException {
        String apiUrl = String.format("https://de.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=true&explaintext=true&redirects=true&titles=%s", URLEncoder.encode(topic, StandardCharsets.UTF_8));
        Document doc = Jsoup.connect(apiUrl).ignoreContentType(true).get();
        JSONObject json = new JSONObject(doc.text());
        JSONObject pages = json.getJSONObject("query").getJSONObject("pages");
        String firstPageKey = pages.keys().next();
        JSONObject page = pages.getJSONObject(firstPageKey);
        if (page.has("extract")) return page.getString("extract");
        else throw new IOException(noSummaryFoundForThisTopic);
    }
}