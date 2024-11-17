package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.Timer;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.CustomManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static de.MCmoderSD.utilities.other.Format.*;

public class CustomTimer {

    // Constructor
    public CustomTimer(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // ToDo Make it possible to create timers with hours, minutes and seconds

        // Variables
        CustomManager customManager = mySQL.getCustomManager();
        String prefix = botClient.getPrefix();
        //String syntax = prefix + "CustomTimer create/enable/disable/delete/list name 1000M/500h/30m/10s : response";
        String syntax = prefix + "CustomTimer create/enable/disable/delete/list name 1000M : response";

        // About
        String[] name = {"customtimer", "ctimer", "ct"};
        //String description = "Kann benutzt werden, um eigene Timer zu erstellen M = Messages, h = hours m = minutes, s = seconds. Syntax: " + syntax;
        String description = "Kann benutzt werden, um eigene Timer zu erstellen M = Messages. Syntax: " + syntax;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Variables
                String response = syntax;

                // Check if user is a moderator
                if (!(botClient.isPermitted(event) || botClient.isAdmin(event))) return;

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Get Custom Timers
                HashMap<String, Timer> customTimers = customManager.getCustomTimers(event, botClient);

                if (!args.isEmpty() && args.getFirst().equalsIgnoreCase("list")) {
                    botClient.respond(event, getCommand(), getCustomTimerNames(event.getChannel(), customTimers, customManager.getActiveCustomTimers(event, botClient)));
                    return;
                }

                // Get Custom Timer syntax
                HashMap<String, Integer> message = new HashMap<>();
                for (var i = 1; i < args.size(); i++) message.put(args.get(i), i);

                if (args.size() < 2) botClient.respond(event, getCommand(), response);
                else if (Arrays.asList("create", "add", "enable", "disable", "delete", "del", "remove", "rm", "list").contains(args.getFirst().toLowerCase())) {

                    // Get Custom Timer name
                    String timerName = args.get(1).toLowerCase();

                    // Process message
                    switch (args.getFirst().toLowerCase()) {

                        // Enable Custom Timer
                        case "enable":
                            response = customTimers.containsKey(timerName) ? customManager.editCustomTimer(event, timerName, customTimers.get(timerName).getTime() + "M", customTimers.get(timerName).getResponse(), true) : "Custom Timer does not exist";
                            break;

                        // Disable Custom Timer
                        case "disable":
                            response = customTimers.containsKey(timerName) ? customManager.editCustomTimer(event, timerName, customTimers.get(timerName).getTime() + "M", customTimers.get(timerName).getResponse(), false) : "Custom Timer does not exist";
                            break;

                        // Delete Custom Timer
                        case "delete":
                        case "del":
                        case "remove":
                        case "rm":
                            response = customTimers.containsKey(timerName) ? customManager.deleteCustomTimer(event, timerName) : "Custom Timer does not exist";
                            break;

                        // Create Custom Timer
                        case "create":
                        case "add":
                            if (!message.containsKey(":") && args.size() < 4) response = "Syntax: " + syntax;
                            else if (customTimers.containsKey(timerName)) response = "Custom Timer already exists";
                            else {

                                // Get Time
                                if (inValidCustomTime(args.get(2))) {
                                    botClient.respond(event, getCommand(), "Not valid format or to long time");
                                    return;
                                }

                                // Get Custom Timer response
                                StringBuilder stringBuilder = new StringBuilder();
                                for (var i = message.get(":") + 1; i < args.size(); i++) stringBuilder.append(args.get(i)).append(" ");
                                String timerResponse = trimMessage(stringBuilder.toString());

                                // Create command
                                response = customManager.createCustomTimer(event, timerName, args.get(2), timerResponse);
                            }
                            break;

                        // Error
                        default:
                            response = "Syntax: " + syntax;
                            break;
                    }
                } else response = "Syntax: " + syntax;

                // Update Custom Timers
                messageHandler.updateCustomTimers(event.getChannelId(), customManager.getActiveCustomTimers(event, botClient));

                // Send message
                botClient.respond(event, getCommand(), response);
            }
        });
    }

    private String getCustomTimerNames(String channel, HashMap<String, Timer> customTimers, HashSet<Timer> enabledTimers) {
        if (customTimers.isEmpty()) return "No Custom Timers available";

        StringBuilder stringBuilder = new StringBuilder("Custom Timers: ");
        HashSet<String> enabledTimersNames = new HashSet<>();

        for (var timer : enabledTimers) {
            if (timer.getChannel().equals(channel)) stringBuilder.append(timer.getName()).append(" enabled, ");
            enabledTimersNames.add(timer.getName());
        }

        for (var timer : customTimers.values()) if (timer.getChannel().equals(channel) && !enabledTimersNames.contains(timer.getName())) stringBuilder.append(timer.getName()).append(" disabled, ");

        return stringBuilder.substring(0, stringBuilder.length() - 2) + ".";
    }

    @SuppressWarnings({"EnhancedSwitchMigration"})
    private boolean inValidCustomTime(String time) {

        // Check if time is a valid number
        try {
            Long.parseLong(time.substring(0, time.length() - 1));
        } catch (NumberFormatException e) {
            return false;
        }

        long timeValue = Long.parseLong(time.substring(0, time.length() - 1));
        char timeType = time.charAt(time.length() - 1);

        //if (!(timeType == 'M' || timeType == 'h' || timeType == 'm' || timeType == 's')) return false; ToDo Fix for all
        if (!(timeType == 'M')) return false;

        switch (timeType) {
            //noinspection DataFlowIssue ToDo Fix
            case 'M', 's':
                return timeValue > 9223372036854775806L;
            /*case 'h':
                return timeValue <= 2562047788015215L;
            case 'm':
                return timeValue <= 153722867280912930L;*/
            default:
                return false;
        }
    }
}