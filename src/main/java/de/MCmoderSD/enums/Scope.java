package de.MCmoderSD.enums;

/**
 * The {@code Scope} enum represents different access permissions (scopes) for the Twitch API.
 * Each scope grants specific permissions that are required by applications to access or modify
 * various resources on behalf of a Twitch user.
 * <p>
 * Scopes are organized into categories (such as "Analytics", "Channel", "Moderation", etc.)
 * and define what actions an application can perform or what data it can access.
 * Refer to the <a href="https://dev.twitch.tv/docs/authentication/scopes/#twitch-api-scopes">Twitch API Scopes</a>
 * documentation for details on each scope.
 * </p>
 */
public enum Scope {

    // Analytics
    ANALYTICS_READ_EXTENSIONS("analytics:read:extensions"),
    ANALYTICS_READ_GAMES("analytics:read:games"),
    BITS_READ("bits:read"),

    // Channel
    CHANNEL_BOT("channel:bot"),
    CHANNEL_MANAGE_ADS("channel:manage:ads"),
    CHANNEL_READ_ADS("channel:read:ads"),
    CHANNEL_MANAGE_BROADCAST("channel:manage:broadcast"),
    CHANNEL_READ_CHARITY("channel:read:charity"),
    CHANNEL_EDIT_COMMERCIAL("channel:edit:commercial"),
    CHANNEL_READ_EDITORS("channel:read:editors"),
    CHANNEL_MANAGE_EXTENSIONS("channel:manage:extensions"),
    CHANNEL_READ_GOALS("channel:read:goals"),
    CHANNEL_READ_GUEST_STAR("channel:read:guest_star"),
    CHANNEL_MANAGE_GUEST_STAR("channel:manage:guest_star"),
    CHANNEL_READ_HYPE_TRAIN("channel:read:hype_train"),
    CHANNEL_MANAGE_MODERATORS("channel:manage:moderators"),
    CHANNEL_READ_POLLS("channel:read:polls"),
    CHANNEL_MANAGE_POLLS("channel:manage:polls"),
    CHANNEL_READ_PREDICTIONS("channel:read:predictions"),
    CHANNEL_MANAGE_PREDICTIONS("channel:manage:predictions"),
    CHANNEL_MANAGE_RAIDS("channel:manage:raids"),
    CHANNEL_READ_REDEMPTIONS("channel:read:redemptions"),
    CHANNEL_MANAGE_REDEMPTIONS("channel:manage:redemptions"),
    CHANNEL_MANAGE_SCHEDULE("channel:manage:schedule"),
    CHANNEL_READ_STREAM_KEY("channel:read:stream_key"),
    CHANNEL_READ_SUBSCRIPTIONS("channel:read:subscriptions"),
    CHANNEL_MANAGE_VIDEOS("channel:manage:videos"),
    CHANNEL_READ_VIPS("channel:read:vips"),
    CHANNEL_MANAGE_VIPS("channel:manage:vips"),

    // Clips
    CLIPS_EDIT("clips:edit"),

    // Moderation
    MODERATION_READ("moderation:read"),
    MODERATOR_MANAGE_ANNOUNCEMENTS("moderator:manage:announcements"),
    MODERATOR_MANAGE_AUTOMOD("moderator:manage:automod"),
    MODERATOR_READ_AUTOMOD_SETTINGS("moderator:read:automod_settings"),
    MODERATOR_MANAGE_AUTOMOD_SETTINGS("moderator:manage:automod_settings"),
    MODERATOR_READ_BANNED_USERS("moderator:read:banned_users"),
    MODERATOR_MANAGE_BANNED_USERS("moderator:manage:banned_users"),
    MODERATOR_READ_BLOCKED_TERMS("moderator:read:blocked_terms"),
    MODERATOR_READ_CHAT_MESSAGES("moderator:read:chat_messages"),
    MODERATOR_MANAGE_BLOCKED_TERMS("moderator:manage:blocked_terms"),
    MODERATOR_MANAGE_CHAT_MESSAGES("moderator:manage:chat_messages"),
    MODERATOR_READ_CHAT_SETTINGS("moderator:read:chat_settings"),
    MODERATOR_MANAGE_CHAT_SETTINGS("moderator:manage:chat_settings"),
    MODERATOR_READ_CHATTERS("moderator:read:chatters"),
    MODERATOR_READ_FOLLOWERS("moderator:read:followers"),
    MODERATOR_READ_GUEST_STAR("moderator:read:guest_star"),
    MODERATOR_MANAGE_GUEST_STAR("moderator:manage:guest_star"),
    MODERATOR_READ_MODERATORS("moderator:read:moderators"),
    MODERATOR_READ_SHIELD_MODE("moderator:read:shield_mode"),
    MODERATOR_MANAGE_SHIELD_MODE("moderator:manage:shield_mode"),
    MODERATOR_READ_SHOUTOUTS("moderator:read:shoutouts"),
    MODERATOR_MANAGE_SHOUTOUTS("moderator:manage:shoutouts"),
    MODERATOR_READ_SUSPICIOUS_USERS("moderator:read:suspicious_users"),
    MODERATOR_READ_UNBAN_REQUESTS("moderator:read:unban_requests"),
    MODERATOR_MANAGE_UNBAN_REQUESTS("moderator:manage:unban_requests"),
    MODERATOR_READ_VIPS("moderator:read:vips"),
    MODERATOR_READ_WARNINGS("moderator:manage:warnings"),
    MODERATOR_MANAGE_WARNINGS("moderator:manage:warnings"),

    // User
    USER_BOT("user:bot"),
    USER_EDIT("user:edit"),
    USER_EDIT_BROADCAST("user:edit:broadcast"),
    USER_READ_BLOCKED_USERS("user:read:blocked_users"),
    USER_MANAGE_BLOCKED_USERS("user:manage:blocked_users"),
    USER_READ_BROADCAST("user:read:broadcast"),
    USER_READ_CHAT("user:read:chat"),
    USER_MANAGE_CHAT_COLOR("user:manage:chat_color"),
    USER_READ_EMAIL("user:read:email"),
    USER_READ_EMOTES("user:read:emotes"),
    USER_READ_FOLLOWS("user:read:follows"),
    USER_READ_MODERATED_CHANNELS("user:read:moderated_channels"),
    USER_READ_SUBSCRIPTIONS("user:read:subscriptions"),
    USER_READ_WHISPERS("user:read:whispers"),
    USER_MANAGE_WHISPERS("user:manage:whispers"),
    USER_WRITE_CHAT("user:write:chat"),

    // IRC Chat Scopes
    CHAT_EDIT("chat:edit"),
    CHAT_READ("chat:read"),

    // PubSub-specific Chat Scopes
    WHISPERS_READ("whispers:read");


    // Attributes
    private final String scope;

    // Constructor
    Scope(String scope) {
        this.scope = scope;
    }

    // Methods
    public static Scope getScope(String scope) {
        if (scope == null || scope.isEmpty() || scope.isBlank()) throw new IllegalArgumentException("Scope cannot be empty");
        for (Scope s : Scope.values()) if (s.getScope().equals(scope)) return s;
        return null;
    }

    public static Scope[] getScopes(String scopes) {
        if (scopes == null || scopes.isEmpty() || scopes.isBlank()) throw new IllegalArgumentException("Scopes cannot be empty");
        String[] scopeArray = scopes.split("\\+");
        Scope[] scopeList = new Scope[scopeArray.length];
        for (var i = 0; i < scopeArray.length; i++) scopeList[i] = getScope(scopeArray[i]);
        return scopeList;
    }

    // Getters
    public String getScope() {
        return scope;
    }
}