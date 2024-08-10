package de.MCmoderSD.core;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.commands.Command;
import de.MCmoderSD.main.Main;
import de.MCmoderSD.objects.Timer;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Random;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class MessageHandler {

    // Associations
    private final BotClient botClient;
    private final MySQL mySQL;
    private final Frame frame;

    // Attributes
    private final HashMap<String, Command> commandList;
    private final HashMap<String, String> aliasMap;
    private final HashMap<Integer, Integer> lurkList;
    private final HashMap<Integer, ArrayList<String>> blackList;
    private final HashMap<Integer, HashMap<String, String>> customCommands;
    private final HashMap<Integer, HashMap<String, String>> customAliases;
    private final HashMap<Integer, HashMap<String, Integer>> counters;
    private final HashMap<Integer, ArrayList<Timer>> customTimers;

    // Utilities
    private final Random random;

    // Constructor
    public MessageHandler(BotClient botClient, MySQL mySQL, Frame frame) {

        // Initialize Associations
        this.botClient = botClient;
        this.mySQL = mySQL;
        this.frame = frame;

        // Initialize Attributes
        commandList = new HashMap<>();
        aliasMap = new HashMap<>();
        lurkList = new HashMap<>();
        blackList = new HashMap<>();
        customCommands = new HashMap<>();
        customAliases = new HashMap<>();
        counters = new HashMap<>();
        customTimers = new HashMap<>();
        random = new Random();

        // Update Lists
        updateLurkList(mySQL.getLurkManager().getLurkList());
        updateBlackList(mySQL.getChannelManager().getBlackList());
        updateCustomCommands(mySQL.getCustomManager().getCustomCommands(), mySQL.getCustomManager().getCustomAliases());
        updateCounters(mySQL.getCustomManager().getCustomCounters());
        updateCustomTimers(mySQL.getCustomManager().getCustomTimers(botClient));
    }

    // Handle Methods
    public void handleMessage(TwitchMessageEvent event) {
        new Thread(() -> {

            // Log Message
            mySQL.getLogManager().logMessage(event);
            event.logToConsole();
            if (!botClient.hasArg(Main.Argument.CLI))
                frame.log(event.getType(), event.getChannel(), event.getUser(), event.getMessage());

            // Handle Timers;
            handleTimers(event);

            // Check for Lurk
            if (lurkList.containsKey(event.getUserId())) handleLurk(event);

            // Check for Command
            if (event.hasCommand()) {
                handleCommand(event);
                return;
            }

            // Reply YEPP
            if (event.hasBotName()) {
                botClient.respond(event, "replyYEPP", tagUser(event) + " YEPP");
                return;
            }

            // Say YEPP
            if (event.hasYEPP()) botClient.respond(event, "YEPP", "YEPP");
        }).start();
    }

    private void handleTimers(TwitchMessageEvent event) {
        new Thread(() -> {
            if (customTimers.containsKey(event.getChannelId())) customTimers.get(event.getChannelId()).forEach(Timer::trigger);
        }).start();
    }

    private void handleLurk(TwitchMessageEvent event) {
        new Thread(() -> {

            // Variables
            var channelID = event.getChannelId();
            var userID = event.getUserId();
            String response;

            HashMap<Timestamp, ArrayList<Integer>> lurker;
            Timestamp start;
            ArrayList<Integer> lurkChannel;

            try {
                lurker = mySQL.getLurkManager().getLurkTime(userID);
                start = lurker.keySet().iterator().next();
                lurkChannel = lurker.get(start);
            } catch (NoSuchElementException e) {
                return;
            }

            if (channelID == lurkChannel.getFirst()) { // Stop lurking

                // Remove user from lurk list
                updateLurkList(mySQL.getLurkManager().removeLurker(userID));

                // Send message
                response = tagUser(event) + " war " + formatLurkTime(start) + " im Lurk!";
                botClient.respond(event, "stoppedLurk", response);

            } else if (!lurkChannel.contains(channelID)) { // Snitch on lurked channel

                // Add user to traitor list
                lurkChannel.add(channelID);
                StringBuilder traitors = new StringBuilder();
                for (var i = 1; i < lurkChannel.size(); i++) traitors.append(lurkChannel.get(i)).append("\t");
                mySQL.getLurkManager().addTraitor(userID, traitors.toString());

                // Send message
                response = tagUser(event) + " ist ein verräter, hab den kek gerade im chat von " + tagChannel(event) + " gesehen!";
                botClient.respond(new TwitchMessageEvent(
                        event.getTimestamp(),
                        lurkChannel.getFirst(),
                        event.getUserId(),
                        mySQL.queryName("channels", lurkChannel.getFirst()),
                        event.getUser(),
                        event.getMessage(),
                        event.getSubMonths(),
                        event.getSubStreak(),
                        event.getSubTier(),
                        event.getBits()
                ), "traitor", response);
            }
        }).start();
    }

    private void handleCommand(TwitchMessageEvent event) {

        // Variables
        ArrayList<String> parts = formatCommand(event);
        String trigger = parts.getFirst();

        // Check for Alias
        if (aliasMap.containsKey(trigger)) {
            trigger = aliasMap.get(trigger);
            parts.set(0, trigger);
        }

        // Check for Command
        if (commandList.containsKey(trigger)) {
            if (isBlackListed(event, trigger)) return;
            Command command = commandList.get(trigger);
            parts.removeFirst();

            // Log Command
            mySQL.getLogManager().logCommand(event, trigger, processArgs(parts));

            // Execute Command
            command.execute(event, parts);
            return;
        }

        // Check for Custom Command in channel
        var channelID = event.getChannelId();
        if (customCommands.containsKey(channelID)) {

            // Check for Alias
            if (customAliases.containsKey(channelID) && customAliases.get(channelID).containsKey(trigger)) {
                trigger = customAliases.get(channelID).get(trigger);
                parts.set(0, trigger);
            }

            // Check for Command
            if (customCommands.get(channelID).containsKey(trigger)) {

                // Check for Blacklist
                if (isBlackListed(event, trigger)) return;
                String response = formatCommand(event, parts, customCommands.get(channelID).get(trigger));
                parts.removeFirst();

                // Log Command
                mySQL.getLogManager().logCommand(event, trigger, processArgs(parts));

                // Execute Command
                botClient.respond(event, "Custom: " + trigger, response);
            }
        }

        // Check for Counter
        if (counters.containsKey(channelID)) {

            // Check for Counter
            if (counters.get(channelID).containsKey(trigger)) {

                var currentValue = counters.get(channelID).get(trigger);
                String response = mySQL.getCustomManager().editCounter(event, trigger, currentValue + 1);
                parts.removeFirst();

                // Log Command
                mySQL.getLogManager().logCommand(event, trigger, processArgs(parts));

                // Execute Command
                botClient.respond(event, "Counter: " + trigger, response);
            }
        }
    }

    // Message Formatting
    private ArrayList<String> formatCommand(TwitchMessageEvent event) {

        // Variables
        String message = event.getMessage();

        // Find Start
        if (message.indexOf(botClient.getPrefix()) == 0) message = message.substring(1);
        else message = message.substring(message.indexOf(" " + botClient.getPrefix()) + 2);

        // Split Command
        String[] split = trimMessage(message).split(" ");
        return new ArrayList<>(Arrays.asList(split));
    }

    private String formatCommand(TwitchMessageEvent event, ArrayList<String> args, String response) {

        // Replace Variables
        if (response.contains("%random%")) response = response.replaceAll("%random%", random.nextInt(100) + "%");
        if (response.contains("%channel%")) response = response.replaceAll("%channel%", tagChannel(event));

        if (response.contains("%user%") || response.contains("%author%")) {
            response = response.replaceAll("%user%", tagUser(event));
            response = response.replaceAll("%author%", tagUser(event));
        }

        if (response.contains("%tagged%")) {
            String tagged;
            if (!args.isEmpty()) tagged = args.getFirst().startsWith("@") ? args.getFirst() : "@" + args.getFirst();
            else tagged = tagUser(event);
            response = response.replaceAll("%tagged%", tagged);
        }

        return response;
    }

    private String formatLurkTime(Timestamp startTime) {

        // Variables
        StringBuilder response = new StringBuilder();
        long time = getTimestamp().getTime() - startTime.getTime();

        // Years
        long years = time / 31536000000L;
        time %= 31536000000L;
        if (years > 1) response.append(years).append(" Jahre, ");
        else if (years > 0) response.append(years).append(" Jahr, ");

        // Months
        long months = time / 2592000000L;
        time %= 2592000000L;
        if (months > 1) response.append(months).append(" Monate, ");
        else if (months > 0) response.append(months).append(" Monat, ");

        // Weeks
        long weeks = time / 604800000L;
        time %= 604800000L;
        if (weeks > 1) response.append(weeks).append(" Wochen, ");
        else if (weeks > 0) response.append(weeks).append(" Woche, ");

        // Days
        long days = time / 86400000L;
        time %= 86400000L;
        if (days > 1) response.append(days).append(" Tage, ");
        else if (days > 0) response.append(days).append(" Tag, ");

        // Hours
        long hours = time / 3600000L;
        time %= 3600000L;
        if (hours > 1) response.append(hours).append(" Stunden, ");
        else if (hours > 0) response.append(hours).append(" Stunde, ");

        // Minutes
        long minutes = time / 60000L;
        time %= 60000L;
        if (minutes > 1) response.append(minutes).append(" Minuten, ");
        else if (minutes > 0) response.append(minutes).append(" Minute, ");

        // Seconds
        long seconds = time / 1000L;
        if (seconds > 1) response.append(seconds).append(" Sekunden, ");
        else if (seconds > 0) response.append(seconds).append(" Sekunde, ");

        // Return
        return response.substring(0, response.length() - 2);
    }

    // Register Command
    public void addCommand(Command command) {

        // Register command
        String name = command.getCommand().toLowerCase();
        commandList.put(name, command);

        // Register aliases
        for (String alias : command.getAlias()) aliasMap.put(alias.toLowerCase(), name);
    }

    // Update Lurk List
    public void updateLurkList(HashMap<Integer, Integer> lurkList) {
        this.lurkList.clear();
        this.lurkList.putAll(lurkList);
    }

    // Update Black List
    public void updateBlackList(HashMap<Integer, ArrayList<String>> blackList) {
        this.blackList.clear();
        this.blackList.putAll(blackList);
    }

    // Update Custom Commands
    public void updateCustomCommands(HashMap<Integer, HashMap<String, String>> customCommands, HashMap<Integer, HashMap<String, String>> customAliases) {
        this.customCommands.clear();
        this.customCommands.putAll(customCommands);
        this.customAliases.clear();
        this.customAliases.putAll(customAliases);
    }

    // Update Counters
    public void updateCounters(HashMap<Integer, HashMap<String, Integer>> counters) {
        this.counters.clear();
        this.counters.putAll(counters);
    }

    // Update Custom Timers
    public void updateCustomTimers(HashMap<Integer, ArrayList<Timer>> customTimers) {
        this.customTimers.clear();
        this.customTimers.putAll(customTimers);
    }

    public void updateCustomTimers(int channelID, ArrayList<Timer> customTimers) {
        this.customTimers.replace(channelID, customTimers);
    }

    // Getter
    public boolean isBlackListed(TwitchMessageEvent event, String command) {
        if (!blackList.containsKey(event.getChannelId())) return false;
        else return blackList.get(event.getChannelId()).contains(command.toLowerCase());
    }

    public boolean checkLurk(TwitchMessageEvent event) {
        return lurkList.containsKey(event.getUserId());
    }

    public boolean checkCommand(String command) {
        return commandList.containsKey(command);
    }

    public boolean checkAlias(String alias) {
        return aliasMap.containsKey(alias);
    }

    public HashMap<String, Command> getCommandList() {
        return commandList;
    }

    public HashMap<String, String> getAliasMap() {
        return aliasMap;
    }
}