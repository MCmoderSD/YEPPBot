package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.objects.AuthToken;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.other.Encryption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.HashMap;

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

            // Close resources
            connection.close();

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
            insertPreparedStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis())); // set timestamp
            insertPreparedStatement.executeUpdate(); // execute

            // Close resources
            insertPreparedStatement.close();

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
            updatePreparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis())); // set timestamp
            updatePreparedStatement.setString(5, oldRefreshToken); // set old refresh token
            updatePreparedStatement.executeUpdate(); // execute

            // Close resources
            updatePreparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Get access token
    public AuthToken getAuthToken(int id) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT * FROM AuthTokens WHERE id = ?";
            PreparedStatement selectPreparedStatement = mySQL.getConnection().prepareStatement(query);
            selectPreparedStatement.setInt(1, id); // set id
            ResultSet resultSet = selectPreparedStatement.executeQuery();

            // Get AuthToken
            if (resultSet.next()) {
                return new AuthToken(
                        resultSet.getInt("id"), // get id
                        resultSet.getString("accessToken"), // get access token
                        resultSet.getString("refreshToken"), // get refresh token
                        resultSet.getString("scopes"), // get scopes
                        resultSet.getInt("expires_in"), // get expires in
                        resultSet.getTimestamp("timestamp") // get timestamp
                );
            } else return null;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    // Get all AuthTokens
    public HashMap<Integer, AuthToken> getAuthTokens(Encryption encryption) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT * FROM AuthTokens";
            PreparedStatement selectPreparedStatement = mySQL.getConnection().prepareStatement(query);
            ResultSet resultSet = selectPreparedStatement.executeQuery();

            // Get AuthTokens
            HashMap<Integer, AuthToken> authTokens = new HashMap<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                authTokens.put(
                        id,
                        new AuthToken(
                                id, // get id
                                encryption.decrypt(resultSet.getString("accessToken")), // get access token
                                encryption.decrypt(resultSet.getString("refreshToken")), // get refresh token
                                resultSet.getString("scopes"), // get scopes
                                resultSet.getInt("expires_in"), // get expires in
                                resultSet.getTimestamp("timestamp") // get timestamp
                        )
                );
            }

            // Close resources
            resultSet.close();
            selectPreparedStatement.close();

            return authTokens;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}