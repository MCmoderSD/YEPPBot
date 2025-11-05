# User table to store user information
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





# ProfileImage table to store user profile images
CREATE TABLE IF NOT EXISTS ProfileImage (
    uuid            UUID        PRIMARY KEY,                    # Image UUID
    id              INT                         NOT NULL,       # User ID
    url             TEXT        UNIQUE          NOT NULL,       # Image URL
    size            INT                         NOT NULL,       # Compressed Size
    uncompressed    INT                         NOT NULL,       # Uncompressed Size
    image           LONGBLOB    UNIQUE          NOT NULL,       # Image Data (compressed)
    FOREIGN KEY (id) REFERENCES User(id) ON DELETE CASCADE      # Foreign Key to User Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4





# OfflineImage table to store user offline images
CREATE TABLE IF NOT EXISTS OfflineImage (
    uuid            UUID        PRIMARY KEY,                    # Image UUID
    id              INT                         NOT NULL,       # User ID
    url             TEXT        UNIQUE          NOT NULL,       # Image URL
    size            INT                         NOT NULL,       # Compressed Size
    uncompressed    INT                         NOT NULL,       # Uncompressed Size
    image           LONGBLOB    UNIQUE          NOT NULL,       # Image Data (compressed)
    FOREIGN KEY (id) REFERENCES User(id) ON DELETE CASCADE      # Foreign Key to User Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4