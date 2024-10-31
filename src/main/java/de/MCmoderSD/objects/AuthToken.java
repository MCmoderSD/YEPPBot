package de.MCmoderSD.objects;

import de.MCmoderSD.core.HelixHandler;

import java.sql.Timestamp;

import java.util.Arrays;

@SuppressWarnings("unused")
public class AuthToken {

    // Attributes
    public final Integer id;
    public final String accessToken;
    public final String refreshToken;
    public final String[] scopes;
    public final Integer expiresIn;
    public final Timestamp timestamp;
    public final Timestamp nextRefresh;

    // Constructor
    public AuthToken(Integer id, String accessToken, String refreshToken, String scopes, Integer expiresIn, Timestamp timestamp) {

        // Set attributes
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.scopes = scopes.split("\\+");
        this.expiresIn = expiresIn;
        this.timestamp = timestamp;

        // Format next refresh
        nextRefresh = new Timestamp(timestamp.getTime() + (expiresIn - 180) * 1000L);
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String[] getScopes() {
        return scopes;
    }

    public String getScopesAsString() {
        return String.join("+", scopes);
    }

    public boolean hasScope(HelixHandler.Scope ... scopes) {
        return Arrays.stream(scopes).allMatch(scope -> Arrays.asList(this.scopes).contains(scope.getScope()));
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public boolean needsRefresh() {
        return System.currentTimeMillis() >= nextRefresh.getTime();
    }
}
