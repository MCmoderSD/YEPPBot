package de.MCmoderSD.events;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.InteractionHandler;

import de.MCmoderSD.utilities.database.MySQL;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class ReplyYepp {

    // Constructor
    public ReplyYepp(MySQL mySQL, InteractionHandler interactionHandler, TwitchChat chat) {

        // About
        String[] name = {"yeppbot", "yepppbot", "@yeppbot", "@yepppbot"};

        // Register Event
        interactionHandler.registerEvent(new Event(name) {
            @Override
            public void execute(ChannelMessageEvent event) {
                String response = tagAuthor(event) + " YEPP";
                chat.sendMessage(getChannel(event), response);
                mySQL.logResponse(event, getEvent(), getMessage(event), response);
            }
        });
    }
}