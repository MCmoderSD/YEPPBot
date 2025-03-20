package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.openai.core.OpenAI;
import de.MCmoderSD.wikipedia.core.WikipediaAPI;
import de.MCmoderSD.wikipedia.data.Page;
import de.MCmoderSD.wikipedia.enums.Language;

import java.io.IOException;
import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Format.*;

public class Wiki {

    // Constructor
    public Wiki(BotClient botClient, MessageHandler messageHandler, OpenAI openAI) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "wiki <Thema>";

        // About
        String[] name = {"wiki", "wikipedia", "summarize", "zusammenfassung"};
        String description = "Sucht auf Wikipedia nach einem Thema und gibt eine Zusammenfassung zurück. " + syntax;

        // Constants
        String errorFetchingWikipedia = "Fehler beim Abrufen der Wikipedia-Zusammenfassung:";
        String noSummaryFoundForThisTopic = "Keine Zusammenfassung für dieses Thema gefunden.";

        // Initialize Wikipedia
        WikipediaAPI wiki = new WikipediaAPI();

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
                    try {
                        // Check for Language
                        Language language = Language.ENGLISH.getLanguage(args.getFirst());
                        if (language == null) language = Language.GERMAN;
                        else args.removeFirst(); // Remove Language

                        // Query Wikipedia
                        String topic = concatArgs(args);

                        // Wikipedia API
                        Page wikiPage;
                        try {
                            wikiPage = wiki.query(language, topic);
                        } catch (IOException | InterruptedException e) {
                            botClient.respond(event, getCommand(), noSummaryFoundForThisTopic);
                            return;
                        }

                        // Extract Summary and Check Length
                        String summary = wikiPage.getExtract();
                        if (summary.length() <= 500) response = summary;
                        else response = trimMessage(openAI.prompt(
                                null,
                                event.getUser(),
                                null,
                                0d,
                                null,
                                null,
                                null,
                                null,
                                "Please summarize the following text into " + language.getName() + " used in the text. Answer only in 500 or less chars",
                                null,
                                summary
                        ));

                        // Filter Response for argument injection
                        response = removePrefix(response);

                    } catch (Exception e) {
                        response = errorFetchingWikipedia;
                    }
                }

                // Send Message
                botClient.respond(event, getCommand(), response);
            }
        });
    }
}