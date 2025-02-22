package de.MCmoderSD.core;

import com.fasterxml.jackson.core.JsonProcessingException;

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
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.Collections;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import de.MCmoderSD.encryption.Encryption;
import de.MCmoderSD.enums.Scope;
import de.MCmoderSD.objects.AuthToken;
import de.MCmoderSD.objects.TwitchUser;
import de.MCmoderSD.server.Server;
import de.MCmoderSD.utilities.database.SQL;
import de.MCmoderSD.utilities.database.manager.TokenManager;
import de.MCmoderSD.utilities.other.Format;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
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
    private final TwitchHelix helix;

    // Utilities
    private final Encryption encryption;

    // Attributes
    private final HashMap<Integer, AuthToken> authTokens;

    // Cache
    private final HashMap<Integer, HashSet<TwitchUser>> moderators;
    private final HashMap<Integer, HashSet<TwitchUser>> vips;
    private final HashMap<Integer, HashSet<TwitchUser>> followers;

    // Constructor
    public HelixHandler(BotClient botClient, SQL sql, Server server) {

        // Set Associations
        this.botClient = botClient;
        helix = botClient.getHelix();
        tokenManager = sql.getTokenManager();
        this.server = server;

        // Set Credentials
        clientId = botClient.getClientId();
        clientSecret = botClient.getClientSecret();

        // Init Cache
        moderators = new HashMap<>();
        vips = new HashMap<>();
        followers = new HashMap<>();

        // Init Encryption
        encryption = new Encryption(botClient.getBotToken());

        // Load Tokens
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

    private static HttpRequest createRequest(String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(HelixHandler.TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    // Parse token
    private boolean parseToken(HttpResponse<String> response, @Nullable String oldRefreshToken) throws JsonProcessingException {

        // Null check
        if (response == null || response.body() == null || response.body().isEmpty() || response.body().isBlank()) {
            System.err.println("Failed to get response");
            return false;
        }

        // Create new token
        AuthToken token = new AuthToken(this, response.body());

        // Null check
        if (token.getAccessToken() == null) {
            System.err.println("Failed to get access token");
            return false;
        }

        // Add token
        authTokens.replace(token.getId(), token);

        // Add Credentials
        botClient.addCredential(token.getAccessToken());

        // Update tokens in the database
        if (oldRefreshToken == null) tokenManager.addToken(getUser(token.getId()).getName(), token, encryption);
        else tokenManager.refreshTokens(oldRefreshToken, token, encryption);

        // Return
        return true;
    }

    // Get access token
    private String getAccessToken(int channelId, Scope... scopes) {

        // Get token
        AuthToken token = authTokens.containsKey(channelId) ? authTokens.get(channelId) : tokenManager.getAuthToken(this, channelId, encryption);

        // Null check
        if (token == null) {
            System.err.println("Failed to get access token");
            return null;
        }

        // Return if token has scope
        if (token.hasScope(scopes)) return token.getAccessToken();

        // Error message
        System.err.println("Token does not have the required scope");
        return null;
    }


    // Refresh token
    public void refreshToken(AuthToken token) {
        try {

            // Variables
            String oldRefreshToken = token.getRefreshToken();

            // Create body
            String body = String.format(
                    "client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token",
                    clientId,
                    clientSecret,
                    oldRefreshToken
            );

            // Request token
            boolean success = parseToken(sendRequest(createRequest(body)), oldRefreshToken);

            // Error message
            if (!success) System.err.println("Failed to refresh token");

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to refresh token");
        }
    }

    // Send shoutout
    public boolean sendShoutout(Integer channelId, TwitchUser user) {

        // Check Parameters
        if (channelId == null || channelId < 1) throw new IllegalArgumentException("Channel ID cannot be null or less than 1");
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        // Variables
        String channel = channelId.toString();
        String raider = user.getId().toString();

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.MODERATOR_MANAGE_SHOUTOUTS);

        // Null check
        if (accessToken == null || accessToken.isBlank()) {
            System.err.println("Failed to get access token");
            return false;
        }

        // Send shoutout
        helix.sendShoutout(accessToken, channel, raider, channel).execute();

        // Return
        return true;
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
    public boolean checkScope(Integer channelId, Scope ... scopes) {
        if (channelId == null || channelId < 1) throw new IllegalArgumentException("Channel ID cannot be null or less than 1");
        if (scopes == null || scopes.length == 0) throw new IllegalArgumentException("Scopes cannot be null or empty");
        return authTokens.containsKey(channelId) && authTokens.get(channelId).hasScope(scopes);
    }

    // Get user with ID
    public TwitchUser getUser(Integer id) {

        // Check Parameters
        if (id == null || id < 1) throw new IllegalArgumentException("ID cannot be null or less than 1");

        // Get user ID
        UserList userList = helix.getUsers(null, Collections.singletonList(String.valueOf(id)), null).execute();

        // Null check
        if (userList == null || userList.getUsers() == null || userList.getUsers().isEmpty()) {
            System.err.println("Failed to get user with ID: " + id);
            return null;
        }

        // Return user
        return new TwitchUser(userList.getUsers().getFirst());
    }

    // Get user with name
    public TwitchUser getUser(String username) {

        // Check Parameters
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username cannot be empty");

        // Get user ID
        UserList userList = helix.getUsers(null, null, Collections.singletonList(username)).execute();

        // Null check
        if (userList == null || userList.getUsers() == null || userList.getUsers().isEmpty()) {
            System.err.println("Failed to get user with name: " + username);
            return null;
        }

        // Return user
        return new TwitchUser(userList.getUsers().getFirst());
    }

    // Get user with ID and name
    public TwitchUser getUser(Integer id, String username) {

        // Check Parameters
        if (id == null || id < 1) throw new IllegalArgumentException("ID cannot be null or less than 1");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username cannot be empty");

        // Get user ID
        UserList userList = helix.getUsers(null, Collections.singletonList(String.valueOf(id)), Collections.singletonList(username)).execute();

        // Null check
        if (userList == null || userList.getUsers() == null || userList.getUsers().isEmpty()) {
            System.err.println("Failed to get user with ID: " + id + " and name: " + username);
            return null;
        }

        // Return user
        return new TwitchUser(userList.getUsers().getFirst());
    }

    // Get user with ID
    public HashSet<TwitchUser> getUsersByID(HashSet<Integer> ids) {

        // Variables
        HashSet<TwitchUser> twitchUsers = new HashSet<>();
        ArrayList<String> stringIds = Format.formatIdsToString(ids);

        // Check Parameters
        if (stringIds.isEmpty()) throw new IllegalArgumentException("IDs cannot be empty");

        // Check Limits
        if (stringIds.size() > 100) {

            // Create Batches
            HashSet<HashSet<Integer>> batches = new HashSet<>();
            while (stringIds.size() > 100) {
                HashSet<Integer> batch = new HashSet<>();
                while (batch.size() < 100) {
                    String id = stringIds.getFirst();
                    batch.add(Integer.parseInt(id));
                    stringIds.remove(id);
                }
                batches.add(batch);
            }

            // Loop through batches
            for (HashSet<Integer> batch : batches) twitchUsers.addAll(getUsersByID(batch));
        }

        // Get user ID
        UserList userList = helix.getUsers(null, stringIds, null).execute();

        // Null check
        if (userList == null || userList.getUsers() == null || userList.getUsers().isEmpty()) {
            System.err.println("Failed to get users with IDs: " + stringIds);
            return null;
        }

        // Add users
        for (User user : userList.getUsers()) twitchUsers.add(new TwitchUser(user));
        return twitchUsers;
    }

    // Get user with ID
    public HashSet<TwitchUser> getUsersByName(HashSet<String> names) {

        // Check Parameters
        if (names.isEmpty()) throw new IllegalArgumentException("Names cannot be empty");

        // Variables
        HashSet<TwitchUser> twitchUsers = new HashSet<>();

        // Check Limits
        if (names.size() > 100) {

            // Create Batches
            HashSet<HashSet<String>> batches = new HashSet<>();
            while (names.size() > 100) {
                HashSet<String> batch = new HashSet<>();
                while (batch.size() < 100) {
                    String name = names.iterator().next();
                    batch.add(name);
                    names.remove(name);
                }
                batches.add(batch);
            }

            // Loop through batches
            for (HashSet<String> batch : batches) twitchUsers.addAll(getUsersByName(batch));
        }

        // Get user ID
        UserList userList = helix.getUsers(null, null, new ArrayList<>(names)).execute();

        // Null check
        if (userList == null || userList.getUsers() == null || userList.getUsers().isEmpty()) {
            System.err.println("Failed to get users with names: " + names);
            return null;
        }

        // Add users
        for (User user : userList.getUsers()) twitchUsers.add(new TwitchUser(user));
        return twitchUsers;
    }

    // Get moderators
    public HashSet<TwitchUser> getModerators(Integer channelId, @Nullable String cursor) {

        // Check Parameters
        if (channelId == null || channelId < 1) throw new IllegalArgumentException("Channel ID cannot be null or less than 1");

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.MODERATION_READ);

        // Null check
        if (accessToken == null || accessToken.isBlank()) {
            System.err.println("Failed to get access token");
            return null;
        }

        // Get moderators
        ModeratorList moderatorList = helix.getModerators(accessToken, channelId.toString(), null, cursor, 100).execute();

        // Null check
        if (moderatorList == null || moderatorList.getModerators() == null || moderatorList.getModerators().isEmpty()) {
            System.err.println("Failed to get moderators");
            return null;
        }

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

        // Check Parameters
        if (channelId == null || channelId < 1) throw new IllegalArgumentException("Channel ID cannot be null or less than 1");
        if (userId == null || userId < 1) throw new IllegalArgumentException("User ID cannot be null or less than 1");

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.MODERATION_READ);

        // Null check
        if (accessToken == null || accessToken.isBlank()) {
            System.err.println("Failed to get access token");
            return false;
        }

        // Get moderators
        ModeratorList moderatorList = helix.getModerators(accessToken, channelId.toString(), Collections.singletonList(userId.toString()), null, 1).execute();

        // Check if user is moderator
        return !moderatorList.getModerators().isEmpty();
    }

    // Get editors
    public HashSet<TwitchUser> getEditors(Integer channelId) {

        // Check Parameters
        if (channelId == null || channelId < 1) throw new IllegalArgumentException("Channel ID cannot be null or less than 1");

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.CHANNEL_READ_EDITORS);

        // Null check
        if (accessToken == null || accessToken.isBlank()) {
            System.err.println("Failed to get access token");
            return null;
        }

        // Get editors
        ChannelEditorList editorList = helix.getChannelEditors(accessToken, channelId.toString()).execute();

        // Null check
        if (editorList == null || editorList.getEditors() == null || editorList.getEditors().isEmpty()) {
            System.err.println("Failed to get editors");
            return null;
        }

        // Variables
        HashSet<TwitchUser> twitchUsers = new HashSet<>();

        // Add editors
        for (ChannelEditor editor : editorList.getEditors()) twitchUsers.add(new TwitchUser(editor));
        return twitchUsers;
    }

    // Get VIPs
    public HashSet<TwitchUser> getVIPs(Integer channelId, @Nullable String cursor) {

        // Check Parameters
        if (channelId == null || channelId < 1) throw new IllegalArgumentException("Channel ID cannot be null or less than 1");

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.CHANNEL_READ_VIPS);

        // Null check
        if (accessToken == null || accessToken.isBlank()) {
            System.err.println("Failed to get access token");
            return null;
        }

        // Get VIPs
        ChannelVipList vipList = helix.getChannelVips(accessToken, channelId.toString(), null, 100, cursor).execute();

        // Null check
        if (vipList == null || vipList.getData() == null || vipList.getData().isEmpty()) {
            System.err.println("Failed to get VIPs");
            return null;
        }

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

        // Check Parameters
        if (channelId == null || channelId < 1) throw new IllegalArgumentException("Channel ID cannot be null or less than 1");
        if (userId == null || userId < 1) throw new IllegalArgumentException("User ID cannot be null or less than 1");

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.CHANNEL_READ_VIPS);

        // Null check
        if (accessToken == null || accessToken.isBlank()) {
            System.err.println("Failed to get access token");
            return false;
        }

        // Get moderators
        ChannelVipList vipList = helix.getChannelVips(accessToken, channelId.toString(), Collections.singletonList(channelId.toString()), 1, null).execute();

        // Check if user is moderator
        return !vipList.getData().isEmpty();
    }


    public HashSet<TwitchUser> getFollowers(Integer channelId, @Nullable String cursor) {

        // Check Parameters
        if (channelId == null || channelId < 1) throw new IllegalArgumentException("Channel ID cannot be null or less than 1");

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.MODERATOR_READ_FOLLOWERS);

        // Null check
        if (accessToken == null || accessToken.isBlank()) {
            System.err.println("Failed to get access token");
            return null;
        }

        // Get followers
        InboundFollowers inboundFollowers = helix.getChannelFollowers(accessToken, channelId.toString(), null, 100, cursor).execute();

        // Null check
        if (inboundFollowers == null) {
            System.err.println("Failed to get followers");
            return null;
        }

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
        followers.replace(channelId, twitchUsers);

        // Return followers
        return twitchUsers;
    }

    // Check if user is follower
    public boolean isFollower(Integer channelId, Integer userId) {

        // Check Parameters
        if (channelId == null || channelId < 1) throw new IllegalArgumentException("Channel ID cannot be null or less than 1");
        if (userId == null || userId < 1) throw new IllegalArgumentException("User ID cannot be null or less than 1");

        // Get access token
        String accessToken = getAccessToken(channelId, Scope.MODERATOR_READ_FOLLOWERS);

        // Null check
        if (accessToken == null || accessToken.isBlank()) {
            System.err.println("Failed to get access token");
            return false;
        }

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

            // Variables
            String text;

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

                // Parse token
                boolean success = helixHandler.parseToken(sendRequest(createRequest(body)), null);

                text = success ? "Successfully authenticated! \nYou can close this tab now!" : "Failed to authenticate, please try again";
            } else text = "Failed to authenticate, please try again";

            // Send response
            exchange.sendResponseHeaders(200, text.length());
            exchange.getResponseBody().write(text.getBytes());
            exchange.getResponseBody().close();
        }
    }
}