package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.encryption.Encryption;
import de.MCmoderSD.objects.AuthToken;
import de.MCmoderSD.utilities.database.SQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;

public class TokenManager {

    // Associations
    private final SQL sql;

    // Constructor
    public TokenManager(SQL sql) {

        // Set associations
        this.sql = sql;

        // Initialize
        initTables();
    }

    // Initialize Tables
    private void initTables() {
        try {

            // Variables
            Connection connection = sql.getConnection();

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
                        expiresIn INT NOT NULL,
                        timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (id) REFERENCES Users(id)
                    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=ascii
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
            if (!sql.isConnected()) sql.connect(); // connect

            // Variables
            var id = authToken.getId();

            // Check Channel and User
            sql.checkCache(id, name, false);
            sql.checkCache(id, name, true);

            // Prepare statement
            PreparedStatement insertPreparedStatement = sql.getConnection().prepareStatement(
                    """
                        INSERT INTO AuthTokens (id, accessToken, refreshToken, scopes, expiresIn, timestamp)
                        VALUES (?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                        accessToken = VALUES(accessToken),
                        refreshToken = VALUES(refreshToken),
                        scopes = VALUES(scopes),
                        expiresIn = VALUES(expiresIn),
                        timestamp = VALUES(timestamp)
                        """
            );

            // Set values and execute
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
            if (!sql.isConnected()) sql.connect(); // connect

            // Prepare statement
            PreparedStatement updatePreparedStatement = sql.getConnection().prepareStatement(
                    "UPDATE AuthTokens SET accessToken = ?, refreshToken = ?, scopes = ?, expiresIn = ?, timestamp = ? WHERE id = ? AND refreshToken = ?"
            );

            // Set values and execute
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
            if (!sql.isConnected()) sql.connect(); // connect

            // Prepare statement
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    "SELECT * FROM AuthTokens WHERE id = ?"
            );

            // Set values and execute
            preparedStatement.setInt(1, id); // set id
            ResultSet resultSet = preparedStatement.executeQuery();

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
            if (!sql.isConnected()) sql.connect(); // connect

            // Prepare statement
            PreparedStatement selectPreparedStatement = sql.getConnection().prepareStatement(
                    "SELECT * FROM AuthTokens"
            );

            // Execute
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

    // Delete AuthToken
    public int deleteAuthToken(String refreshToken, Encryption encryption) {
        try {
            if (!sql.isConnected()) sql.connect(); // connect

            // Get ID
            PreparedStatement selectPreparedStatement = sql.getConnection().prepareStatement(
                    "SELECT id FROM AuthTokens WHERE refreshToken = ?"
            );

            // Set values and execute
            selectPreparedStatement.setString(1, encryption.encrypt(refreshToken)); // set refresh token
            ResultSet resultSet = selectPreparedStatement.executeQuery(); // execute

            // Get ID
            if (!resultSet.next()) return -1;
            var id = resultSet.getInt("id");

            // Delete AuthToken
            PreparedStatement deletePreparedStatement = sql.getConnection().prepareStatement(
                    "DELETE FROM AuthTokens WHERE id = ?"
            );

            // Set values and execute
            deletePreparedStatement.setInt(1, id); // set id
            deletePreparedStatement.executeUpdate(); // execute

            // Close resources
            deletePreparedStatement.close();
            selectPreparedStatement.close();
            resultSet.close();
            return id;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }
}