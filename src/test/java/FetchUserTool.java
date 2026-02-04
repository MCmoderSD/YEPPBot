import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.helix.TwitchHelix;

import de.MCmoderSD.enums.ImageFormat;
import de.MCmoderSD.enums.UserImageType;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.sql.Driver;
import tools.jackson.databind.JsonNode;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static de.MCmoderSD.enums.ImageFormat.getFormat;
import static de.MCmoderSD.enums.UserImageType.OFFLINE;
import static de.MCmoderSD.enums.UserImageType.PROFILE;
import static de.MCmoderSD.tools.GZIP.deflate;
import static de.MCmoderSD.tools.GZIP.deflateObject;
import static de.MCmoderSD.utilities.FormatUUID.asBytes;
import static de.MCmoderSD.utilities.FormatUUID.validUUID;
import static java.util.UUID.fromString;

void main() throws IOException, URISyntaxException {

    // OAuth Token
    String oauthToken = "OAUTH_TOKEN"; // Replace with your actual OAuth token

    // Load Config
    JsonNode config = JsonUtility.getInstance().load("/database.json");

    // Initialize SQL
    SQL sql = new SQL(Driver.Builder
            .withType(Driver.DatabaseType.MARIADB)
            .withHost(config.get("host").asString())
            .withPort(config.get("port").asInt())
            .withDatabase(config.get("database").asString())
            .withUsername(config.get("username").asString())
            .withPassword(config.get("password").asString())
    );

    // Initialize Helix
    TwitchHelix helix = TwitchClientBuilder.builder()
            .withDefaultAuthToken(new OAuth2Credential("twitch", oauthToken))
            .withEnableHelix(true)
            .build()
            .getHelix();

    // Load resource
    byte[] data = ResourceLoader.loadResource("/Users.tsv");
    String[] lines = new String(data).split("\n");
    HashSet<Integer> ids = new HashSet<>();
    for (var line : lines) {
        try {
            ids.add(Integer.parseInt(line.trim().split("\t")[0].trim()));
        } catch (NumberFormatException e) {
            IO.println("Invalid ID in line: " + line);
        }
    }
    IO.println("Total IDs to fetch: " + ids.size());

    // Fetch Users
    HashSet<TwitchUser> users = fetchUsersbyID(ids, helix);
    IO.println("Fetched users: " + users.size());

    // Update Database
    for (var user : users) {
        IO.println("Updating user: " + user.getUsername() + " (" + user.getId() + ")");
        sql.addTwitchUser(user);
    }
}

@SuppressWarnings("unused")
private HashSet<TwitchUser> fetchUsersbyID(HashSet<Integer> ids, TwitchHelix helix) {

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
    IO.println("Total batches: " + batches.size());
    IO.println("Total users to fetch: " + ids.size());

    // Fetch Users
    var users = new HashSet<TwitchUser>();
    for (var batch : batches) {
        IO.println("Fetching batch of " + batch.size() + " users...");
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
    IO.println("Total batches: " + batches.size());
    IO.println("Total users to fetch: " + names.size());

    // Fetch Users
    var users = new HashSet<TwitchUser>();
    for (var batch : batches) {
        IO.println("Fetching batch of " + batch.size() + " users...");
        var response = helix.getUsers(null, batch, null).execute();
        response.getUsers().forEach(user -> users.add(new TwitchUser(user)));
    }

    // Return
    return users;
}

@SuppressWarnings("unused")
private static class ResourceLoader {
    public static byte[] loadResource(String path) {
        try (var bis = new BufferedInputStream(Objects.requireNonNull(ResourceLoader.class.getResourceAsStream(path)))) {
            return bis.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + path, e);
        }
    }
}

@SuppressWarnings("unused")
private static class SQL extends Driver {

    // Constructor
    public SQL(Builder builder) {

        // Initialize Driver
        super(builder);

        // Connect to Database
        connect();

//        // Load Table Statements
//        ArrayList<String> userTable = loadTables("database/UserTable.sql");
//        ArrayList<String> channelTable = loadTables("database/ChannelTable.sql");
//        ArrayList<String> messages = loadTables("database/Messages.sql");
//        ArrayList<String> events = loadTables("database/Events.sql");
//        ArrayList<String> birthday = loadTables("database/BirthdayTable.sql");
//        ArrayList<String> lurker = loadTables("database/Lurker.sql");
//        ArrayList<String> quotes = loadTables("database/QuoteTable.sql");
//
//        // Initialize Tables
//        initTables(userTable);      // User & UserImage Tables
//        initTables(channelTable);   // Channel & Blacklist Tables               | needs UserTable
//        initTables(messages);       // Message, Response & Command Log Tables   | needs UserTable
//        initTables(events);         // Raid & Follow Table                      | needs UserTable
//        initTables(birthday);       // Birthday Table                           | needs UserTable
//        initTables(lurker);         // Lurker Table                             | needs UserTable
//        initTables(quotes);         // Quote Table                              | needs ChannelTable
    }

//    private static ArrayList<String> loadTables(String path) {
//
//        // Check Parameters
//        if (path == null || path.isBlank()) throw new IllegalArgumentException("Path cannot be null or blank");
//        var resource = de.MCmoderSD.database.Database.class.getClassLoader().getResourceAsStream(path);
//        if (resource == null) throw new IllegalArgumentException("Resource not found at path: " + path);
//
//        try (var bis = new BufferedInputStream(resource)) {
//
//            // Load Data
//            byte[] data = bis.readAllBytes();
//            String content = new String(data);
//            String[] statements = content.split(";");
//
//            // Prepare Table Names
//            ArrayList<String> tables = new ArrayList<>();
//            for (var statement : statements) {
//                if (statement == null || statement.isBlank()) continue;
//                tables.add(statement.trim() + ";");
//            }
//
//            // Return Tables
//            return tables;
//
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to load tables from path: " + path + ": " + e.getMessage(), e);
//        }
//    }
//
//    private void initTables(ArrayList<String> tables) {
//        if (tables == null || tables.isEmpty()) throw new IllegalArgumentException("Tables cannot be null or empty");
//        for (var table : tables)
//            if (table == null || table.isBlank())
//                throw new IllegalArgumentException("Table statement cannot be null or blank");
//        try {
//            for (var table : tables) {
//                PreparedStatement preparedStatement = connection.prepareStatement(table);
//                preparedStatement.executeUpdate();
//                preparedStatement.close();
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("Failed to initialize tables: " + e.getMessage(), e);
//        }
//    }

    private void checkImage(TwitchUser user, String imageUrl, UserImageType imageType) {
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

    public void addTwitchUser(TwitchUser user) {
        try {

            // Check Parameters
            if (user == null) throw new IllegalArgumentException("TwitchUser cannot be null");

            // Variables
            byte[] data = deflateObject(user);
            String profileImageUrl = user.getProfileImageUrl();
            String offlineImageUrl = user.getOfflineImageUrl();

            // Prepare the SQL statement
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO User (id, username, displayName, type, broadcasterType, user) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE username = ?, displayName = ?, type = ?, broadcasterType = ?, user = ?;"
            );

            // Set the insert values
            preparedStatement.setInt(1, user.getId());                          // Twitch User ID
            preparedStatement.setString(2, user.getUsername());                 // Twitch Username
            preparedStatement.setString(3, user.getDisplayName());              // Twitch Display Name
            preparedStatement.setString(4, user.getType().name());              // Type
            preparedStatement.setString(5, user.getBroadcasterType().name());   // Broadcaster Type
            preparedStatement.setBytes(6, data);                                // TwitchUser Object (compressed)

            // Set the update values
            preparedStatement.setString(7, user.getUsername());                 // Twitch Username
            preparedStatement.setString(8, user.getDisplayName());              // Twitch Display Name
            preparedStatement.setString(9, user.getType().name());              // Type
            preparedStatement.setString(10, user.getBroadcasterType().name());  // Broadcaster Type
            preparedStatement.setBytes(11, data);                               // TwitchUser Object (compressed)

            // Execute the statement
            preparedStatement.executeUpdate();

            // Close the statement
            preparedStatement.close();

            // Check profile image
            if (profileImageUrl != null && validUUID(profileImageUrl.substring(47, 83)))
                checkImage(user, profileImageUrl, PROFILE);
            if (offlineImageUrl != null && validUUID(offlineImageUrl.substring(47, 83)))
                checkImage(user, offlineImageUrl, OFFLINE);

        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to add or update TwitchUser with id " + user.getId() + ": " + e.getMessage(), e);
        }
    }

    public HashSet<Integer> getAllUserIds() {
        try {

            // Prepare the SQL statement
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id FROM User;"
            );

            // Execute the query
            var resultSet = preparedStatement.executeQuery();

            // Prepare result set
            HashSet<Integer> userIds = new HashSet<>();

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