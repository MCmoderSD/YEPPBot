package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchUser;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.EventManager;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class DickDestroyDecember {

    // Associations
    private final HelixHandler helixHandler;
    private final EventManager eventManager;

    // Constants
    private final EventManager.Event event = EventManager.Event.DDD;

    // Responses
    private final String itsNotDecember;
    private final String userNotFound;
    private final String userHasNotJoined;
    private final String userStillIn;
    private final String userHasLeft;

    private final String youHaveNotJoined;
    private final String youAlreadyJoined;
    private final String youAlreadyLeft;
    private final String youJoined;
    private final String youLeft;
    private final String errorJoining;
    private final String errorLeaving;

    private final String nobodyJoined;
    private final String everyoneLeft;
    private final String everyoneStillIn;
    private final String listUsers;

    // Syntax
    private final String syntax;

    // Constructor
    public DickDestroyDecember(BotClient botClient, MessageHandler messageHandler, MySQL mySQL, HelixHandler helixHandler) {

        // Syntax
        syntax = "Syntax: " + botClient.getPrefix() + "DDD [ join | leave | status ] <user>";

        // About
        String[] name = {"dickdestroydecember", "ddd", "destroydickdecember", "dicdestroydecember"};
        String description = "Join, leave or check the status of the Dick Destroy December. " + syntax;

        // Init Associations
        this.helixHandler = helixHandler;
        this.eventManager = mySQL.getEventManager();

        // Init Responses
        itsNotDecember = "It's not December, you don't have to beat your meat. YEPP";
        userNotFound = "User not found.";
        userHasNotJoined = "%s hasn't joined Dick Destroy December. YEPP";
        userStillIn = "%s is still in Dick Destroy December. YEPP";
        userHasLeft = "Bro's meat already gave up on day %d YEPP";

        youHaveNotJoined = "You haven't even joined Dick Destroy December. YEPP";
        youAlreadyJoined = "You already joined Dick Destroy December. YEPP";
        youAlreadyLeft = "You already left Dick Destroy December. YEPP";
        youJoined = "You joined Dick Destroy December. YEPP";
        youLeft = "You left Dick Destroy December. YEPP";
        errorJoining = "Error: Couldn't join Dick Destroy December. YEPP";
        errorLeaving = "Error: Couldn't leave Dick Destroy December. YEPP";

        nobodyJoined = "No one has joined Dick Destroy December. YEPP";
        everyoneLeft = "Everyone's meat already gave up. YEPP";
        everyoneStillIn = "Everyone is still in Dick Destroy December. YEPP";
        listUsers = "%s is fighting, but %s already gave up. YEPP";

        // Register command
        messageHandler.addCommand(new Command(description, name) {
            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Check if December
                if (Calendar.getInstance().get(Calendar.MONTH) != Calendar.DECEMBER) {
                    botClient.respond(event, getCommand(), itsNotDecember);
                    return;
                }

                // Check args
                if (args.isEmpty()) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                // Check if just tagged
                if (args.size() == 1 && args.getFirst().startsWith("@")) {
                    botClient.respond(event, getCommand(), checkStatus(helixHandler.getUser(args.getFirst().substring(1))));
                    return;
                }

                // Check Verb
                String verb = args.getFirst().toLowerCase();

                // Check Verb
                if (!Arrays.asList("join", "leave", "status", "check", "list").contains(verb)) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                // Check Verb
                if (args.size() < 2 || Arrays.asList("status", "check").contains(verb)) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                String response = switch (verb) {
                    case "join" -> join(event.getUserId());
                    case "leave" -> leave(event.getUserId());
                    case "status", "check" -> checkStatus(helixHandler.getUser(args.get(1)));
                    case "list" -> list(event.getChannelId());
                    default -> syntax;
                };

                // Respond
                botClient.respond(event, getCommand(), response);
            }
        });
    }

    // Check Status
    private String checkStatus(TwitchUser user) {

        // Check if user exists
        if (user == null) return userNotFound;

        // Variables
        var id = user.getId();

        // Check if user has joined
        if (!eventManager.isJoined(id, event)) return String.format(userHasNotJoined, user.getName());
        if (!eventManager.hasLeft(id, event)) return String.format(userStillIn, user.getName());

        // Get Days
        Timestamp[] events = eventManager.getParticipant(id, event);
        var lasted = events[1].getTime() - events[0].getTime();
        var days = (byte) (lasted / (1000 * 60 * 60 * 24));

        // User has left
        return String.format(userHasLeft, days);
    }

    private String join(Integer id) {

        // Check if user has already left or joined
        if (eventManager.hasLeft(id, event)) return youAlreadyLeft;
        if (eventManager.joinEvent(id, event)) return youAlreadyJoined;

        // Join
        if (eventManager.joinEvent(id, event)) return youJoined;
        return errorJoining;
    }

    // Leave
    private String leave(Integer id) {

        // Check if user has already left or joined
        if (eventManager.hasLeft(id, event)) return youAlreadyLeft;
        if (!eventManager.isJoined(id, event)) return youHaveNotJoined;

        // Leave
        if (eventManager.leaveEvent(id, event)) return youLeft;
        return errorLeaving;
    }

    // List
    private String list(Integer channelId) {

        // Get Participants
        HashMap<Integer, Boolean> participants = eventManager.getParticipants(event);
        if (participants == null || participants.isEmpty()) return nobodyJoined;

        // Remove non-followers
        HashSet<TwitchUser> followers = helixHandler.getFollowers(channelId, null);
        participants.keySet().removeIf(id -> !followers.contains(helixHandler.getUser(id)));

        // Check if anyone is left
        if (participants.isEmpty()) return nobodyJoined;

        HashSet<TwitchUser> users = helixHandler.getUsersByID((HashSet<Integer>) participants.keySet());

        // Variables
        StringBuilder stillIn = new StringBuilder();
        StringBuilder alreadyLeft = new StringBuilder();

        for (TwitchUser user : users) {
            if (participants.get(user.getId())) stillIn.append("@").append(user.getName()).append(", ");
            else alreadyLeft.append("@").append(user.getName()).append(", ");
        }

        // Check if anyone is left
        if (stillIn.isEmpty()) return everyoneLeft;
        if (alreadyLeft.isEmpty()) return everyoneStillIn;

        // Format
        stillIn.delete(stillIn.length() - 2, stillIn.length());
        alreadyLeft.delete(alreadyLeft.length() - 2, alreadyLeft.length());
        return String.format(listUsers, stillIn, alreadyLeft);
    }
}