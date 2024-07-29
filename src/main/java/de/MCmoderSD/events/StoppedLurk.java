package de.MCmoderSD.events;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.InteractionHandler;

import de.MCmoderSD.utilities.database.MySQL;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class StoppedLurk {

    // Constructor
    public StoppedLurk(MySQL mySQL, InteractionHandler interactionHandler, TwitchChat chat) {

        // Register Event
        interactionHandler.registerEvent(new Event("$stoppedlurk") {
            @Override
            public void execute(ChannelMessageEvent event) {
                var channelID = getChannelID(event);
                String response;

                HashMap<Timestamp, ArrayList<Integer>> lurker = mySQL.getLurkTime(getUserID(event));
                Timestamp start = lurker.keySet().iterator().next();
                ArrayList<Integer> lurkChannel = lurker.get(start);

                if (channelID == lurkChannel.getFirst()) { // Stop lurking

                    // Remove user from lurk list
                    mySQL.removeLurker(getUserID(event), interactionHandler);

                    // Send message
                    response = tagAuthor(event) + " war " + getLurkTime(start) + " im Lurk!";
                    chat.sendMessage(getChannel(event), response);

                    // Log response
                    mySQL.logResponse(event, getEvent(), getMessage(event), response);

                } else if (!lurkChannel.contains(channelID)) { // Snitch on lurked channel

                    // Add user to traitor list
                    lurkChannel.add(channelID);
                    StringBuilder traitors = new StringBuilder();
                    for (var i = 1; i < lurkChannel.size(); i++) traitors.append(lurkChannel.get(i)).append("\t");
                    mySQL.addTraitor(getUserID(event), traitors.toString());

                    // Send message
                    response = tagAuthor(event) + " ist ein verräter, hab den kek gerade im chat von " + tagChannel(event) + " gesehen!";
                    chat.sendMessage(mySQL.queryChannel(lurkChannel.getFirst()), response);

                    // Log response
                    mySQL.logResponse(event, getEvent(), getMessage(event), response);
                }
            }
        });
    }

    // Calculate the time the user was lurking
    private String getLurkTime(Timestamp startTime) {

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
}