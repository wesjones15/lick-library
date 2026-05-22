-- Add user_id nullable first, backfill, then enforce NOT NULL

ALTER TABLE lick        ADD COLUMN user_id    BIGINT;
ALTER TABLE song        ADD COLUMN user_id    BIGINT;
ALTER TABLE chord_shape ADD COLUMN user_id    BIGINT;
ALTER TABLE chord_shape ADD COLUMN created_at TIMESTAMP;
ALTER TABLE playlist    ADD COLUMN user_id    BIGINT;
ALTER TABLE playlist    ADD COLUMN is_public  BOOLEAN NOT NULL DEFAULT TRUE;

-- Backfill all existing rows → admin (id=1)
UPDATE lick        SET user_id = 1                                  WHERE user_id IS NULL;
UPDATE song        SET user_id = 1                                  WHERE user_id IS NULL;
UPDATE chord_shape SET user_id = 1, created_at = CURRENT_TIMESTAMP  WHERE user_id IS NULL;
UPDATE playlist    SET user_id = 1                                  WHERE user_id IS NULL;

-- Enforce NOT NULL
ALTER TABLE lick        ALTER COLUMN user_id    SET NOT NULL;
ALTER TABLE song        ALTER COLUMN user_id    SET NOT NULL;
ALTER TABLE chord_shape ALTER COLUMN user_id    SET NOT NULL;
ALTER TABLE chord_shape ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE playlist    ALTER COLUMN user_id    SET NOT NULL;

-- Foreign key constraints
ALTER TABLE lick        ADD CONSTRAINT fk_lick_user        FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE song        ADD CONSTRAINT fk_song_user        FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE chord_shape ADD CONSTRAINT fk_chord_shape_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE playlist    ADD CONSTRAINT fk_playlist_user    FOREIGN KEY (user_id) REFERENCES users(id);
