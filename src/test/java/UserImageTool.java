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

public class UserImageTool {

    public static void main(String[] args) throws Exception {

        // Initialize SQL
        SQL sql = new SQL(JsonUtility.getInstance().load("/database.json"));

        // Get Users
        HashSet<TwitchUser> users = sql.getTwitchUsers();
        System.out.println("Total Users: " + users.size());

        // Download Profile and Offline Images
        for (TwitchUser user : users) {
            var profileUrl = user.getProfileImageUrl();
            if (profileUrl != null && validUUID(profileUrl.substring(47, 83))) sql.checkImage(user, profileUrl, PROFILE);

            var offlineUrl = user.getOfflineImageUrl();
            if (offlineUrl != null && validUUID(offlineUrl.substring(47, 83))) sql.checkImage(user, offlineUrl, OFFLINE);
        }

        sql.writeImages();
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static class SQL extends Driver {

        public SQL(JsonNode config) {
            super(DatabaseType.MARIADB, config);
        }

        public HashSet<TwitchUser> getTwitchUsers() throws Exception {

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id, user FROM  User"
            );

            var resultSet = preparedStatement.executeQuery();
            HashSet<TwitchUser> users = new HashSet<>();
            while (resultSet.next()) users.add((TwitchUser) inflateObject(resultSet.getBytes("user")));
            return users;
        }

        public void checkImage(TwitchUser user, String imageUrl, UserImageType imageType) {
            new Thread(() -> {
                try {

                    // Image UUID
                    var uuid = asBytes(UUID.fromString(imageUrl.substring(47, 83)));

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
                        throw new RuntimeException("Failed to download image from URL: " + imageUrl, e);
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

        public void writeImages() throws Exception {

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT i.image, u.user, i.uuid, i.type, i.format FROM UserImage i JOIN User u ON i.id = u.id;"
            );

            var resultSet = preparedStatement.executeQuery();

            // Create directories
            File dir = new File("UserImages/");
            File profileDir = new File(dir, "Profile/");
            File offlineDir = new File(dir, "Offline/");
            if (!profileDir.exists()) profileDir.mkdirs();
            if (!offlineDir.exists()) offlineDir.mkdirs();

            while (resultSet.next()) {

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
            }
        }
    }
}
