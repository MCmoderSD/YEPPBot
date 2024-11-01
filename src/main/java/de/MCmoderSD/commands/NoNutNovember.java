package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchUser;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.EventManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import static de.MCmoderSD.utilities.other.Format.*;

public class NoNutNovember {

    // Associations
    private final HelixHandler helixHandler;
    private final EventManager eventManager;

    // Constants
    private final EventManager.Event event = EventManager.Event.NNN;

    // Responses
    private final String explanation;
    private final String itsNotNovember;
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
    public NoNutNovember(BotClient botClient, MessageHandler messageHandler, MySQL mySQL, HelixHandler helixHandler) {

        // Syntax
        syntax = "Syntax: " + botClient.getPrefix() + "NNN [ join | leave | status | list ] <user>";

        // About
        String[] name = {"nonutnovember", "nnn", "no-nut-november"};
        String description = "Join, leave or check the status of the No-Nut-November. " + syntax;

        // Init Associations
        this.helixHandler = helixHandler;
        this.eventManager = mySQL.getEventManager();

        // Init Responses
        explanation = "It's Not-Nut-November YEPP Type !nnn join in Chat to participate, type !nnn leave if you have sinned. Check the Status of others with !nnn status @user YEPP Happy November YEPP";
        itsNotNovember = "It's not November, you can nut all you want! YEPP";
        userNotFound = "User not found.";
        userHasNotJoined = "%s hasn't joined No-Nut-November. YEPP";
        userStillIn = "%s is still in No-Nut-November. YEPP";
        userHasLeft = "Bro already busted a nut on day %d YEPP";

        youHaveNotJoined = "You haven't even joined No-Nut-November. YEPP";
        youAlreadyJoined = "You already joined No-Nut-November. YEPP";
        youAlreadyLeft = "You already failed No-Nut-November - Try again next year! YEPP";
        youJoined = "You joined No-Nut-November. YEPP";
        youLeft = "You failed No-Nut-November - Try again next year! YEPP";
        errorJoining = "Error: Couldn't join No-Nut-November. YEPP";
        errorLeaving = "Error: Couldn't leave No-Nut-November. YEPP";

        nobodyJoined = "No one has joined No-Nut-November. YEPP";
        everyoneLeft = "Everyone has already busted a nut. YEPP";
        everyoneStillIn = "Everyone is still in No-Nut-November. YEPP";
        listUsers = "%s are still fighting, but %s already busted a nut. YEPP";

        // Register command
        messageHandler.addCommand(new Command(description, name) {
            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Check if November
                if (Calendar.getInstance().get(Calendar.MONTH) != Calendar.NOVEMBER) {
                    botClient.respond(event, getCommand(), itsNotNovember);
                    return;
                }

                // Check args
                if (args.isEmpty()) {
                    botClient.respond(event, getCommand(), explanation);
                    return;
                }

                // Check if just tagged
                if (args.size() == 1 && args.getFirst().startsWith("@")) {
                    botClient.respond(event, getCommand(), checkStatus(helixHandler.getUser(removeTag(args.getFirst()))));
                    return;
                }

                // Check Verb
                String verb = args.getFirst().toLowerCase();

                // Check Verb
                if (!Arrays.asList("join", "leave", "", "status", "check", "list").contains(verb)) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                // Check Verb
                if (args.size() < 2 && Arrays.asList("status", "check").contains(verb)) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                // Response
                String response = switch (verb) {
                    case "join" -> join(event.getUserId());
                    case "leave", "bust", "fail" -> leave(event.getUserId());
                    case "status", "check" -> checkStatus(helixHandler.getUser(removeTag(args.get(1))));
                    case "list", "liste" -> list(event.getChannelId());
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
    private String list(Integer channelId) {

        // Get Participants
        HashMap<Integer, Boolean> participants = eventManager.getParticipants(event);
        if (participants == null || participants.isEmpty()) return nobodyJoined;

        // Remove non-followers
        participants.keySet().removeIf(id -> ! helixHandler.getFollowers(channelId, null).contains(helixHandler.getUser(id)));

        // Check if anyone is left
        if (participants.isEmpty()) return nobodyJoined;

        // Variables
        StringBuilder stillIn = new StringBuilder();
        StringBuilder alreadyLeft = new StringBuilder();

        for (TwitchUser user : helixHandler.getUsersByID(new HashSet<>(participants.keySet()))) {
            if (participants.get(user.getId())) stillIn.append("@").append(user.getName()).append(", ");
            else alreadyLeft.append("@").append(user.getName()).append(", ");
        }

        // Check if anyone is left
        if (stillIn.isEmpty() && !alreadyLeft.isEmpty()) return everyoneLeft;
        if (!stillIn.isEmpty() && alreadyLeft.isEmpty()) return everyoneStillIn;

        // Format
        stillIn.delete(stillIn.length() - 2, stillIn.length());
        alreadyLeft.delete(alreadyLeft.length() - 2, alreadyLeft.length());
        return String.format(listUsers, stillIn, alreadyLeft);
    }
}