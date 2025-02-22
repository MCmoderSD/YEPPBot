import de.MCmoderSD.encryption.Encryption;
import de.MCmoderSD.sql.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UpdateToken {

    // Change Encryption Token
    public static void main(String[] args) throws SQLException {

        // Old and new token
        String oldToken = "oauth:"; // Old token
        String newToken = "oauth:"; // New token

        // Create encryptors
        Encryption oldEncryptor = new Encryption(oldToken); // Old token
        Encryption newEncryptor = new Encryption(newToken); // New token

        // Database connection
        String host = "localhost";
        int port = 3306; // SQL Default Port
        String database = "your_database";
        String username = "your_username";
        String password = "your_password";

        // Connect to database
        Connection connection = DriverManager.getConnection(Driver.DatabaseType.MARIADB.getUrl(host, port, database), username, password); // connect

        // Replace old token with new token
        ResultSet resultSet = connection.prepareStatement("SELECT * FROM AuthTokens").executeQuery();
        while (resultSet.next()) {
            try {

                // Get the token
                String accessToken = resultSet.getString("accessToken");
                String refreshToken = resultSet.getString("refreshToken");

                // Decrypt the token
                String oldAccessToken = oldEncryptor.decrypt(accessToken);
                String oldRefreshToken = oldEncryptor.decrypt(refreshToken);

                // Encrypt the token
                String newAccessToken = newEncryptor.encrypt(oldAccessToken);
                String newRefreshToken = newEncryptor.encrypt(oldRefreshToken);

                connection.prepareStatement("UPDATE AuthTokens SET accessToken = '" + newAccessToken + "', refreshToken = '" + newRefreshToken + "' WHERE accessToken = '" + accessToken + "' AND refreshToken = '" + refreshToken + "'").execute();

                // Log the change
                System.out.println("Updated token from Client ID: " + resultSet.getInt("id"));

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}