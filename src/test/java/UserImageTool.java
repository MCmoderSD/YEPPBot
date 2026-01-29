import de.MCmoderSD.enums.ImageFormat;
import de.MCmoderSD.enums.UserImageType;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.sql.Driver;
import tools.jackson.databind.JsonNode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;

import static de.MCmoderSD.enums.ImageFormat.getFormat;
import static de.MCmoderSD.utilities.FormatUUID.*;
import static de.MCmoderSD.enums.UserImageType.*;
import static de.MCmoderSD.tools.GZIP.*;

import static java.util.UUID.fromString;

void main() throws IOException, URISyntaxException {

    // Load Config
    JsonNode config = JsonUtility.getInstance().load("/database.json");

    // Build Driver
    Driver.Builder builder = Driver.Builder
            .withType(Driver.DatabaseType.MARIADB)
            .withHost(config.get("host").asString())
            .withPort(config.get("port").asInt())
            .withDatabase(config.get("database").asString())
            .withUsername(config.get("username").asString())
            .withPassword(config.get("password").asString());

    // Initialize SQL
    SQL sql = new SQL(builder);

    // Get Users
    HashSet<TwitchUser> users = sql.getTwitchUsers();
    IO.println("Total Users: " + users.size());

    // Download Profile and Offline Images
    for (var user : users)
        new Thread(() -> {

            // Profile Image
            var profileUrl = user.getProfileImageUrl();
            if (profileUrl != null && validUUID(profileUrl.substring(47, 83)))
                sql.checkImage(user, profileUrl, PROFILE);

            // Offline Image
            var offlineUrl = user.getOfflineImageUrl();
            if (offlineUrl != null && validUUID(offlineUrl.substring(47, 83)))
                sql.checkImage(user, offlineUrl, OFFLINE);

        }).start();

    // Write Images to Disk
    sql.writeImages();
}

private static class SQL extends Driver {

    public SQL(Builder builder) {

        // Initialize the Database Driver
        super(builder);
        connect();

        // Initialize Tables
        try {

            // User Table
            PreparedStatement userTable = connection.prepareStatement(
                    """
                        # User Table Definition
                        CREATE TABLE IF NOT EXISTS User (
                            id              INT                 PRIMARY KEY,                                                # Twitch User ID
                            username        VARCHAR(25)         UNIQUE                      NOT NULL,                       # Twitch Username
                            displayName     VARCHAR(25)         UNIQUE                      NOT NULL,                       # Twitch Display Name
                            type            ENUM('USER', 'STAFF', 'GLOBAL_MOD', 'ADMIN')    NOT NULL    DEFAULT 'USER',     # Type
                            broadcasterType ENUM('NONE', 'AFFILIATE', 'PARTNER')            NOT NULL    DEFAULT 'NONE',     # Broadcaster Type
                            user            BLOB                UNIQUE                      NOT NULL                        # TwitchUser Object (compressed)
                        )
                            ROW_FORMAT = COMPRESSED     # Compressed Row Format
                            KEY_BLOCK_SIZE = 1          # Key Block Size
                            CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
                            COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4
                        """
            );

            // UserImage Table
            PreparedStatement userImageTable = connection.prepareStatement(
                        """
                        # UserImage Table Definition
                        CREATE TABLE IF NOT EXISTS UserImage (
                            uuid            UUID        PRIMARY KEY,                    # Image UUID
                            id              INT                         NOT NULL,       # User ID
                            url             TEXT        UNIQUE          NOT NULL,       # Image URL
                            size            INT                         NOT NULL,       # Compressed Size
                            totalSize       INT                         NOT NULL,       # Uncompressed Size
                            type            ENUM('PROFILE', 'OFFLINE')  NOT NULL,       # Image Type (Profile or Offline)
                            format          ENUM('JPEG', 'PNG', 'GIF')  NOT NULL,       # Image Format (JPEG, PNG, GIF)
                            image           MEDIUMBLOB                  NOT NULL,       # Image Data (compressed)
                            FOREIGN KEY (id) REFERENCES User(id) ON DELETE CASCADE      # Foreign Key to User Table
                        )
                            ROW_FORMAT = COMPRESSED     # Compressed Row Format
                            KEY_BLOCK_SIZE = 1          # Key Block Size
                            CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
                            COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4
                        """
            );

            // Execute Table Creation
            userTable.executeUpdate();
            userImageTable.executeUpdate();

            // Close Resources
            userTable.close();
            userImageTable.close();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize UserImageTool tables: " + e.getMessage(), e);
        }
    }

    public HashSet<TwitchUser> getTwitchUsers() {
        try {

            // Get all users
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id, user FROM  User"
            );

            // Execute query
            var resultSet = preparedStatement.executeQuery();

            // Process results
            HashSet<TwitchUser> users = new HashSet<>();
            while (resultSet.next()) users.add((TwitchUser) inflateObject(resultSet.getBytes("user")));

            // Close resources
            resultSet.close();
            preparedStatement.close();

            // Return users
            return users;

        } catch (SQLException | IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to get TwitchUsers: " + e.getMessage(), e);
        }
    }

    public void checkImage(TwitchUser user, String imageUrl, UserImageType imageType) {
        new Thread(() -> {
            try {

                // Image UUID
                var uuid = asBytes(fromString(imageUrl.substring(47, 83)));

                // Check if image is already downloaded
                PreparedStatement checkStatement = connection.prepareStatement(
                        "SELECT COUNT(uuid) AS count FROM UserImage WHERE uuid = ?;"
                );

                // Set the query value
                checkStatement.setBytes(1, uuid);   // Image UUID

                // Execute the query
                var resultSet = checkStatement.executeQuery();

                // If image exists, return
                if (resultSet.next() && resultSet.getInt(1) == 1) return;

                // Close resources
                checkStatement.close();
                resultSet.close();

                // Variables
                byte[] imageData;
                byte[] compressedData;
                ImageFormat imageFormat = getFormat(imageUrl);

                // Download Image
                try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new URI(imageUrl).toURL().openStream())) {
                    imageData = bufferedInputStream.readAllBytes();
                } catch (IOException | URISyntaxException e) {
                    throw new IOException("Failed to download image from URL: " + imageUrl, e);
                }

                // Compress Image
                compressedData = deflate(imageData);

                // Image Sizes
                var uncompressed = imageData.length;
                var compressed = compressedData.length;

                // Insert into database
                PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO UserImage (uuid, id, url, size, totalSize, type, format, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
                );

                // Set the insert values
                insertStatement.setBytes(1, uuid);                  // Image UUID
                insertStatement.setInt(2, user.getId());            // User ID
                insertStatement.setString(3, imageUrl);             // Image URL
                insertStatement.setInt(4, compressed);              // Compressed Size
                insertStatement.setInt(5, uncompressed);            // Uncompressed Size
                insertStatement.setString(6, imageType.name());     // Image Type (Profile or Offline)
                insertStatement.setString(7, imageFormat.name());   // Image Format (JPEG, PNG, GIF)
                insertStatement.setBytes(8, compressedData);        // Image Data (compressed)

                // Execute the statement
                insertStatement.executeUpdate();

                // Close the statement
                insertStatement.close();

            } catch (SQLException | IOException e) {
                throw new RuntimeException("Failed to check profile image for TwitchUser with id " + user.getId() + ": " + e.getMessage(), e);
            }
        }).start();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void writeImages() {
        try {

            // Get all images
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT i.image, u.user, i.uuid, i.type, i.format FROM UserImage i, User u WHERE i.id = u.id;"
            );

            // Execute query
            var resultSet = preparedStatement.executeQuery();

            // Create directories
            File dir = new File("UserImages/");
            File profileDir = new File(dir, "Profile/");
            File offlineDir = new File(dir, "Offline/");
            if (!profileDir.exists()) profileDir.mkdirs();
            if (!offlineDir.exists()) offlineDir.mkdirs();

            // Write images
            while (resultSet.next()) {
                try {

                    // Get Data
                    byte[] imageData = inflate(resultSet.getBytes("image"));
                    TwitchUser user = (TwitchUser) inflateObject(resultSet.getBytes("user"));
                    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    UserImageType imageType = UserImageType.valueOf(resultSet.getString("type"));
                    ImageFormat imageFormat = ImageFormat.valueOf(resultSet.getString("format"));

                    // Create file
                    File imageFile = switch (imageType) {
                        case PROFILE -> new File(profileDir, user.getDisplayName() + "_" + uuid + "." + imageFormat.name().toLowerCase());
                        case OFFLINE -> new File(offlineDir, user.getDisplayName() + "_" + uuid + "." + imageFormat.name().toLowerCase());
                    };

                    // Write file
                    Files.write(imageFile.toPath(), imageData);
                } catch (IOException | ClassNotFoundException | SQLException e) {
                    throw new RuntimeException("Failed to write user image: " + e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to write user images: " + e.getMessage(), e);
        }
    }
}