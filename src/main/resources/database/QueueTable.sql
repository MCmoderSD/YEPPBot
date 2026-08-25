# Queue Table Definition
CREATE TABLE IF NOT EXISTS Queue (
    id          INT                                                 NOT NULL,                       # Channel ID
    isOpen      BIT                                                 NOT NULL    DEFAULT FALSE,      # Accepts !queue join
    requirement ENUM ('everyone', 'follower', 'subscriber', 'vip')  NOT NULL    DEFAULT 'everyone', # Who may join (checked by the bot, set by the dashboard)
    queue       TEXT                                                NOT NULL    DEFAULT (''),       # The waiting list: user IDs, comma separated, in order
    updatedAt   DATETIME(3)                                         NOT NULL    DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),                                                                               # One Queue Entry per Channel
    FOREIGN KEY (id) REFERENCES Channel(id) ON DELETE CASCADE                                       # Foreign Key to Channel Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size
    CHARACTER SET = utf8mb4     # UTF-8 MB4 Character Set
    COLLATE utf8mb4_bin;        # Binary Collation for utf8mb4