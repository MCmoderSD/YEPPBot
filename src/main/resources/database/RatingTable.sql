# Rating Table Definition
CREATE TABLE IF NOT EXISTS Rating
(
    hash    BINARY(8)   PRIMARY KEY,                                            # Content Hash
    flagged BIT         NOT NULL DEFAULT FALSE,                                 # Flagged
    rating  BLOB        NOT NULL,                                               # Rating Data (compressed)
    FOREIGN KEY (hash)   REFERENCES MessageContent (hash)    ON DELETE CASCADE  # Foreign Key to MessageContent Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1;         # Key Block Size




# RatingFlag Table Definition
CREATE TABLE IF NOT EXISTS RatingFlag
(
    hash                    BINARY(8)   PRIMARY KEY,                            # Content Hash
    harassment              BIT         NOT NULL DEFAULT FALSE,                 # Harassment
    harassmentThreatening   BIT         NOT NULL DEFAULT FALSE,                 # Harassment Threatening
    hate                    BIT         NOT NULL DEFAULT FALSE,                 # Hate
    hateThreatening         BIT         NOT NULL DEFAULT FALSE,                 # Hate Threatening
    illicit                 BIT         NOT NULL DEFAULT FALSE,                 # Illicit
    illicitViolent          BIT         NOT NULL DEFAULT FALSE,                 # Illicit Violent
    selfHarm                BIT         NOT NULL DEFAULT FALSE,                 # Self-Harm
    selfHarmInstructions    BIT         NOT NULL DEFAULT FALSE,                 # Self-Harm Instructions
    selfHarmIntent          BIT         NOT NULL DEFAULT FALSE,                 # Self-Harm Intent
    sexual                  BIT         NOT NULL DEFAULT FALSE,                 # Sexual
    sexualMinors            BIT         NOT NULL DEFAULT FALSE,                 # Sexual Minors
    violence                BIT         NOT NULL DEFAULT FALSE,                 # Violence
    violenceGraphic         BIT         NOT NULL DEFAULT FALSE,                 # Violence Graphic
    FOREIGN KEY (hash)   REFERENCES MessageContent (hash)    ON DELETE CASCADE  # Foreign Key to MessageContent Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1;         # Key Block Size




# RatingScore Table Definition
CREATE TABLE IF NOT EXISTS RatingScore
(
    hash                    BINARY(8)   PRIMARY KEY,                            # Content Hash
    harassment              DOUBLE      NOT NULL DEFAULT 0,                     # Harassment
    harassmentThreatening   DOUBLE      NOT NULL DEFAULT 0,                     # Harassment Threatening
    hate                    DOUBLE      NOT NULL DEFAULT 0,                     # Hate
    hateThreatening         DOUBLE      NOT NULL DEFAULT 0,                     # Hate Threatening
    illicit                 DOUBLE      NOT NULL DEFAULT 0,                     # Illicit
    illicitViolent          DOUBLE      NOT NULL DEFAULT 0,                     # Illicit Violent
    selfHarm                DOUBLE      NOT NULL DEFAULT 0,                     # Self-Harm
    selfHarmInstructions    DOUBLE      NOT NULL DEFAULT 0,                     # Self-Harm Instructions
    selfHarmIntent          DOUBLE      NOT NULL DEFAULT 0,                     # Self-Harm Intent
    sexual                  DOUBLE      NOT NULL DEFAULT 0,                     # Sexual
    sexualMinors            DOUBLE      NOT NULL DEFAULT 0,                     # Sexual Minors
    violence                DOUBLE      NOT NULL DEFAULT 0,                     # Violence
    violenceGraphic         DOUBLE      NOT NULL DEFAULT 0,                     # Violence Graphic
    FOREIGN KEY (hash)   REFERENCES MessageContent (hash)    ON DELETE CASCADE  # Foreign Key to MessageContent Table
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1;         # Key Block Size