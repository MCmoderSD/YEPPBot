package de.MCmoderSD.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.philippheuer.events4j.core.EventManager;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientHelper;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.domain.ChannelVip;
import com.github.twitch4j.helix.domain.ChannelVipList;
import com.github.twitch4j.helix.domain.ChannelEditor;
import com.github.twitch4j.helix.domain.ChannelEditorList;
import com.github.twitch4j.helix.domain.InboundFollow;
import com.github.twitch4j.helix.domain.InboundFollowers;
import com.github.twitch4j.helix.domain.Moderator;
import com.github.twitch4j.helix.domain.ModeratorList;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.github.twitch4j.helix.domain.HelixPagination;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.Collections;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import de.MCmoderSD.objects.AuthToken;
import de.MCmoderSD.objects.TwitchUser;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.TokenManager;
import de.MCmoderSD.utilities.other.Encryption;
import de.MCmoderSD.utilities.server.Server;

import org.jetbrains.annotations.Nullable;

public class HelixHandler {

    // Constants
    private static final String AUTH_URL = "https://id.twitch.tv/oauth2/authorize";
    private static final String TOKEN_URL = "https://id.twitch.tv/oauth2/token";

    // Credentials
    private final String clientId;
    private final String clientSecret;

    // Associations
    private final BotClient botClient;
    private final Server server;
    private final TokenManager tokenManager;

    // Client
    private final TwitchClient client;
    private final TwitchClientHelper helper;
    private final TwitchChat chat;
    private final TwitchHelix helix;
    private final EventManager eventManager;

    // Utilities
    private final Encryption encryption;

    // Attributes
    private final HashMap<Integer, AuthToken> authTokens;

    // Cache
    private final HashMap<Integer, HashSet<TwitchUser>> moderators;
    private final HashMap<Integer, HashSet<TwitchUser>> vips;
    private final HashMap<Integer, HashSet<TwitchUser>> followers;

    // Constructor
    public HelixHandler(BotClient botClient, MySQL mySQL, Server server) {

        // Set Associations
        this.botClient = botClient;
        tokenManager = mySQL.getTokenManager();
        this.server = server;

        // Set Credentials
        clientId = botClient.getClientId();
        clientSecret = botClient.getClientSecret();

        // Init Client
        client = botClient.getClient();
        helper = botClient.getHelper();
        chat = botClient.getChat();
        helix = botClient.getHelix();
        eventManager = botClient.getEventManager();

        // Init Cache
        moderators = new HashMap<>();
        vips = new HashMap<>();
        followers = new HashMap<>();

        // Set Utilities
        encryption = new Encryption(botClient.getBotToken());

        // Init Attributes
        authTokens = tokenManager.getAuthTokens(this, encryption);

        // Init Server Context
        server.getHttpsServer().createContext("/callback", new CallbackHandler(this));
    }

    // Send request
    private static HttpResponse<String> sendRequest(HttpRequest request) {
        try {
            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to send request: " + e.getMessage());
            return null;
        }
    }

    // Refresh token
    public void refreshToken(AuthToken token) {
        try {

            String oldRefreshToken = token.getRefreshToken();

            // Create body
            String body = String.format(
                    "client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token",
                    clientId,
                    clientSecret,
                    oldRefreshToken
            );

            // Create request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            // Send request
            HttpResponse<String> response = sendRequest(request);

            // Create new token
            AuthToken newToken = new AuthToken(this, Objects.requireNonNull(response).body());

            // Add Credentials
            botClient.addCredential(newToken.getAccessToken());

            // Update tokens in the database
            tokenManager.refreshTokens(oldRefreshToken, newToken, encryption);

            System.out.println("Token refreshed");
            System.out.println("Old Token: " + oldRefreshToken);
            System.out.println("New Token: " + newToken.getRefreshToken());
            System.out.println("New Access Token: " + newToken.getAccessToken());

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to refresh token");
        }
    }

    // Get access token
    private String getAccessToken(int channelId, Scope... scopes) {

        // Variables
        boolean inCache = authTokens.containsKey(channelId);

        // Get access token
        AuthToken token = inCache ? authTokens.get(channelId) : tokenManager.getAuthToken(this, channelId, encryption);
        if (token == null) return null;
        if (!inCache) authTokens.put(channelId, token);


        // Check if token is valid
        boolean isValid = token.hasScope(scopes);

        // Return access token
        if (isValid) return token.getAccessToken();
        else return null;
    }

    // Get authorization URL
    public String getAuthorizationUrl(Scope... scopes) {
        StringBuilder scopeBuilder = new StringBuilder();
        for (Scope scope : Set.of(scopes)) scopeBuilder.append(scope.getScope()).append("+");
        return String.format(
                "%s?client_id=%s&redirect_uri=https://%s:%d/callback&response_type=code&scope=%s",
                AUTH_URL,
                clientId,
                server.getHostname(),
                server.getPort(),
                scopeBuilder.substring(0, scopeBuilder.length() - 1)
        );
    }

    // Check Scope
    public boolean checkScope(int channelId, Scope ... scopes) {
        return authTokens.containsKey(channelId) && authTokens.get(channelId).hasScope(scopes);
    }

    // Get user with ID
    public TwitchUser getUser(Integer id) {

        // Get access token
        String accessToken = getAccessToken(botClient.getBotId(), Scope.USER_READ_EMAIL, Scope.USER_READ_BLOCKED_USERS);

        // Get user ID
        UserList userList = helix.getUsers(accessToken, Collections.singletonList(String.valueOf(id)), null).execute();
        if (userList.getUsers().isEmpty()) return null;
        return new TwitchUser(userList.getUsers().getFirst());
    }

    // Get user with name
    public TwitchUser getUser(String username) {

        // Check Parameters
        if (username == null || username.isEmpty()) throw new IllegalArgumentException("Username cannot be empty");

        // Get access token
        String accessToken = getAccessToken(botClient.getBotId(), Scope.USER_READ_EMAIL, Scope.USER_READ_BLOCKED_USERS);

        // Get user ID
        UserList userList = helix.getUsers(accessToken, null, Collections.singletonList(username)).execute();
        if (userList.getUsers().isEmpty()) return null;
        return new TwitchUser(userList.getUsers().getFirst());
    }

    // Get user with ID
    public HashSet<TwitchUser> getUsersByID(HashSet<Integer> ids) {

        // Check Parameters
        if (ids.isEmpty()) throw new IllegalArgumentException("IDs cannot be empty");

        // Get access token
        String accessToken = getAccessToken(botClient.getBotId(), Scope.USER_READ_EMAIL, Scope.USER_READ_BLOCKED_USERS);

        // Convert to string
        List<String> stringIds = new ArrayList<>();
        ids.forEach(id -> stringIds.add(id.toString()));

        // Get user ID
        UserList userList = helix.getUsers(accessToken, stringIds, null).execute();
        if (userList.getUsers().isEmpty()) return null;

        // Variables
        HashSet<TwitchUser> twitchUsers = new HashSet<>();

        // Add users
        for (User user : userList.getUsers()) twitchUsers.add(new TwitchUser(user));
        return twitchUsers;
    }

    // Get user with ID
    public HashSet<TwitchUser> getUsersByName(HashSet<String> names) {

        // Check Parameters
        if (names.isEmpty()) throw new IllegalArgumentException("Names cannot be empty");

        // Get access token
        String accessToken = getAccessToken(botClient.getBotId(), Scope.USER_READ_EMAIL, Scope.USER_READ_BLOCKED_USERS);

        // Convert to List
        List<String> nameList = new ArrayList<>(names);
        names.addAll(nameList);

        // Variables
        HashSet<TwitchUser> twitchUsers = new HashSet<>();

        // Get user ID
        UserList userList = helix.getUsers(accessToken, null, nameList).execute();
        if (userList.getUsers().isEmpty()) return null;

        // Add users
        for (User user : userList.getUsers()) twitchUsers.add(new TwitchUser(user));
        return twitchUsers;
    }

    // Get user with ID and name
    public TwitchUser getUser(Integer id, String username) {

        // Get access token
        String accessToken = getAccessToken(botClient.getBotId(), Scope.USER_READ_EMAIL, Scope.USER_READ_BLOCKED_USERS);

        // Get user ID
        UserList userList = helix.getUsers(accessToken, Collections.singletonList(String.valueOf(id)), Collections.singletonList(username)).execute();
        if (userList.getUsers().isEmpty()) return null;
        return new TwitchUser(userList.getUsers().getFirst());
    }

    // Get moderators
    public HashSet<TwitchUser> getModerators(Integer channelId, @Nullable String cursor) {

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.MODERATION_READ);

        // Get moderators
        ModeratorList moderatorList = helix.getModerators(accessToken, channelId.toString(), null, cursor, 100).execute();

        // Variables
        HashSet<TwitchUser> twitchUsers = new HashSet<>();

        // Add moderators
        for (Moderator moderator : moderatorList.getModerators()) twitchUsers.add(new TwitchUser(moderator));


        // Check if cache is up to date
        boolean cacheUpToDate = cursor == null;
        HashSet<TwitchUser> cache = moderators.get(channelId);
        if (cacheUpToDate && cache != null && TwitchUser.containsTwitchUsers(cache, twitchUsers)) return cache;

        // Check if there are more moderators
        HelixPagination pagination = moderatorList.getPagination();
        String nextCursor = pagination != null ? pagination.getCursor() : null;
        if (nextCursor != null) twitchUsers.addAll(getModerators(channelId, nextCursor));

        // Update cache
        moderators.replace(channelId, twitchUsers);

        // Return moderators
        return twitchUsers;
    }

    // Check if user is moderator
    public boolean isModerator(Integer channelId, Integer userId) {

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.MODERATION_READ);

        // Get moderators
        ModeratorList moderatorList = helix.getModerators(accessToken, channelId.toString(), Collections.singletonList(userId.toString()), null, 1).execute();

        // Check if user is moderator
        return !moderatorList.getModerators().isEmpty();
    }

    // Get editors
    public HashSet<TwitchUser> getEditors(Integer channelId) {

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.CHANNEL_READ_EDITORS);

        // Get editors
        ChannelEditorList editorList = helix.getChannelEditors(accessToken, channelId.toString()).execute();

        // Variables
        HashSet<TwitchUser> twitchUsers = new HashSet<>();

        // Add editors
        for (ChannelEditor editor : editorList.getEditors()) twitchUsers.add(new TwitchUser(editor));
        return twitchUsers;
    }

    // Get VIPs
    public HashSet<TwitchUser> getVIPs(Integer channelId, @Nullable String cursor) {

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.CHANNEL_READ_VIPS);

        // Get VIPs
        ChannelVipList vipList = helix.getChannelVips(accessToken, channelId.toString(), null, 100, cursor).execute();

        // Variables
        HashSet<TwitchUser> twitchUsers = new HashSet<>();

        // Add VIPs
        for (ChannelVip vip : vipList.getData()) twitchUsers.add(new TwitchUser(vip));


        // Check if cache is up to date
        boolean cacheUpToDate = cursor == null;
        HashSet<TwitchUser> cache = vips.get(channelId);
        if (cacheUpToDate && cache != null && TwitchUser.containsTwitchUsers(cache, twitchUsers)) return cache;

        // Check if there are more VIPs
        HelixPagination pagination = vipList.getPagination();
        String nextCursor = pagination != null ? pagination.getCursor() : null;
        if (nextCursor != null) twitchUsers.addAll(getVIPs(channelId, nextCursor));

        // Update cache
        vips.replace(channelId, twitchUsers);

        // Return VIPs
        return twitchUsers;
    }

    // Check if user is VIP
    public boolean isVIP(Integer channelId, Integer userId) {

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.CHANNEL_READ_VIPS);

        // Get moderators
        ChannelVipList vipList = helix.getChannelVips(accessToken, channelId.toString(), Collections.singletonList(channelId.toString()), 1, null).execute();

        // Check if user is moderator
        return !vipList.getData().isEmpty();
    }


    public HashSet<TwitchUser> getFollowers(Integer channelId, @Nullable String cursor) {

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.MODERATOR_READ_FOLLOWERS);

        // Get followers
        InboundFollowers inboundFollowers = helix.getChannelFollowers(accessToken, channelId.toString(), null, 100, cursor).execute();

        // Variables
        HashSet<TwitchUser> twitchUsers = new HashSet<>();

        // Add followers
        for (InboundFollow follow : Objects.requireNonNull(inboundFollowers.getFollows())) twitchUsers.add(new TwitchUser(follow));

        // Check if cache is up to date
        boolean cacheUpToDate = cursor == null;
        HashSet<TwitchUser> cache = followers.get(channelId);
        if (cacheUpToDate && cache != null && TwitchUser.containsTwitchUsers(cache, twitchUsers)) return cache;

        // Check if there are more followers
        HelixPagination pagination = inboundFollowers.getPagination();
        String nextCursor = pagination != null ? pagination.getCursor() : null;
        if (nextCursor != null) twitchUsers.addAll(getFollowers(channelId, inboundFollowers.getPagination().getCursor()));

        // Update cache
        followers.remove(channelId);
        followers.put(channelId, twitchUsers);

        // Return followers
        return twitchUsers;
    }

    // Check if user is follower
    public boolean isFollower(Integer channelId, Integer userId) {

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.MODERATOR_READ_FOLLOWERS);

        // Get followers
        InboundFollowers inboundFollowers = helix.getChannelFollowers(accessToken, channelId.toString(), userId.toString(), 1, null).execute();

        // Check if user is follower
        return !Objects.requireNonNull(inboundFollowers.getFollows()).isEmpty();
    }

    public TwitchHelix getHelix() {
        return helix;
    }

    // Callback handler
    private class CallbackHandler implements HttpHandler {

        // Attributes
        private final HelixHandler helixHandler;

        // Constructor
        public CallbackHandler(HelixHandler helixHandler) {
            this.helixHandler = helixHandler;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // Extract code and scopes
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("code=")) {

                // Create body
                String body = String.format(
                        "client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&redirect_uri=https://%s:%d/callback",
                        clientId,
                        clientSecret,
                        query.split("code=")[1].split("&")[0],
                        server.getHostname(),
                        server.getPort()
                );

                // Create request
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(TOKEN_URL))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                // Send request
                HttpResponse<String> response = sendRequest(request);

                // Create new token
                AuthToken token = new AuthToken(helixHandler, Objects.requireNonNull(response).body());

                // Add Credentials
                botClient.addCredential(token.getAccessToken());

                // Update tokens in the database
                tokenManager.addToken(getUser(token.getId()).getName(), token, encryption);
            }

            // Send response
            String response = "You can close this window now.";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }
    }

    // Enums
    public enum Scope {

        // Scopes
        ANALYTICS_READ_EXTENSIONS("analytics:read:extensions", "View analytics data for your extensions."),
        ANALYTICS_READ_GAMES("analytics:read:games", "View analytics data for your games."),
        BITS_READ("bits:read", "View bits information for your channel."),
        CHANNEL_BOT("channel:bot", "Allows the client's bot users access to a channel."),
        USER_EDIT("user:edit", "Manage a user object."),
        USER_READ_EMAIL("user:read:email", "Read authorized user's email address."),
        CLIPS_EDIT("clips:edit", "Create and edit clips as a specific user."),
        USER_EDIT_BROADCAST("user:edit:broadcast", "Edit your channel's broadcast configuration, including extension configuration."),
        USER_READ_BROADCAST("user:read:broadcast", "View your broadcasting configuration, including extension configurations."),
        CHAT_READ("chat:read", "View live Stream Chat and Rooms messages."),
        CHAT_EDIT("chat:edit", "Send live Stream Chat and Rooms messages."),
        CHANNEL_MODERATE("channel:moderate", "Perform moderation actions in a channel."),
        CHANNEL_READ_SUBSCRIPTIONS("channel:read:subscriptions", "Get a list of all subscribers to your channel and check if a user is subscribed to your channel."),
        WHISPERS_READ("whispers:read", "View your whisper messages."),
        WHISPERS_EDIT("whispers:edit", "Send whisper messages."),
        MODERATION_READ("moderation:read", "View your channel's moderation data including Moderators, Bans, Timeouts and Automod settings."),
        CHANNEL_READ_REDEMPTIONS("channel:read:redemptions", "View your channel points custom reward redemptions."),
        CHANNEL_EDIT_COMMERCIAL("channel:edit:commercial", "Run commercials on a channel."),
        CHANNEL_READ_HYPE_TRAIN("channel:read:hype_train", "View hype train data for a given channel."),
        CHANNEL_READ_STREAM_KEY("channel:read:stream_key", "Read authorized user's stream key."),
        CHANNEL_MANAGE_EXTENSIONS("channel:manage:extensions", "Manage your channel's extension configuration, including activating extensions."),
        CHANNEL_MANAGE_BROADCAST("channel:manage:broadcast", "Manage your channel's broadcast configuration, including updating channel configuration and managing stream markers and stream tags."),
        USER_EDIT_FOLLOWS("user:edit:follows", "Edit your follows."),
        CHANNEL_MANAGE_REDEMPTIONS("channel:manage:redemptions", "Manage Channel Points custom rewards and their redemptions on a channel."),
        CHANNEL_READ_EDITORS("channel:read:editors", "View a list of users with the editor role for a channel."),
        CHANNEL_MANAGE_VIDEOS("channel:manage:videos", "Manage a channel's videos, including deleting videos."),
        USER_READ_BLOCKED_USERS("user:read:blocked_users", "View the block list of a user."),
        USER_MANAGE_BLOCKED_USERS("user:manage:blocked_users", "Manage the block list of a user."),
        USER_READ_SUBSCRIPTIONS("user:read:subscriptions", "Get the details of your subscription to a channel."),
        USER_READ_FOLLOWS("user:read:follows", "View the list of channels a user follows."),
        CHANNEL_MANAGE_POLLS("channel:manage:polls", "Manage a channel's polls."),
        CHANNEL_MANAGE_PREDICTIONS("channel:manage:predictions", "Manage of channel's Channel Points Predictions"),
        CHANNEL_READ_POLLS("channel:read:polls", "View a channel's polls."),
        CHANNEL_READ_PREDICTIONS("channel:read:predictions", "View a channel's Channel Points Predictions."),
        MODERATOR_MANAGE_AUTOMOD("moderator:manage:automod", "Manage messages held for review by AutoMod in channels where you are a moderator."),
        CHANNEL_MANAGE_SCHEDULE("channel:manage:schedule", "Manage a channel's stream schedule."),
        CHANNEL_READ_GOALS("channel:read:goals", "View Creator Goals for a channel."),
        MODERATOR_READ_AUTOMOD_SETTINGS("moderator:read:automod_settings", "Read AutoMod settings in channels where you have the moderator role"),
        MODERATOR_MANAGE_AUTOMOD_SETTINGS("moderator:manage:automod_settings", "Manage AutoMod settings in channels where you have the moderator role"),
        MODERATOR_MANAGE_BANNED_USERS("moderator:manage:banned_users", "Ban or unban users in channels where you have the moderator role"),
        MODERATOR_READ_BLOCKED_TERMS("moderator:read:blocked_terms", "Read non-private blocked terms in channels where you have the moderator role"),
        MODERATOR_MANAGE_BLOCKED_TERMS("moderator:manage:blocked_terms", "Manage non-private blocked terms in channels where you have the moderator role"),
        MODERATOR_READ_CHAT_SETTINGS("moderator:read:chat_settings", "Read chat settings in channels where you have the moderator role"),
        MODERATOR_MANAGE_CHAT_SETTINGS("moderator:manage:chat_settings", "Manage chat settings in channels where you have the moderator role"),
        CHANNEL_MANAGE_RAIDS("channel:manage:raids", "Manage raids on your channel"),
        MODERATOR_MANAGE_ANNOUNCEMENTS("moderator:manage:announcements", "Send announcements in channels where you have the moderator role."),
        MODERATOR_MANAGE_CHAT_MESSAGES("moderator:manage:chat_messages", "Delete chat messages in channels where you have the moderator role"),
        USER_MANAGE_CHAT_COLOR("user:manage:chat_color", "Update the color used for the user's name in chat."),
        CHANNEL_MANAGE_MODERATORS("channel:manage:moderators", "Add or remove the moderator role from users in your channel."),
        CHANNEL_READ_VIPS("channel:read:vips", "Read the list of VIPs in your channel."),
        CHANNEL_MANAGE_VIPS("channel:manage:vips", "Add or remove the VIP role from users in your channel."),
        USER_MANAGE_WHISPERS("user:manage:whispers", "Read whispers that you send and receive, and send whispers on your behalf."),
        CHANNEL_READ_CHARITY("channel:read:charity", "Read charity campaign details and user donations on your channel."),
        MODERATOR_READ_CHATTERS("moderator:read:chatters", "Read the list of chatters in channels where you have the moderator role"),
        MODERATOR_READ_SHIELD_MODE("moderator:read:shield_mode", "Get information about Shield Mode and Shield Mode settings in channels where you have the moderator role"),
        MODERATOR_MANAGE_SHIELD_MODE("moderator:manage:shield_mode", "Manage Shield Mode and Shield Mode settings in channels where you have the moderator role"),
        MODERATOR_READ_SHOUTOUTS("moderator:read:shoutouts", "View a broadcaster's shoutouts."),
        MODERATOR_MANAGE_SHOUTOUTS("moderator:manage:shoutouts", "Manage a broadcaster's shoutouts."),
        MODERATOR_READ_FOLLOWERS("moderator:read:followers", "Read the list of followers in channels where you are a moderator."),
        CHANNEL_READ_GUEST_STAR("channel:read:guest_star", "Read Guest Star details for your channel."),
        CHANNEL_MANAGE_GUEST_STAR("channel:manage:guest_star", "Manage Guest Star for your channel."),
        MODERATOR_READ_GUEST_STAR("moderator:read:guest_star", "Read Guest Star details for channels where you are a Guest Star moderator."),
        MODERATOR_MANAGE_GUEST_STAR("moderator:manage:guest_star", "Manage Guest Star for channels where you are a Guest Star moderator."),
        USER_BOT("user:bot", "Allows client's bot to act as this user."),
        USER_READ_CHAT("user:read:chat", "View live stream chat and room messages."),
        CHANNEL_MANAGE_ADS("channel:manage:ads", "Manage ads schedule on a channel."),
        CHANNEL_READ_ADS("channel:read:ads", "Read the ads schedule and details on your channel."),
        USER_READ_MODERATED_CHANNELS("user:read:moderated_channels", "Read the list of channels you have moderator privileges in."),
        USER_WRITE_CHAT("user:write:chat", "Send live stream chat messages using Send Chat Message API."),
        USER_READ_EMOTES("user:read:emotes", "View emotes available to a user."),
        MODERATOR_READ_UNBAN_REQUESTS("moderator:read:unban_requests", "View a broadcaster’s unban requests."),
        MODERATOR_MANAGE_UNBAN_REQUESTS("moderator:manage:unban_requests", "Manage a broadcaster’s unban requests."),
        MODERATOR_READ_SUSPICIOUS_USERS("moderator:read:suspicious_users", "Read chat messages from suspicious users and see users flagged as suspicious in channels where you have the moderator role."),
        MODERATOR_MANAGE_WARNINGS("moderator:manage:warnings", "Warn users in channels where you have the moderator role.");

        // Attributes
        private final String scope;
        private final String description;

        // Constructor
        Scope(String scope, String description) {

            // Set Attributes
            this.scope = scope;
            this.description = description;
        }

        // Methods
        public static Scope getScope(String scope) {
            if (scope == null || scope.isEmpty() || scope.isBlank()) throw new IllegalArgumentException("Scope cannot be empty");
            for (Scope s : Scope.values()) if (s.getScope().equals(scope)) return s;
            return null;
        }

        public static Scope[] getScopes(String scopes) {
            if (scopes == null || scopes.isEmpty() || scopes.isBlank())
                throw new IllegalArgumentException("Scopes cannot be empty");
            String[] scopeArray = scopes.split("\\+");
            Scope[] scopeList = new Scope[scopeArray.length];
            for (var i = 0; i < scopeArray.length; i++) scopeList[i] = getScope(scopeArray[i]);
            return scopeList;
        }

        // Getters
        public String getScope() {
            return scope;
        }

        public String getDescription() {
            return description;
        }
    }
}