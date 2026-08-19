package de.MCmoderSD.handlers;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.database.manager.ChannelManager;
import de.MCmoderSD.database.manager.MessageManager;
import de.MCmoderSD.database.manager.CommandManager;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.openai.services.EmbeddingService;
import de.MCmoderSD.openai.services.ModerationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import static de.MCmoderSD.utilities.MessageHelper.*;

public class CommandHandler {

    // Database
    private final ChannelManager channelManager;
    private final MessageManager messageManager;
    private final CommandManager commandManager;

    // OpenAI Service
    private final EmbeddingService embeddingService;
    private final ModerationService moderationService;

    // Attributes
    private final ArrayList<String> prefixes;
    private final HashMap<String, Command> commandMap;
    private final HashMap<String, String> aliasMap;
    private final ConcurrentHashMap<TwitchUser, HashSet<String>> blacklist;
    private final ConcurrentHashMap<TwitchUser, HashMap<String, Command>> customCommands;
    private final ConcurrentHashMap<TwitchUser, HashMap<String, String>> customAliases;

    // Constructor
    public CommandHandler(TwitchBot twitchBot) {

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");

        // Set Database
        channelManager = twitchBot.getChannelManager();
        messageManager = twitchBot.getMessageManager();
        commandManager = twitchBot.getCommandManager();

        // Set OpenAI Service
        embeddingService = twitchBot.getOpenAI() == null ? null : twitchBot.getOpenAI().embeddings();
        moderationService = twitchBot.getOpenAI() == null ? null : twitchBot.getOpenAI().moderations();

        // Initialize Attributes
        prefixes = twitchBot.getPrefixes();
        commandMap = new HashMap<>();
        aliasMap = new HashMap<>();
        customCommands = new ConcurrentHashMap<>();
        customAliases = new ConcurrentHashMap<>();
        blacklist = new ConcurrentHashMap<>(channelManager.getBlacklist());
    }

    // Fetch Blacklist from Database
    public int fetchBlacklist() {

        // Reload Blacklist
        updateBlacklist(channelManager.getBlacklist());

        // Count Entries
        var entries = 0;
        for (var commands : blacklist.values()) entries += commands.size();
        return entries;
    }

    // Update Blacklist
    private void updateBlacklist(HashMap<TwitchUser, HashSet<String>> blacklist) {

        // Check Parameters
        if (blacklist == null) throw new IllegalArgumentException("Blacklist cannot be null");

        // Update Blacklist
        this.blacklist.clear();
        this.blacklist.putAll(blacklist);
    }

    // Format Command
    private ArrayList<String> formatCommand(MessageEvent event) {

        // Variables
        var message = event.getMessage();

        // Find Prefix
        String prefix = null;
        for (var p : prefixes) if (message.startsWith(p) || message.contains(SPACE + p)) prefix = p;
        if (prefix == null) throw new IllegalStateException("Prefix not found in message: " + message);

        // Find Start
        if (message.indexOf(prefix) == 0) message = message.substring(1);       // Remove Prefix
        else message = message.substring(message.indexOf(SPACE + prefix) + 2);  // Remove Space and Prefix

        // Split Arguments
        return new ArrayList<>(Arrays.asList(normalizeMessage(message).split(SPACE)));
    }

    // Check Blacklist
    private boolean isBlacklisted(MessageEvent event, String command) {
        if (!blacklist.containsKey(event.getChannel())) return false;
        return blacklist.get(event.getChannel()).contains(command);
    }

    // Check if Command is Blacklisted
    private boolean isBlacklisted(TwitchUser channel, String command) {

        // Check Parameters
        if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
        if (command == null || command.isBlank()) throw new IllegalArgumentException("Command cannot be null or blank");

        // Normalize Command
        command = command.toLowerCase();

        // Convert Alias to Command
        if (aliasMap.containsKey(command)) command = aliasMap.get(command);

        // Check Blacklist
        if (!blacklist.containsKey(channel)) return false;
        return blacklist.get(channel).contains(command);
    }

    // Handle Command
    public boolean handleCommand(MessageEvent event) {

        // Check Parameters
        if (event == null) throw new IllegalArgumentException("MessageEvent cannot be null");

        // Variables
        var channel = event.getChannel();
        var parts = formatCommand(event);
        var trigger = parts.getFirst().toLowerCase();

        // Check for Alias
        if (aliasMap.containsKey(trigger)) {
            trigger = aliasMap.get(trigger);
            parts.set(0, trigger);
        }

        // Check for Command
        if (commandMap.containsKey(trigger)) {

            // Check Blacklist
            if (isBlacklisted(event, trigger)) return false;

            // Get Command
            var command = commandMap.get(trigger);
            parts.removeFirst();

            // Execute Command
            var success = command.execute(event, parts);

            // Log Command
            if (success) {
                System.out.printf("%s <%s> #%s executed command: %s%n", COMMAND, channel.getDisplayName(), event.getUser().getDisplayName(), trigger);

                // Join Args
                var args = String.join(SPACE, parts);

                // Insert Message and Related Data
                if (!args.isBlank() && messageManager.insertMessage(args) && !(embeddingService == null || moderationService == null)) {
                    new Thread(() -> messageManager.insertRating(moderationService.create(args))).start();
                    new Thread(() -> messageManager.insertEmbedding(embeddingService.create(args))).start();
                }

                // Log Command
                commandManager.logCommand(event, trigger, args);
            }

            // Return
            return success;
        }

        var alias = customAliases.get(channel);

        // Check for Custom Command
        if (alias != null && alias.containsKey(trigger)) {
            trigger = alias.get(trigger);
            parts.set(0, trigger);
        }

        // Check for Custom Command
        var custom = customCommands.get(channel);
        if (custom != null && custom.containsKey(trigger)) {

            // Get Command
            var command = custom.get(trigger);
            parts.removeFirst();

            // Execute Command
            var success = command.execute(event, parts);

            // Log Command
            if (success) {
                System.out.printf("%s <%s> #%s executed command: %s%n", COMMAND, channel.getDisplayName(), event.getUser().getDisplayName(), trigger);

                // Join Args
                var args = String.join(SPACE, parts);

                // Insert Message and Related Data
                if (!args.isBlank() && messageManager.insertMessage(args) && !(embeddingService == null || moderationService == null)) {
                    new Thread(() -> messageManager.insertRating(moderationService.create(args))).start();
                    new Thread(() -> messageManager.insertEmbedding(embeddingService.create(args))).start();
                }

                // Log Command
                commandManager.logCommand(event, trigger, args);
            }
        }

        // Command not found
        return false;
    }

    // Register Command
    @SuppressWarnings("SameReturnValue")
    public boolean registerCommand(Command command) {

        // Check Parameters
        if (command == null) throw new IllegalArgumentException("Command cannot be null");
        
        // Variables
        var name = command.getName().toLowerCase();
        
        // Check if Command already exists
        if (commandMap.containsKey(name)) throw new IllegalArgumentException("Command " + command.getName() + " is already registered");
        for (var alias : command.getAliases()) if (aliasMap.containsKey(alias.toLowerCase())) throw new IllegalArgumentException("Alias " + alias + " is already registered");
        
        // Register Command
        commandMap.put(name, command);
        for (var alias : command.getAliases()) aliasMap.put(alias.toLowerCase(), name);

        // Return
        return true;
    }

    public boolean registerCustomCommand(TwitchUser twitchUser, Command command) {

        // Check Parameters
        if (command == null) throw new IllegalArgumentException("Command cannot be null");

        // Variables
        var name = command.getName().toLowerCase();

        // Check if Command already exists
        if (commandMap.containsKey(name)) return false;
        for (var alias : command.getAliases()) if (aliasMap.containsKey(alias.toLowerCase())) return false;

        // Register Custom Command
        if (!customCommands.containsKey(twitchUser)) customCommands.put(twitchUser, new HashMap<>());
        if (!customAliases.containsKey(twitchUser)) customAliases.put(twitchUser, new HashMap<>());

        // Check if Custom Command already exists
        if (customCommands.get(twitchUser).containsKey(name)) return false;
        for (var alias : command.getAliases()) if (customAliases.get(twitchUser).containsKey(alias.toLowerCase())) return false;

        // Register Command
        customCommands.get(twitchUser).put(name, command);
        for (var alias : command.getAliases()) customAliases.get(twitchUser).put(alias.toLowerCase(), name);

        // Return
        return true;
    }

    public void resetCustomCommands(TwitchUser twitchUser) {

        // Check Parameters
        if (twitchUser == null) throw new IllegalArgumentException("TwitchUser cannot be null");

        // Reset Custom Commands
        customCommands.remove(twitchUser);
        customAliases.remove(twitchUser);
    }

    public HashSet<Command> getCommands(TwitchUser channel) {

        // Check Parameters
        if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

        // Variables
        var commands = new HashSet<Command>();

        // Get Commands
        for (var entry : commandMap.entrySet()) {

            // Get Command
            var commandName = entry.getKey();
            var command = entry.getValue();

            // Check Blacklist
            if (!isBlacklisted(channel, commandName)) commands.add(command);
        }

        // Return
        return commands;
    }

    // Check if Command is Blacklisted
    public boolean blacklistAdd(TwitchUser channel, String command) {

        // Check Parameters
        if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
        if (command == null || command.isBlank()) throw new IllegalArgumentException("Command cannot be null or blank");

        // Normalize Command
        command = command.toLowerCase();

        // Convert Alias to Command
        if (aliasMap.containsKey(command)) command = aliasMap.get(command);

        // Check if command exists
        if (!commandMap.containsKey(command)) return false;

        // Check if already blacklisted
        if (isBlacklisted(channel, command)) return false;

        // Update Blacklist
        updateBlacklist(channelManager.blacklistAdd(channel, command));
        return true;
    }

    // Check if Command is Blacklisted
    public boolean blacklistRemove(TwitchUser channel, String command) {

        // Check Parameters
        if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
        if (command == null || command.isBlank()) throw new IllegalArgumentException("Command cannot be null or blank");

        // Normalize Command
        command = command.toLowerCase();

        // Convert Alias to Command
        if (aliasMap.containsKey(command)) command = aliasMap.get(command);

        // Check if command exists
        if (!commandMap.containsKey(command)) return false;

        // Check if not blacklisted
        if (!isBlacklisted(channel, command)) return false;

        // Update Blacklist
        updateBlacklist(channelManager.blacklistRemove(channel, command));
        return true;
    }
}