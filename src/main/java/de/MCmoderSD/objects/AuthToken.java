package de.MCmoderSD.objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.enums.Scope;
import de.MCmoderSD.utilities.other.Encryption;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Format.formatScopes;

@SuppressWarnings("unused")
public class AuthToken {

    // Attributes
    private final int id;
    private final String accessToken;
    private final String refreshToken;
    private final Scope[] scopes;
    private final int expiresIn;
    private final Timestamp timestamp;

    // JSON Constructor
    public AuthToken(HelixHandler helixHandler, String responseBody) throws JsonProcessingException {

        // Parse JSON
        JsonNode jsonNode = new ObjectMapper().readTree(responseBody);

        // Extract data
        accessToken = jsonNode.get("access_token").asText();
        refreshToken = jsonNode.get("refresh_token").asText();
        scopes = Scope.getScopes(formatScopes(jsonNode.get("scope")));
        expiresIn = jsonNode.get("expires_in").asInt();
        timestamp = new Timestamp(System.currentTimeMillis());

        // Get user ID
        id = Integer.parseInt(helixHandler.getHelix().getUsers(accessToken, null, null).execute().getUsers().getFirst().getId());

        // Set next refresh
        new Thread(() -> refresh(helixHandler)).start();
    }

    // Database Constructor
    public AuthToken(HelixHandler helixHandler, ResultSet resultSet, Encryption encryption) throws SQLException {

        // Extract data
        id = resultSet.getInt("id");
        accessToken = encryption.decrypt(resultSet.getString("accessToken"));
        refreshToken = encryption.decrypt(resultSet.getString("refreshToken"));
        scopes = Scope.getScopes(resultSet.getString("scopes"));
        expiresIn = resultSet.getInt("expires_in");
        timestamp = resultSet.getTimestamp("timestamp");

        // Set next refresh
        new Thread(() -> refresh(helixHandler)).start();
    }

    private void refresh(HelixHandler helixHandler) {

        // Sleep until the token expires
        try {
            var sleepTime = expiresIn * 1000L - (System.currentTimeMillis() - timestamp.getTime());
            if (sleepTime > 1000) Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Refresh the token
        helixHandler.refreshToken(this);
    }

    public String getScopesAsString() {
        return Arrays.stream(scopes).map(Scope::getScope).reduce((s1, s2) -> s1 + "+" + s2).orElse("");
    }

    // Getters

    public int getId() {
        return id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Scope[] getScopes() {
        return scopes;
    }

    public boolean hasScope(Scope... scopes) {
        return Arrays.stream(scopes).allMatch(scope -> Arrays.asList(this.scopes).contains(scope));
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
