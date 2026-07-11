import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.helix.TwitchHelix;

import de.MCmoderSD.enums.UserImageType;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.sql.Driver;

import java.sql.SQLException;

import static de.MCmoderSD.enums.ImageFormat.getFormat;
import static de.MCmoderSD.sql.Driver.DatabaseType.*;
import static de.MCmoderSD.utilities.FormatUUID.*;
import static de.MCmoderSD.enums.UserImageType.*;
import static de.MCmoderSD.tools.GZIP.*;
import static java.lang.IO.println;
import static java.util.UUID.fromString;

void main() {

    // OAuth Token
    var oauthToken = "OAUTH_TOKEN"; // Replace with your actual OAuth token

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

    // Initialize Helix
    var helix = TwitchClientBuilder.builder()
            .withDefaultAuthToken(new OAuth2Credential("twitch", oauthToken))
            .withEnableHelix(true)
            .build()
            .getHelix();

    // Get User IDs from Database
    var ids = sql.getAllUserIds();

    // Fetch Users
    var users = fetchUsersByID(ids, helix);
    println("Fetched users: " + users.size());

    // Update Database
    for (var user : users) {
        println("Updating user: " + user.getUsername() + " (" + user.getId() + ")");
        sql.updateUser(user);
    }
}

private HashSet<TwitchUser> fetchUsersByID(HashSet<Integer> ids, TwitchHelix helix) {

    // Batch IDs
    var batches = new ArrayList<ArrayList<String>>();
    var currentBatch = new ArrayList<String>();
    for (var id : ids) {
        currentBatch.add(id.toString());
        if (currentBatch.size() == 100) {
            batches.add(currentBatch);
            currentBatch = new ArrayList<>();
        }
    }
    if (!currentBatch.isEmpty()) batches.add(currentBatch);

    // Log Batching Info
    println("Total batches: " + batches.size());
    println("Total users to fetch: " + ids.size());

    // Fetch Users
    var users = new HashSet<TwitchUser>();
    for (var batch : batches) {
        println("Fetching batch of " + batch.size() + " users...");
        var response = helix.getUsers(null, batch, null).execute();
        response.getUsers().forEach(user -> users.add(new TwitchUser(user)));
    }

    // Return
    return users;
}

@SuppressWarnings("unused")
private HashSet<TwitchUser> fetchUsersByName(HashSet<String> names, TwitchHelix helix) {

    // Batch IDs
    var batches = new ArrayList<ArrayList<String>>();
    var currentBatch = new ArrayList<String>();
    for (var name : names) {
        currentBatch.add(name);
        if (currentBatch.size() == 100) {
            batches.add(currentBatch);
            currentBatch = new ArrayList<>();
        }
    }
    if (!currentBatch.isEmpty()) batches.add(currentBatch);

    // Log Batching Info
    println("Total batches: " + batches.size());
    println("Total users to fetch: " + names.size());

    // Fetch Users
    var users = new HashSet<TwitchUser>();
    for (var batch : batches) {
        println("Fetching batch of " + batch.size() + " users...");
        var response = helix.getUsers(null, null, batch).execute();
        response.getUsers().forEach(user -> users.add(new TwitchUser(user)));
    }

    // Return
    return users;
}

// SQL Driver Implementation
private static class SQL extends Driver {

    // Constructor
    public SQL(Builder builder) {

        // Initialize Driver
        super(builder);

        // Connect to Database
        connect();
    }

    // Check Image Method
    private void checkImage(TwitchUser user, String imageUrl, UserImageType imageType) {
        try {

            // Image UUID
            var uuid = asBytes(fromString(imageUrl.substring(47, 83)));

            // Check if image is already downloaded
            var checkStatement = connection.prepareStatement(
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
                    "INSERT IGNORE INTO UserImage (uuid, id, url, size, totalSize, type, format, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
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
    }

    // Update User Method
    public void updateUser(TwitchUser user) {
        try {

            // Check Parameters
            if (user == null) throw new IllegalArgumentException("TwitchUser cannot be null");

            // Variables
            var data = deflateObject(user);
            var profileImageUrl = user.getProfileImageUrl();
            var offlineImageUrl = user.getOfflineImageUrl();

            // Prepare the SQL statement
            var preparedStatement = connection.prepareStatement(
                    "UPDATE User SET username = ?, displayName = ?, type = ?, broadcasterType = ?, user = ? WHERE id = ?;"
            );

            // Set the update values
            preparedStatement.setString(1, user.getUsername());                 // Twitch Username
            preparedStatement.setString(2, user.getDisplayName());              // Twitch Display Name
            preparedStatement.setString(3, user.getType().name());              // Type
            preparedStatement.setString(4, user.getBroadcasterType().name());   // Broadcaster Type
            preparedStatement.setBytes(5, data);                                // TwitchUser Object (compressed)
            preparedStatement.setInt(6, user.getId());                          // Twitch User ID

            // Execute the statement
            preparedStatement.executeUpdate();

            // Close the statement
            preparedStatement.close();

            // Check profile image
            if (profileImageUrl != null && validUUID(profileImageUrl.substring(47, 83))) checkImage(user, profileImageUrl, PROFILE);
            if (offlineImageUrl != null && validUUID(offlineImageUrl.substring(47, 83))) checkImage(user, offlineImageUrl, OFFLINE);

        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to add or update TwitchUser with id " + user.getId() + ": " + e.getMessage(), e);
        }
    }

    // Get All User IDs Method
    public HashSet<Integer> getAllUserIds() {
        try {

            // Prepare the SQL statement
            var preparedStatement = connection.prepareStatement(
                    "SELECT id FROM User;"
            );

            // Execute the query
            var resultSet = preparedStatement.executeQuery();

            // Prepare result set
            var userIds = new HashSet<Integer>();

            // Process the results
            while (resultSet.next()) userIds.add(resultSet.getInt("id"));

            // Close resources
            resultSet.close();
            preparedStatement.close();

            // Return IDs
            return userIds;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve all user IDs: " + e.getMessage(), e);
        }
    }
}