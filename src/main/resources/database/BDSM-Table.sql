# BDSM Table
CREATE TABLE IF NOT EXISTS BDSM (
    id              VARCHAR(32) PRIMARY KEY,
    user            INT         NOT NULL,
    timestamp       TIMESTAMP   NOT NULL,
    version         INT         NOT NULL,
    gender          TEXT        NOT NULL,
    ageGroup        ENUM('<20', '20-22', '23-25', '26-30', '31-35', '36-40', '41-50', '51-60', '61-75', '>75') NOT NULL,
    data            BLOB        NOT NULL,
    ageplayer       DOUBLE      NOT NULL CHECK ( ageplayer >= 0 AND ageplayer <= 1),
    brat            DOUBLE      NOT NULL CHECK ( brat >= 0 AND brat <= 1),
    bratTamer       DOUBLE      NOT NULL CHECK ( bratTamer >= 0 AND bratTamer <= 1),
    daddyMommy      DOUBLE      NOT NULL CHECK ( daddyMommy >= 0 AND daddyMommy <= 1),
    degrader        DOUBLE      NOT NULL CHECK ( degrader >= 0 AND degrader <= 1),
    dominant        DOUBLE      NOT NULL CHECK ( dominant >= 0 AND dominant <= 1),
    degradee        DOUBLE      NOT NULL CHECK ( degradee >= 0 AND degradee <= 1),
    little          DOUBLE      NOT NULL CHECK ( little >= 0 AND little <= 1),
    masochist       DOUBLE      NOT NULL CHECK ( masochist >= 0 AND masochist <= 1),
    masterMistress  DOUBLE      NOT NULL CHECK ( masterMistress >= 0 AND masterMistress <= 1),
    nonMonogamist   DOUBLE      NOT NULL CHECK ( nonMonogamist >= 0 AND nonMonogamist <= 1),
    owner           DOUBLE      NOT NULL CHECK ( owner >= 0 AND owner <= 1),
    primalHunter    DOUBLE      NOT NULL CHECK ( primalHunter >= 0 AND primalHunter <= 1),
    pet             DOUBLE      NOT NULL CHECK ( pet >= 0 AND pet <= 1),
    primalPrey      DOUBLE      NOT NULL CHECK ( primalPrey >= 0 AND primalPrey <= 1),
    rigger          DOUBLE      NOT NULL CHECK ( rigger >= 0 AND rigger <= 1),
    ropeBunny       DOUBLE      NOT NULL CHECK ( ropeBunny >= 0 AND ropeBunny <= 1),
    sadist          DOUBLE      NOT NULL CHECK ( sadist >= 0 AND sadist <= 1),
    slave           DOUBLE      NOT NULL CHECK ( slave >= 0 AND slave <= 1),
    submissive      DOUBLE      NOT NULL CHECK ( submissive >= 0 AND submissive <= 1),
    switch          DOUBLE      NOT NULL CHECK ( switch >= 0 AND switch <= 1),
    vanilla         DOUBLE      NOT NULL CHECK ( vanilla >= 0 AND vanilla <= 1),
    voyeur          DOUBLE      NOT NULL CHECK ( voyeur >= 0 AND voyeur <= 1),
    exhibitionist   DOUBLE      NOT NULL CHECK ( exhibitionist >= 0 AND exhibitionist <= 1),
    experimentalist DOUBLE      NOT NULL CHECK ( experimentalist >= 0 AND experimentalist <= 1),
    FOREIGN KEY (user) REFERENCES User(id) ON DELETE CASCADE
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1;         # Key Block Size

# Match Cache Table
CREATE TABLE IF NOT EXISTS MatchCache(
    id      VARCHAR(32) NOT NULL,
    partner VARCHAR(32) NOT NULL,
    score   DOUBLE      NOT NULL CHECK ( score >= 0 AND score <= 1),
    data    BLOB        NOT NULL,
    PRIMARY KEY (id, partner),
    FOREIGN KEY (id) REFERENCES BDSM(id) ON DELETE CASCADE,
    FOREIGN KEY (partner) REFERENCES BDSM(id) ON DELETE CASCADE
)
    ROW_FORMAT = COMPRESSED     # Compressed Row Format
    KEY_BLOCK_SIZE = 1;         # Key Block Size