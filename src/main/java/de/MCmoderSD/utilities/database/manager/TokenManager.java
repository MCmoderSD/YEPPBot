package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.utilities.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class TokenManager {

    // Assosiations
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
    public void addToken(int id, String name, String accessToken, String refreshToken, String scopes, int expiresIn) {

        // Log message
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

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
            insertPreparedStatement.setInt(1, id); // set id
            insertPreparedStatement.setString(2, accessToken); // set access token
            insertPreparedStatement.setString(3, refreshToken); // set refresh token
            insertPreparedStatement.setString(4, scopes); // set scopes
            insertPreparedStatement.setInt(5, expiresIn); // set expires in
            insertPreparedStatement.setTimestamp(6, getTimestamp()); // set timestamp
            insertPreparedStatement.executeUpdate(); // execute
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Refresh tokens
    public void refreshTokens(String oldRefreshToken, String newAccessToken, String newRefreshToken, int expiresIn) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "UPDATE AuthTokens SET accessToken = ?, refreshToken = ?, expires_in = ?, timestamp = ? WHERE refreshToken = ?";
            PreparedStatement updatePreparedStatement = mySQL.getConnection().prepareStatement(query);
            updatePreparedStatement.setString(1, newAccessToken); // set access token
            updatePreparedStatement.setString(2, newRefreshToken); // set refresh token
            updatePreparedStatement.setInt(3, expiresIn); // set expires in
            updatePreparedStatement.setTimestamp(4, getTimestamp()); // set timestamp
            updatePreparedStatement.setString(5, oldRefreshToken); // set old refresh token
            updatePreparedStatement.executeUpdate(); // execute
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Check if the token has the required scopes
    public boolean hasScope(int id, HelixHandler.Scope... requiredScopes) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT scopes FROM AuthTokens WHERE id = ?";
            PreparedStatement selectPreparedStatement = mySQL.getConnection().prepareStatement(query);
            selectPreparedStatement.setInt(1, id); // set id
            ResultSet resultSet = selectPreparedStatement.executeQuery();

            // Check if the user has the required scopes
            if (resultSet.next()) {
                String scopes = resultSet.getString("scopes"); // get scopes
                String[] scopeArray = scopes.split("\\+"); // split scopes by '+'

                // Convert scopeArray to a HashSet for easy lookup
                HashSet<String> scopeSet = new HashSet<>(Arrays.asList(scopeArray));

                // Check if all required scopes are present
                for (HelixHandler.Scope requiredScope : requiredScopes) if (!scopeSet.contains(requiredScope.toString())) return false;
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    // Get access token
    public String getAccessToken(int id) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT accessToken FROM AuthTokens WHERE id = ?";
            PreparedStatement selectPreparedStatement = mySQL.getConnection().prepareStatement(query);
            selectPreparedStatement.setInt(1, id); // set id
            ResultSet resultSet = selectPreparedStatement.executeQuery();

            // Return access token
            return resultSet.next() ? resultSet.getString("accessToken") : null;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}