# RaidEvent table to store raid events
CREATE TABLE IF NOT EXISTS RaidEvent (
    id          UUID        PRIMARY KEY,                                # Event ID
    firedAt     TIMESTAMP   NOT NULL        DEFAULT CURRENT_TIMESTAMP,  # Event fired at Timestamp
    channelId   INT         NOT NULL,                                   # Channel (target)  (User ID)
    userId      INT         NOT NULL,                                   # Raider  (source)  (User ID)
    viewers     INT         NOT NULL        DEFAULT 0,                  # Raid viewers count
    event       BLOB        NOT NULL        UNIQUE,                     # Full event data (compressed)
    FOREIGN KEY (channelId) REFERENCES User(id) ON DELETE CASCADE,      # Foreign Key to User Table (Channel)
    FOREIGN KEY (userId)    REFERENCES User(id) ON DELETE CASCADE       # Foreign Key to User Table (Raider)
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1;         # Key Block Size




# FollowEvent table to store follow events
CREATE TABLE IF NOT EXISTS FollowEvent (
    followedAt  TIMESTAMP   NOT NULL    DEFAULT CURRENT_TIMESTAMP,  # Event fired at Timestamp
    channelId   INT         NOT NULL,                               # Channel   (User ID)
    userId      INT         NOT NULL,                               # Follower  (User ID)
    event       BLOB        NOT NULL    UNIQUE ,                    # Full event data (compressed)
    FOREIGN KEY (channelId) REFERENCES User(id) ON DELETE CASCADE,  # Foreign Key to User Table (Channel)
    FOREIGN KEY (userId)    REFERENCES User(id) ON DELETE CASCADE   # Foreign Key to User Table (Follower)
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1;         # Key Block Size