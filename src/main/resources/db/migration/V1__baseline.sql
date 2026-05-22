-- Baseline: schema as of Phase 1 start (Postgres syntax).
-- Skipped on existing databases via baseline-on-migrate=true.
-- Applied on new/fresh environments to establish starting state.

CREATE TABLE IF NOT EXISTS lick (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    interval_hash   VARCHAR(64)  NOT NULL UNIQUE,
    auto_imported   BOOLEAN      NOT NULL DEFAULT FALSE,
    intervals       TEXT         NOT NULL,
    raw_tab         TEXT,
    mode            VARCHAR(16),
    tab_span        INTEGER,
    instrument      VARCHAR(16),
    endpoint_degree VARCHAR(16),
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS song (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR      NOT NULL,
    artist          VARCHAR,
    original_key    VARCHAR(20),
    mode            VARCHAR(20),
    instrument      VARCHAR(20),
    capo            INTEGER,
    tempo           INTEGER,
    chord_lines     TEXT         NOT NULL,
    num_columns     INTEGER      DEFAULT 2,
    raw_chord_sheet TEXT,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS song_lick (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    song_id     UUID    NOT NULL,
    tab_order   INTEGER NOT NULL,
    lick_id     UUID,
    raw_tab     TEXT    NOT NULL,
    UNIQUE (song_id, tab_order)
);

CREATE TABLE IF NOT EXISTS song_beatmap (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    song_id UUID NOT NULL UNIQUE,
    beats   TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS chord_quality (
    id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    suffix VARCHAR NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS chord_shape (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chord_quality_id UUID    NOT NULL REFERENCES chord_quality(id),
    shape_name       VARCHAR,
    template_frets   TEXT    NOT NULL,
    root_string      INTEGER,
    source           VARCHAR,
    label            VARCHAR,
    instrument       VARCHAR
);

CREATE TABLE IF NOT EXISTS playlist (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR   NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS playlist_entry (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    playlist_id         UUID    NOT NULL REFERENCES playlist(id),
    song_id             UUID    NOT NULL,
    position            INTEGER NOT NULL,
    override_semitones  INTEGER,
    override_capo       INTEGER,
    override_instrument VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS position_cache (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    interval_hash  VARCHAR(64) NOT NULL,
    note_key       VARCHAR(8)  NOT NULL,
    positions_json TEXT        NOT NULL,
    UNIQUE (interval_hash, note_key)
);
