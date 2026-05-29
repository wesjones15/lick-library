ALTER TABLE playlist_entry DROP COLUMN bpm_offset;
ALTER TABLE playlist_entry ADD COLUMN override_tempo INTEGER;
