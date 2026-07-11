import de.MCmoderSD.enums.ImageFormat;
import de.MCmoderSD.enums.UserImageType;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.sql.Driver;

import java.sql.SQLException;

import static de.MCmoderSD.tools.GZIP.*;
import static de.MCmoderSD.enums.UserImageType.*;
import static de.MCmoderSD.utilities.FormatUUID.*;
import static de.MCmoderSD.utilities.ZipUtil.*;
import static de.MCmoderSD.sql.Driver.DatabaseType.*;
import static de.MCmoderSD.enums.ImageFormat.getFormat;
import static java.lang.IO.println;
import static java.util.UUID.fromString;

void main() {

    // Load Config
    var config = JsonUtility.getInstance().loadResource("/Database.json");

    // Initialize SQL
    var sql = new SQL(SQL.builder()
            .withType(MARIADB)
            .withHost(config.get("host").asString())
            .withPort(config.get("port").asInt())
            .withDatabase(config.get("database").asString())
            .withUsername(config.get("username").asString())
            .withPassword(config.get("password").asString())
    );

    // Get Users
    var users = sql.getTwitchUsers();
    println("Total Users: " + users.size());

    // Download Profile and Offline Images
    for (var user : users)
        new Thread(() -> {

            // Profile Image
            var profileUrl = user.getProfileImageUrl();
            if (profileUrl != null && validUUID(profileUrl.substring(47, 83))) sql.checkImage(user, profileUrl, PROFILE);

            // Offline Image
            var offlineUrl = user.getOfflineImageUrl();
            if (offlineUrl != null && validUUID(offlineUrl.substring(47, 83))) sql.checkImage(user, offlineUrl, OFFLINE);

        }).start();

    // Write Images to Disk
    sql.writeImages();
}

// SQL Driver Implementation
private static class SQL extends Driver {

    // Constructor
    public SQL(Builder builder) {

        // Initialize the Database Driver
        super(builder);
        connect();

        // Initialize Tables
        try {

            // User Table
            var userTable = connection.prepareStatement(
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
            var userImageTable = connection.prepareStatement(
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

    // Get all Twitch Users
    public HashSet<TwitchUser> getTwitchUsers() {
        try {

            // Get all users
            var preparedStatement = connection.prepareStatement(
                    "SELECT username, user FROM  User"
            );

            // Execute query
            var resultSet = preparedStatement.executeQuery();

            // Process results
            var users = new HashSet<TwitchUser>();
            while (resultSet.next()) users.add(inflateTwitchUser(resultSet.getBytes("user")));

            // Close resources
            resultSet.close();
            preparedStatement.close();

            // Return users
            return users;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get TwitchUsers: " + e.getMessage(), e);
        }
    }

    // Check and Insert Image Method
    public void checkImage(TwitchUser user, String imageUrl, UserImageType imageType) {
        new Thread(() -> {
            try {

                // Image UUID
                var uuid = asBytes(fromString(imageUrl.substring(47, 83)));

                // Check if image is already downloaded
                var checkStatement = connection.prepareStatement(
                        "SELECT COUNT(uuid) AS count FROM UserImage WHERE uuid = ?;"
                );

                // Set the query value
                checkStatement.setBytes(1, uuid); // Image UUID

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
                var imageFormat = getFormat(imageUrl);

                // Download Image
                try (var bis = new BufferedInputStream(new URI(imageUrl).toURL().openStream())) {
                    imageData = bis.readAllBytes();
                } catch (IOException | URISyntaxException e) {
                    throw new IOException("Failed to download image from URL: " + imageUrl, e);
                }

                // Compress Image
                compressedData = deflate(imageData);

                // Image Sizes
                var uncompressed = imageData.length;
                var compressed = compressedData.length;

                // Insert into database
                var insertStatement = connection.prepareStatement(
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
            var preparedStatement = connection.prepareStatement(
                    "SELECT i.image, u.user, i.uuid, i.type, i.format FROM UserImage i, User u WHERE i.id = u.id;"
            );

            // Execute query
            var resultSet = preparedStatement.executeQuery();

            // Create directories
            var dir = new File("UserImages/");
            var profileDir = new File(dir, "Profile/");
            var offlineDir = new File(dir, "Offline/");
            if (!profileDir.exists()) profileDir.mkdirs();
            if (!offlineDir.exists()) offlineDir.mkdirs();

            // Write images
            while (resultSet.next()) {
                try {

                    // Get Data
                    var imageData = inflate(resultSet.getBytes("image"));
                    var user = inflateTwitchUser(resultSet.getBytes("user"));
                    var uuid = fromString(resultSet.getString("uuid"));
                    var imageType = UserImageType.valueOf(resultSet.getString("type"));
                    var imageFormat = ImageFormat.valueOf(resultSet.getString("format"));

                    // Create file
                    var imageFile = switch (imageType) {
                        case PROFILE -> new File(profileDir, user.getDisplayName() + "_" + uuid + "." + imageFormat.name().toLowerCase());
                        case OFFLINE -> new File(offlineDir, user.getDisplayName() + "_" + uuid + "." + imageFormat.name().toLowerCase());
                    };

                    // Write file
                    Files.write(imageFile.toPath(), imageData);
                } catch (IOException | SQLException e) {
                    throw new RuntimeException("Failed to write user image: " + e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to write user images: " + e.getMessage(), e);
        }
    }
}