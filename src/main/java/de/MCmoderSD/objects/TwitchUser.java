package de.MCmoderSD.objects;

import com.github.twitch4j.helix.domain.Moderator;
import com.github.twitch4j.helix.domain.User;

@SuppressWarnings("unused")
public class TwitchUser {

    // Attributes
    private final int id;
    private final String name;

    // Constructor
    public TwitchUser(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public TwitchUser(TwitchMessageEvent event) {
        this.id = event.getUserId();
        this.name = event.getUser();
    }

    // User
    public TwitchUser(User user) {
        this.id = Integer.parseInt(user.getId());
        this.name = user.getLogin();
    }

    // Moderator
    public TwitchUser(Moderator moderator) {
        this.id = Integer.parseInt(moderator.getUserId());
        this.name = moderator.getUserName();
    }

    // Getter
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
