# Birthday
CREATE TABLE IF NOT EXISTS Birthday (
    id          INT         PRIMARY KEY,                                            # User ID
    day         TINYINT     NOT NULL        CHECK (day >= 1 AND day <= 31),         # Day of Birth
    month       TINYINT     NOT NULL        CHECK (month >= 1 AND month <= 12),     # Month of Birth
    year        SMALLINT    NOT NULL        CHECK (year >= 1900 AND year <= 24576), # Year of Birth
    FOREIGN KEY (id) REFERENCES User(id) ON DELETE CASCADE                          # Foreign Key to User Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1          # Key Block Size