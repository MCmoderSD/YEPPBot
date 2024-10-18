package de.MCmoderSD.objects;

import com.github.twitch4j.helix.domain.ChannelEditor;
import com.github.twitch4j.helix.domain.ChannelVip;
import com.github.twitch4j.helix.domain.Moderator;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.InboundFollow;

@SuppressWarnings("unused")
public class TwitchUser {

    // Attributes
    private final int id;
    private final String name;

    // Constructor
    public TwitchUser(int id, String name) {
        this.id = id;
        this.name = name.toLowerCase();
    }

    // Message Event
    public TwitchUser(TwitchMessageEvent event) {
        this.id = event.getUserId();
        this.name = event.getUser().toLowerCase();
    }

    // User
    public TwitchUser(User user) {
        this.id = Integer.parseInt(user.getId());
        this.name = user.getDisplayName().toLowerCase();
    }

    // Moderator
    public TwitchUser(Moderator moderator) {
        this.id = Integer.parseInt(moderator.getUserId());
        this.name = moderator.getUserName().toLowerCase();
    }

    // Editor
    public TwitchUser(ChannelEditor editor) {
        this.id = Integer.parseInt(editor.getUserId());
        this.name = editor.getUserName().toLowerCase();
    }

    // VIP
    public TwitchUser(ChannelVip vip) {
        this.id = Integer.parseInt(vip.getUserId());
        this.name = vip.getUserName().toLowerCase();
    }

    // Follow
    public TwitchUser(InboundFollow follow) {
        this.id = Integer.parseInt(follow.getUserId());
        this.name = follow.getUserName().toLowerCase();
    }

    // Equals
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TwitchUser user) return user.getId() == id && user.getName().equals(name);
        else return false;
    }

    // Getter
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
