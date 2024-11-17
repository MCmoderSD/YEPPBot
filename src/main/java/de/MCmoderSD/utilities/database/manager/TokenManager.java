package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.objects.AuthToken;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.other.Encryption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;

public class TokenManager {

    // Associations
    private final MySQL mySQL;

    // Constructor
    public TokenManager(MySQL mySQL) {

        // Set associations
        this.mySQL = mySQL;

        // Initialize
        initTables();
    }

    // Initialize Tables
    private void initTables() {
        try {

            // Variables
            Connection connection = mySQL.getConnection();

            // Condition for creating tables
            String condition = "CREATE TABLE IF NOT EXISTS ";

            // SQL statement for creating the fact list table
            connection.prepareStatement(condition +
                    """
                    AuthTokens (
                        id INT PRIMARY KEY,
                        accessToken TEXT NOT NULL,
                        refreshToken TEXT NOT NULL,
                        scopes TEXT NOT NULL,
                        expires_in INT NOT NULL,
                        timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (id) REFERENCES users(id)
                    )
                    """
            ).execute();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Add or update token
    public void addToken(String name, AuthToken authToken, Encryption encryption) {

        // Log message
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Variables
            var id = authToken.getId();

            // Check Channel and User
            mySQL.checkCache(id, name, false);
            mySQL.checkCache(id, name, true);

            // Prepare statement
            String query = "INSERT INTO AuthTokens (id, accessToken, refreshToken, scopes, expires_in, timestamp) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "accessToken = VALUES(accessToken), " +
                    "refreshToken = VALUES(refreshToken), " +
                    "scopes = VALUES(scopes), " +
                    "expires_in = VALUES(expires_in), " +
                    "timestamp = VALUES(timestamp)";

            PreparedStatement insertPreparedStatement = mySQL.getConnection().prepareStatement(query);
            insertPreparedStatement.setInt(1, id);                                                  // set id
            insertPreparedStatement.setString(2, encryption.encrypt(authToken.getAccessToken()));   // set access token
            insertPreparedStatement.setString(3, encryption.encrypt(authToken.getRefreshToken()));  // set refresh token
            insertPreparedStatement.setString(4, authToken.getScopesAsString());                    // set scopes
            insertPreparedStatement.setInt(5, authToken.getExpiresIn());                            // set expires in
            insertPreparedStatement.setTimestamp(6, authToken.getTimestamp());                      // set timestamp
            insertPreparedStatement.executeUpdate(); // execute

            // Close resources
            insertPreparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Refresh tokens
    public void refreshTokens(String oldRefreshToken, AuthToken authToken, Encryption encryption) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "UPDATE AuthTokens SET accessToken = ?, refreshToken = ?, scopes = ?, expires_in = ?, timestamp = ? WHERE id = ? AND refreshToken = ?";
            PreparedStatement updatePreparedStatement = mySQL.getConnection().prepareStatement(query);
            updatePreparedStatement.setString(1, encryption.encrypt(authToken.getAccessToken()));   // set access token
            updatePreparedStatement.setString(2, encryption.encrypt(authToken.getRefreshToken()));  // set refresh token
            updatePreparedStatement.setString(3, authToken.getScopesAsString());                    // set scopes
            updatePreparedStatement.setInt(4, authToken.getExpiresIn());                            // set expires in
            updatePreparedStatement.setTimestamp(5, authToken.getTimestamp());                      // set timestamp
            updatePreparedStatement.setInt(6, authToken.getId());                                   // set id
            updatePreparedStatement.setString(7, encryption.encrypt(oldRefreshToken));              // set old refresh token
            updatePreparedStatement.executeUpdate(); // execute

            // Close resources
            updatePreparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Get access token
    public AuthToken getAuthToken(HelixHandler helixHandler, int id, Encryption encryption) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT * FROM AuthTokens WHERE id = ?";
            PreparedStatement selectPreparedStatement = mySQL.getConnection().prepareStatement(query);
            selectPreparedStatement.setInt(1, id); // set id
            ResultSet resultSet = selectPreparedStatement.executeQuery();

            // Get AuthToken
            if (resultSet.next()) return new AuthToken(helixHandler, resultSet, encryption);
            else return null;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    // Get all AuthTokens
    public HashMap<Integer, AuthToken> getAuthTokens(HelixHandler helixHandler, Encryption encryption) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT * FROM AuthTokens";
            PreparedStatement selectPreparedStatement = mySQL.getConnection().prepareStatement(query);
            ResultSet resultSet = selectPreparedStatement.executeQuery();

            // Get AuthTokens
            HashMap<Integer, AuthToken> authTokens = new HashMap<>();
            while (resultSet.next()) authTokens.put(resultSet.getInt("id"), new AuthToken(helixHandler, resultSet, encryption));

            // Close resources
            resultSet.close();
            selectPreparedStatement.close();

            // Return AuthTokens
            return authTokens;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}