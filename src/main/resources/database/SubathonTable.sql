# Subathon Timer Table Definition
CREATE TABLE IF NOT EXISTS SubathonTimer (
    id              INT             NOT NULL,                                                                   # Channel ID
    running         BIT             NOT NULL    DEFAULT FALSE,                                                  # Is Timer Running (decides which of the next two columns holds the truth)
    endsAt          DATETIME(3)         NULL    DEFAULT NULL,                                                   # End Timestamp in UTC (only valid while running)
    remaining       INT             NOT NULL    DEFAULT 0,                                                      # Remaining Seconds (only valid while paused)
    startSeconds    INT             NOT NULL    DEFAULT 0,                                                      # Reset Value (written by the dashboard only)
    style           TEXT            NOT NULL    DEFAULT (''),                                                   # Overlay Style as JSON (written by the dashboard only)
    updatedAt       DATETIME(3)     NOT NULL    DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),    # Change Marker (maintained by MariaDB, never written by the bot)
    PRIMARY KEY (id),                                                                                           # One-Timer Entry per Channel
    FOREIGN KEY (id) REFERENCES Channel(id) ON DELETE CASCADE                                                   # Foreign Key to Channel Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin         # Binary Collation for utf8mb4