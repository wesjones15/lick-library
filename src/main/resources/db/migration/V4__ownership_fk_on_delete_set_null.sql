-- Allow user deletion without cascading to content (content survives with user_id nulled)

ALTER TABLE lick        ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE song        ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE chord_shape ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE playlist    ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE lick        DROP CONSTRAINT fk_lick_user;
ALTER TABLE lick        ADD  CONSTRAINT fk_lick_user        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE song        DROP CONSTRAINT fk_song_user;
ALTER TABLE song        ADD  CONSTRAINT fk_song_user        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE chord_shape DROP CONSTRAINT fk_chord_shape_user;
ALTER TABLE chord_shape ADD  CONSTRAINT fk_chord_shape_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE playlist    DROP CONSTRAINT fk_playlist_user;
ALTER TABLE playlist    ADD  CONSTRAINT fk_playlist_user    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
