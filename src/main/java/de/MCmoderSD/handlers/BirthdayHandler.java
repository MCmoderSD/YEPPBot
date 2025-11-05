package de.MCmoderSD.handlers;

import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.database.Database;
import de.MCmoderSD.helix.core.HelixHandler;
import de.MCmoderSD.helix.handler.*;
import de.MCmoderSD.objects.MessageEvent;

public class BirthdayHandler {

    // Associations
    private final TwitchBot twitchBot;

    // Database
    private final Database database;

    // Constructor
    public BirthdayHandler(TwitchBot twitchBot) {

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");

        // Set Associations
        this.twitchBot = twitchBot;

        // Set Database
        database = twitchBot.getDatabase();
    }

    public void handleBirthday(MessageEvent event) {
        new Thread(() -> {
            // ToDo LOGIC
        }, "Handle-Birthday-" + event.getId().toString()).start();
    }
}
