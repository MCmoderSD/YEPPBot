# Queue Table Definition
CREATE TABLE IF NOT EXISTS Queue (
    userId      INT         NOT NULL,                                   # User ID
    position    INT         NOT NULL,                                   # Queue Position
    joinedAt    TIMESTAMP   NOT NULL    DEFAULT CURRENT_TIMESTAMP,      # Joined At Timestamp
    channelId   INT         NOT NULL,                                   # Channel ID
    FOREIGN KEY (userId)    REFERENCES User(id) ON DELETE CASCADE,      # Foreign Key to User Table
    FOREIGN KEY (channelId) REFERENCES Channel(id) ON DELETE CASCADE,   # Foreign Key to Channel Table
    UNIQUE KEY positionEntry (position, channelId),                     # Unique Position Entry
    UNIQUE KEY queueEntry (userId, channelId)                           # Unique User Queue Entry
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4