# Lurker Table Definition
CREATE TABLE IF NOT EXISTS Lurker (
    lurkerId    INT         PRIMARY KEY,                                # Lurker User ID
    channelId   INT         NOT NULL,                                   # Lurk Channel ID
    traitor     BIT         NOT NULL        DEFAULT FALSE,              # Is Traitor
    timestamp   TIMESTAMP   NOT NULL        DEFAULT CURRENT_TIMESTAMP,  # Lurk Timestamp
    FOREIGN KEY (lurkerId)  REFERENCES User(id) ON DELETE CASCADE,      # Foreign Key to User Table
    FOREIGN KEY (channelId) REFERENCES User(id) ON DELETE CASCADE       # Foreign Key to Channel Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4