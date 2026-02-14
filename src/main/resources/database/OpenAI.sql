# Conversation Table Definition
CREATE TABLE IF NOT EXISTS Conversation (
    userId      INT         PRIMARY KEY,                            # User ID
    response    TEXT        NOT NULL,                               # Response ID
    FOREIGN KEY (userId)    REFERENCES User(id) ON DELETE CASCADE   # Foreign Key to User Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4