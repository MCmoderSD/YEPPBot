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

import de.MCmoderSD.enums.Scope;
import de.MCmoderSD.objects.AuthToken;
import de.MCmoderSD.objects.TwitchUser;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.TokenManager;
import de.MCmoderSD.utilities.other.Encryption;
import de.MCmoderSD.utilities.server.Server;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
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
        String accessToken = getAccessToken(botClient.getBotId());

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
        String accessToken = getAccessToken(botClient.getBotId());

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
        String accessToken = getAccessToken(botClient.getBotId());

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
        String accessToken = getAccessToken(botClient.getBotId());

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
        String accessToken = getAccessToken(botClient.getBotId());

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
}