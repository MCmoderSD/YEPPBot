package de.MCmoderSD.objects;

import com.github.twitch4j.helix.domain.ChannelEditor;
import com.github.twitch4j.helix.domain.ChannelVip;
import com.github.twitch4j.helix.domain.Moderator;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.InboundFollow;

import java.util.HashSet;
import java.util.Objects;

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public class TwitchUser {

    // Attributes
    private final Integer id;
    private final String name;

    // Constructor
    public TwitchUser(Integer id, String name) {
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
        if (obj instanceof TwitchUser user) return Objects.equals(user.getId(), id) && user.getName().equals(name);
        else return false;
    }

    // Contains
    public static boolean containsTwitchUser(HashSet<TwitchUser> list, TwitchUser user) {
        for (TwitchUser u : list) if (u.equals(user)) return true;
        return false;
    }

    public static boolean containsTwitchUsers(HashSet<TwitchUser> list, HashSet<TwitchUser> users) {
        for (TwitchUser user : users) if (!containsTwitchUser(list, user)) return false;
        return true;
    }

    public static boolean containsTwitchUser(HashSet<TwitchUser> list, Integer id) {
        for (TwitchUser user : list) if (Objects.equals(user.getId(), id)) return true;
        return false;
    }

    public static boolean containsTwitchUser(HashSet<TwitchUser> list, String name) {
        for (TwitchUser user : list) if (user.getName().equalsIgnoreCase(name)) return true;
        return false;
    }

    // Getter
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
