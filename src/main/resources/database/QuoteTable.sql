# Quote Table Definition
CREATE TABLE IF NOT EXISTS Quote (
    channelId   INT         NOT NULL,                                           # Channel ID
    id          INT         NOT NULL,                                           # Quote ID
    quote       TEXT        NOT NULL    CHECK ( char_length(quote) <= 500 ),    # Quote Text
    timestamp   TIMESTAMP   NOT NULL    DEFAULT CURRENT_TIMESTAMP,              # Timestamp
    FOREIGN KEY (channelId) REFERENCES Channel(id) ON DELETE CASCADE,           # Foreign Key to Channel Table
    UNIQUE KEY quoteEntry (channelId, id)                                       # Unique Quote Entry
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4