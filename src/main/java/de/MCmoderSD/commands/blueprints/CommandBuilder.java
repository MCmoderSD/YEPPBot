package de.MCmoderSD.commands.blueprints;

import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.database.Database;
import de.MCmoderSD.database.manager.*;
import de.MCmoderSD.handlers.CommandHandler;
import de.MCmoderSD.helix.core.HelixHandler;
import de.MCmoderSD.helix.handler.*;
import de.MCmoderSD.helix.objects.TwitchUser;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class CommandBuilder {

    // Associations
    protected final TwitchBot twitchBot;

    // Database
    protected final Database database;
    protected final ChannelManager channelManager;
    protected final CommandManager commandManager;
    protected final EventLogManager eventLogManager;
    protected final BirthdayManager birthdayManager;
    protected final LurkManager lurkManager;

    // Handlers
    protected final HelixHandler helixHandler;
    protected final UserHandler userHandler;
    protected final ChatHandler chatHandler;
    protected final RoleHandler roleHandler;
    protected final StreamHandler streamHandler;
    protected final ChannelHandler channelHandler;

    // Command Handler
    protected final CommandHandler commandHandler;

    // Configuration Constants
    protected final TwitchUser botUser;
    protected final HashSet<TwitchUser> owners;
    protected final HashSet<String> botAliases;
    protected final ArrayList<String> prefixes;
    protected final String prefix;

    // Constructor
    public CommandBuilder(TwitchBot twitchBot) {

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");

        // Set Associations
        this.twitchBot = twitchBot;

        // Set Database
        database = twitchBot.getDatabase();
        channelManager = database.getChannelManager();
        commandManager = database.getCommandManager();
        eventLogManager = database.getEventLogManager();
        birthdayManager = database.getBirthdayManager();
        lurkManager = database.getLurkManager();

        // Set Handlers
        helixHandler = twitchBot.getHelixHandler();
        userHandler = helixHandler.getUserHandler();
        chatHandler = helixHandler.getChatHandler();
        roleHandler = helixHandler.getRoleHandler();
        streamHandler = helixHandler.getStreamHandler();
        channelHandler = helixHandler.getChannelHandler();

        // Set Configuration Constants
        botUser = twitchBot.getBotUser();
        owners = twitchBot.getOwners();
        botAliases = twitchBot.getBotAliases();
        prefixes = twitchBot.getPrefixes();
        prefix = twitchBot.getPrefix();

        // Set CommandHandler
        commandHandler = twitchBot.getEventHandler().getCommandHandler();
    }
}