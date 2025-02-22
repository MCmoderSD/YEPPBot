package de.MCmoderSD.commands.blueprints;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchUser;
import de.MCmoderSD.utilities.database.SQL;
import de.MCmoderSD.utilities.database.manager.EventManager;

import java.sql.Timestamp;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;

import static de.MCmoderSD.utilities.other.Format.*;
import static de.MCmoderSD.utilities.other.Util.removeNonFollower;

public abstract class Event {

    // Associations
    private final HelixHandler helixHandler;
    private final EventManager eventManager;

    // Constants
    private final EventManager.Event event;

    // Responses
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
    private final String usersStillIn;
    private final String usersAlreadyLeft;

    // Syntax
    private final String syntax;

    // Constructor
    public Event(BotClient botClient, MessageHandler messageHandler, HelixHandler helixHandler, SQL sql,
                 EventManager.Event event,
                 String explanation,
                 String itsNotMonth,
                 String userNotFound,
                 String userHasNotJoined,
                 String userStillIn,
                 String userHasLeft,
                 String youHaveNotJoined,
                 String youAlreadyJoined,
                 String youAlreadyLeft,
                 String youJoined,
                 String youLeft,
                 String errorJoining,
                 String errorLeaving,
                 String nobodyJoined,
                 String everyoneLeft,
                 String everyoneStillIn,
                 String listUsers,
                 String usersStillIn,
                 String usersAlreadyLeft,
                 String eventName,
                 String... name
    ) {

        // Set Event
        this.event = event;

        // Syntax
        syntax = "Syntax: " + botClient.getPrefix() + event + " [ join | leave | status | list ] <user>";

        // Description
        String description = "Join, leave or check the status of the " + eventName + ". " + syntax;

        // Init Associations
        this.helixHandler = helixHandler;
        this.eventManager = sql.getEventManager();

        // Init Responses
        this.userNotFound = userNotFound;
        this.userHasNotJoined = userHasNotJoined;
        this.userStillIn = userStillIn;
        this.userHasLeft = userHasLeft;
        this.youHaveNotJoined = youHaveNotJoined;
        this.youAlreadyJoined = youAlreadyJoined;
        this.youAlreadyLeft = youAlreadyLeft;
        this.youJoined = youJoined;
        this.youLeft = youLeft;
        this.errorJoining = errorJoining;
        this.errorLeaving = errorLeaving;
        this.nobodyJoined = nobodyJoined;
        this.everyoneLeft = everyoneLeft;
        this.everyoneStillIn = everyoneStillIn;
        this.listUsers = listUsers;
        this.usersStillIn = usersStillIn;
        this.usersAlreadyLeft = usersAlreadyLeft;


        // Register command
        messageHandler.addCommand(new Command(description, name) {
            @Override
            public void execute(TwitchMessageEvent messageEvent, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Check if December
                var currentMonth = Calendar.getInstance().get(Calendar.MONTH);
                if (currentMonth != event.getMonth()) {
                    botClient.respond(messageEvent, getCommand(), itsNotMonth);
                    return;
                }

                // Check args
                if (args.isEmpty()) {
                    botClient.respond(messageEvent, getCommand(), explanation);
                    return;
                }

                // Check if just tagged
                if (args.size() == 1 && args.getFirst().startsWith("@")) {
                    botClient.respond(messageEvent, getCommand(), checkStatus(helixHandler.getUser(removeTag(args.getFirst()))));
                    return;
                }

                // Check Verb
                String verb = args.getFirst().toLowerCase();

                // Check Verb
                if (!Arrays.asList("join", "leave", "fail", "status", "check", "list", "liste").contains(verb)) {
                    botClient.respond(messageEvent, getCommand(), syntax);
                    return;
                }

                // Check Verb
                if (args.size() < 2 && Arrays.asList("status", "check").contains(verb)) {
                    botClient.respond(messageEvent, getCommand(), syntax);
                    return;
                }

                // List
                if (Arrays.asList("list", "liste").contains(verb)) {
                    list(botClient, messageEvent, getCommand(), args);
                    return;
                }

                // Response
                String response = switch (verb) {
                    case "join" -> join(messageEvent.getUserId());
                    case "leave", "fail" -> leave(messageEvent.getUserId());
                    case "status", "check" -> checkStatus(helixHandler.getUser(removeTag(args.get(1))));
                    default -> syntax;
                };

                // Respond
                botClient.respond(messageEvent, getCommand(), response);
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

    // Join
    private String join(Integer id) {

        // Check if user has already left or joined
        if (eventManager.hasLeft(id, event)) return youAlreadyLeft;
        if (eventManager.isJoined(id, event)) return youAlreadyJoined;

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
    private void list(BotClient botClient, TwitchMessageEvent event, String command, ArrayList<String> args) {

        // Get Participants
        HashMap<Integer, Boolean> participants = eventManager.getParticipants(this.event);
        if (participants == null || participants.isEmpty()) {
            botClient.respond(event, command, nobodyJoined);
            return;
        }

        // Remove non-followers
        HashSet<Integer> ids = new HashSet<>(participants.keySet());
        HashSet<Integer> follower = removeNonFollower(event, ids, helixHandler);

        // Null check
        if (follower == null) {
            botClient.respond(event, command, "The bot is not authorized to read the followers of this channel. Type !mod auth to authorize the bot. YEPP");
            return;
        }

        // Check if anyone is left
        if (participants.isEmpty()) {
            botClient.respond(event, command, nobodyJoined);
            return;
        }

        // Variables
        StringBuilder stillIn = new StringBuilder();
        StringBuilder alreadyLeft = new StringBuilder();

        // Get Users
        HashSet<TwitchUser> users = helixHandler.getUsersByID(new HashSet<>(follower));
        for (TwitchUser user : users) {
            if (participants.get(user.getId())) stillIn.append("@").append(user.getName()).append(", ");
            else alreadyLeft.append("@").append(user.getName()).append(", ");
        }

        // Check if anyone is left
        if (stillIn.isEmpty() && !alreadyLeft.isEmpty()) {
            botClient.respond(event, command, everyoneLeft);
            return;
        } else if (!stillIn.isEmpty() && alreadyLeft.isEmpty()) {
            botClient.respond(event, command, everyoneStillIn);
            return;
        }

        // Format
        stillIn.delete(stillIn.length() - 2, stillIn.length());
        alreadyLeft.delete(alreadyLeft.length() - 2, alreadyLeft.length());

        // Check for args
        String arg = args.size() > 1 ? args.get(1) : EMPTY;
        String response = switch (arg) {
            case "in", "still", "fighting" -> String.format(usersStillIn, stillIn);
            case "out", "left", "busted" -> String.format(usersAlreadyLeft, alreadyLeft);
            default -> String.format(listUsers, stillIn, alreadyLeft);
        };

        // Respond
        botClient.respond(event, command, response);
    }
}