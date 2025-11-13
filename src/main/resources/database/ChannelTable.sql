# Channel Table Definition
CREATE TABLE IF NOT EXISTS Channel (
    id              INT PRIMARY KEY,                        # Twitch User ID
    active          BIT NOT NULL        DEFAULT FALSE,      # Is Channel Active
    autoShoutout    BIT NOT NULL        DEFAULT FALSE,      # Is Auto Shoutout Enabled
    FOREIGN KEY (id) REFERENCES User(id) ON DELETE CASCADE  # Foreign Key to User Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4




# Blacklist Table Definition
CREATE TABLE IF NOT EXISTS Blacklist (
    id      INT     NOT NULL,                                           # Channel ID
    command TEXT    NOT NULL    CHECK ( char_length(command) <= 500 ),  # Blacklisted Command
    FOREIGN KEY (id) REFERENCES Channel(id) ON DELETE CASCADE,          # Foreign Key to Channel Table
    UNIQUE KEY blacklistEntry (id, command)                             # Unique Blacklist Entry
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4