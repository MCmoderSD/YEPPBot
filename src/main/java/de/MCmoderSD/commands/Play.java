package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Mimic;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;

public class Play extends Mimic {

    // Constructor
    public Play(BotClient botClient, MessageHandler messageHandler) {

        // Call Mimic constructor
        super(botClient, messageHandler, "play");
    }
}