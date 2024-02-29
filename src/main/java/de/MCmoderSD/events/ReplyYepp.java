package de.MCmoderSD.events;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.InteractionHandler;

import static de.MCmoderSD.utilities.Calculate.*;

public class ReplyYepp {

    // Constructor
    public ReplyYepp(InteractionHandler interactionHandler, TwitchChat chat) {

        // Register Event
        interactionHandler.registerEvent(new Event("yeppbot", "yepppbot", "@yeppbot", "@yepppbot") { // Event Name and Alias
            @Override
            public void execute(ChannelMessageEvent event) {
                chat.sendMessage(getChannel(event), tagAuthor(event) + " YEPP");
            }
        });
    }
}