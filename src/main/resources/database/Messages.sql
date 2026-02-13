# MessagesContent Table Definition
CREATE TABLE IF NOT EXISTS MessageContent
(
    hash    BINARY(8) PRIMARY KEY,                                          # Content Hash
    content TEXT UNIQUE NOT NULL CHECK ( char_length(content) <= 500 )      # Message Content
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4




# Embedding Table Definition
CREATE TABLE IF NOT EXISTS Embedding
(
    hash        BINARY(8)   PRIMARY KEY,                                        # Content Hash
    dimension   SMALLINT    NOT NULL        DEFAULT 3072,                       # Embedding Dimension
    embedding   BLOB        NOT NULL,                                           # Embedding Data (compressed)
    FOREIGN KEY (hash)   REFERENCES MessageContent (hash)    ON DELETE CASCADE  # Foreign Key to MessageContent Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1;         # Key Block Size




# MessageEvent Table Definition
CREATE TABLE IF NOT EXISTS MessageEvent
(
    id                  UUID PRIMARY KEY,                                                                           # Event ID
    firedAt             TIMESTAMP                                           NOT NULL DEFAULT CURRENT_TIMESTAMP,     # Event fired at Timestamp
    channelId           INT                                                 NOT NULL,                               # Channel ID
    userId              INT                                                 NOT NULL,                               # User ID
    content             BINARY(8)                                           NOT NULL,                               # Message Content Hash
    deviceType          ENUM ('WEB', 'IOS', 'ANDROID', 'UNKNOWN')           NOT NULL DEFAULT 'UNKNOWN',             # Device Type
    subTier             ENUM ('NONE', 'TIER1', 'TIER2', 'TIER3', 'PRIME')   NOT NULL DEFAULT 'NONE',                # Subscription Tier
    subMonths           INT                                                 NOT NULL DEFAULT 0,                     # Subscription Months
    action              BIT                                                 NOT NULL DEFAULT FALSE,                 # Action Message (/me)
    highlighted         BIT                                                 NOT NULL DEFAULT FALSE,                 # Highlighted Message
    firstMessage        BIT                                                 NOT NULL DEFAULT FALSE,                 # First Message
    userIntroduction    BIT                                                 NOT NULL DEFAULT FALSE,                 # User Introduction
    skipSubsModeMessage BIT                                                 NOT NULL DEFAULT FALSE,                 # Skip Subs Mode Message
    event               BLOB                UNIQUE                          NOT NULL,                               # Full Event Data (compressed)
    FOREIGN KEY (channelId) REFERENCES User (id)                ON DELETE CASCADE,                                  # Foreign Key to User Table (Channel)
    FOREIGN KEY (userId)    REFERENCES User (id)                ON DELETE CASCADE,                                  # Foreign Key to User Table (User)
    FOREIGN KEY (content)   REFERENCES MessageContent (hash)    ON DELETE CASCADE                                   # Foreign Key to MessageContent Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1;         # Key Block Size




# ResponseMessage Table Definition
CREATE TABLE IF NOT EXISTS ResponseMessage
(
    id          UUID        PRIMARY KEY,                                            # Response ID
    firedAt     TIMESTAMP   NOT NULL        DEFAULT CURRENT_TIMESTAMP,              # Timestamp
    channelId   INT         NOT NULL,                                               # Channel ID
    userId      INT         NOT NULL,                                               # User ID
    command     TEXT        NOT NULL        CHECK ( char_length(command) <= 500 ),  # Command that triggered the response
    content     BINARY(8)   NOT NULL,                                               # Response Content Hash
    messageId   UUID        NOT NULL,                                               # Message Event ID
    FOREIGN KEY (channelId) REFERENCES User (id)                ON DELETE CASCADE,  # Foreign Key to User Table (Channel)
    FOREIGN KEY (userId)    REFERENCES User (id)                ON DELETE CASCADE,  # Foreign Key to User Table (User)
    FOREIGN KEY (content)   REFERENCES MessageContent (hash)    ON DELETE CASCADE,  # Foreign Key to MessageContent Table
    FOREIGN KEY (messageId) REFERENCES MessageEvent (id)        ON DELETE CASCADE   # Foreign Key to MessageEvent Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4




# CommandLog Table Definition
CREATE TABLE IF NOT EXISTS CommandLog
(
    messageId   UUID        NOT NULL,                                               # Original Message ID
    firedAt     TIMESTAMP   NOT NULL        DEFAULT CURRENT_TIMESTAMP,              # Timestamp
    channelId   INT         NOT NULL,                                               # Channel ID
    userId      INT         NOT NULL,                                               # User ID
    command     TEXT        NOT NULL        CHECK ( char_length(command) <= 500 ),  # Command that triggered the response
    args        BINARY(8)   NOT NULL,                                               # Command arguments content hash
    FOREIGN KEY (messageId) REFERENCES MessageEvent (id)        ON DELETE CASCADE,  # Foreign Key to MessageEvent Table
    FOREIGN KEY (channelId) REFERENCES User (id)                ON DELETE CASCADE,  # Foreign Key to User Table (Channel)
    FOREIGN KEY (userId)    REFERENCES User (id)                ON DELETE CASCADE,  # Foreign Key to User Table (User)
    FOREIGN KEY (args)      REFERENCES MessageContent (hash)    ON DELETE CASCADE   # Foreign Key to MessageContent Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4