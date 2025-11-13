# Lurker Table Definition
CREATE TABLE IF NOT EXISTS Lurker (
    eventId     UUID    PRIMARY KEY,                                        # Event ID
    lurkerId    INT     NOT NULL,                                           # Lurker User ID
    traitor     BIT     NOT NULL        DEFAULT FALSE,                      # Is Traitor
    FOREIGN KEY (eventId) REFERENCES MessageEvent(id) ON DELETE CASCADE,    # Foreign Key to MessageEvent Table
    FOREIGN KEY (lurkerId) REFERENCES User(id) ON DELETE CASCADE            # Foreign Key to User Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4