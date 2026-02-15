package de.MCmoderSD.commands.blueprints;

import de.MCmoderSD.core.TwitchBot;

import de.MCmoderSD.database.Database;
import de.MCmoderSD.database.manager.ChannelManager;
import de.MCmoderSD.database.manager.MessageManager;
import de.MCmoderSD.database.manager.EventLogManager;
import de.MCmoderSD.database.manager.CommandManager;
import de.MCmoderSD.database.manager.BirthdayManager;
import de.MCmoderSD.database.manager.LurkManager;
import de.MCmoderSD.database.manager.OpenAIManger;
import de.MCmoderSD.database.manager.QueueManager;
import de.MCmoderSD.database.manager.QuoteManager;

import de.MCmoderSD.handlers.EventHandler;
import de.MCmoderSD.handlers.MessageHandler;
import de.MCmoderSD.handlers.BirthdayHandler;
import de.MCmoderSD.handlers.LurkHandler;
import de.MCmoderSD.handlers.CommandHandler;

import de.MCmoderSD.helix.core.HelixHandler;
import de.MCmoderSD.helix.handler.ChannelHandler;
import de.MCmoderSD.helix.handler.ChatHandler;
import de.MCmoderSD.helix.handler.RoleHandler;
import de.MCmoderSD.helix.handler.StreamHandler;
import de.MCmoderSD.helix.handler.UserHandler;
import de.MCmoderSD.helix.objects.TwitchUser;

import de.MCmoderSD.server.core.Server;
import de.MCmoderSD.openai.core.OpenAI;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class CommandBuilder {

    // Associations
    protected final TwitchBot twitchBot;
    protected final Server server;
    protected final OpenAI openAI;

    // Database
    protected final Database database;                  // Database
    protected final ChannelManager channelManager;      // Channel Manager
    protected final MessageManager messageManager;      // Message Manager
    protected final EventLogManager eventLogManager;    // Event Log Manager
    protected final CommandManager commandManager;      // Command Manager
    protected final BirthdayManager birthdayManager;    // Birthday Manager
    protected final LurkManager lurkManager;            // Lurk Manager
    protected final OpenAIManger openAIManger;          // OpenAI Manager
    protected final QueueManager queueManager;          // Queue Manager
    protected final QuoteManager quoteManager;          // Quote Manager

    // Helix-Handlers
    protected final HelixHandler helixHandler;          // Helix Handler
    protected final UserHandler userHandler;            // User Handler
    protected final ChatHandler chatHandler;            // Chat Handler
    protected final RoleHandler roleHandler;            // Role Handler
    protected final StreamHandler streamHandler;        // Stream Handler
    protected final ChannelHandler channelHandler;      // Channel Handler

    // Bot-Handlers
    protected final EventHandler eventHandler;          // Event Handler
    protected final MessageHandler messageHandler;      // Message Handler
    protected final BirthdayHandler birthdayHandler;    // Birthday Handler
    protected final LurkHandler lurkHandler;            // Lurk Handler
    protected final CommandHandler commandHandler;      // Command Handler

    // Configuration
    protected final TwitchUser botUser;                 // Bot User
    protected final HashSet<TwitchUser> owners;         // Owners
    protected final HashSet<String> botAliases;         // Bot Aliases
    protected final ArrayList<String> prefixes;         // Command Prefixes
    protected final String prefix;                      // Primary Command Prefix

    // Constructor
    public CommandBuilder(TwitchBot twitchBot) {

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");

        // Set Associations
        this.twitchBot = twitchBot;
        this.server = twitchBot.getServer();
        this.openAI = twitchBot.getOpenAI();

        // Set Database
        database = twitchBot.getDatabase();
        channelManager = twitchBot.getChannelManager();
        messageManager = twitchBot.getMessageManager();
        eventLogManager = twitchBot.getEventLogManager();
        commandManager = twitchBot.getCommandManager();
        birthdayManager = twitchBot.getBirthdayManager();
        lurkManager = twitchBot.getLurkManager();
        openAIManger = twitchBot.getOpenAIManger();
        queueManager = twitchBot.getQueueManager();
        quoteManager = twitchBot.getQuoteManager();

        // Set Handlers
        helixHandler = twitchBot.getHelixHandler();
        userHandler = twitchBot.getUserHandler();
        chatHandler = twitchBot.getChatHandler();
        roleHandler = twitchBot.getRoleHandler();
        streamHandler = twitchBot.getStreamHandler();
        channelHandler = twitchBot.getChannelHandler();

        // Set Bot Handlers
        eventHandler = twitchBot.getEventHandler();
        messageHandler = twitchBot.getMessageHandler();
        birthdayHandler = twitchBot.getBirthdayHandler();
        lurkHandler = twitchBot.getLurkHandler();
        commandHandler = twitchBot.getCommandHandler();

        // Set Configuration Constants
        botUser = twitchBot.getBotUser();
        owners = twitchBot.getOwners();
        botAliases = twitchBot.getBotAliases();
        prefixes = twitchBot.getPrefixes();
        prefix = twitchBot.getPrefix();
    }
}