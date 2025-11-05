package de.MCmoderSD.handlers;

import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;

import java.util.ArrayList;
import java.util.HashSet;

import static de.MCmoderSD.utilities.MessageHelper.*;

public class MessageHandler {

    // Associations
    private final TwitchBot twitchBot;

    // Handlers
    private final BirthdayHandler birthdayHandler;
    private final LurkHandler lurkHandler;
    private final CommandHandler commandHandler;

    // Attributes
    private final TwitchUser botUser;
    private final HashSet<String> botAliases;
    private final ArrayList<String> prefixes;

    // Constructor
    public MessageHandler(TwitchBot twitchBot) {

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");

        // Set Associations
        this.twitchBot = twitchBot;

        // Initialize Handlers
        birthdayHandler = new BirthdayHandler(twitchBot);
        lurkHandler = new LurkHandler(twitchBot);
        commandHandler = new CommandHandler(twitchBot);

        // Initialize Attributes
        botUser = twitchBot.getBotUser();
        botAliases = twitchBot.getBotAliases();
        prefixes = twitchBot.getPrefixes();
    }

    private boolean isCommand(String message) {
        for (var prefix : prefixes) if (message.startsWith(prefix)) return true;
        for (var prefix : prefixes) if (message.contains(SPACE + prefix)) return true;
        return false;
    }

    private boolean mentionsBot(String message) {
        message = message.toLowerCase();
        for (var alias : botAliases) if (message.contains(alias.toLowerCase())) return true;
        return message.contains(botUser.getUsername());
    }

    public boolean handleMessage(MessageEvent event) {

        // Check Parameters
        if (event == null) throw new IllegalArgumentException("MessageEvent cannot be null");

        // Handle Birthday
        birthdayHandler.handleBirthday(event);

        // Handle Lurk
        lurkHandler.handleLurk(event);

        // Handle Command
        if (isCommand(event.getMessage())) return commandHandler.handleCommand(event);

        // Handle YEPP
        if (mentionsBot(event.getMessage())) return twitchBot.sendMessage(event, "YEPP", tagUser(event.getUser(), "YEPP"));
        else if (event.getMessage().toUpperCase().contains("YEP")) return twitchBot.sendMessage(event, "YEPP", "YEPP");

        // Default
        return true;
    }

    // Getter
    public BirthdayHandler getBirthdayHandler() {
        return birthdayHandler;
    }

    public LurkHandler getLurkHandler() {
        return lurkHandler;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }
}