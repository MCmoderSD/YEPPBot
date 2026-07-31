#  Custom Commands Table
CREATE TABLE IF NOT EXISTS CustomCommands (
    id          INT     NOT NULL,                                                       # Channel ID
    name        TEXT    NOT NULL                CHECK ( char_length(name) <= 500 ),     # Command Name
    alias       TEXT    NOT NULL    DEFAULT ''  CHECK ( char_length(alias) <= 500 ),    # Command Alias
    message     TEXT    NOT NULL                CHECK ( char_length(message) <= 500 ),  # Command Message
    active      BIT     NOT NULL    DEFAULT TRUE,                                       # Is Command Active
    type        ENUM('reply', 'mention', 'say') NOT NULL DEFAULT 'reply',               # Command Type
    userLevel   ENUM('everyone', 'follower', 'editor', 'vip', 'moderator', 'broadcaster') NOT NULL DEFAULT 'everyone',
    UNIQUE KEY (id, name),                                  # Unique Command Entry
    FOREIGN KEY (id) REFERENCES User(id) ON DELETE CASCADE  # Foreign Key to Channel Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4