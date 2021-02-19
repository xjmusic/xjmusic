/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */


ALTER TABLE xj.account
    DROP COLUMN created_at;
ALTER TABLE xj.account
    DROP COLUMN updated_at;

ALTER TABLE xj.account_user
    DROP COLUMN created_at;
ALTER TABLE xj.account_user
    DROP COLUMN updated_at;

ALTER TABLE xj.instrument
    DROP COLUMN created_at;
ALTER TABLE xj.instrument
    DROP COLUMN updated_at;

ALTER TABLE xj.instrument_audio
    DROP COLUMN created_at;
ALTER TABLE xj.instrument_audio
    DROP COLUMN updated_at;

ALTER TABLE xj.instrument_audio_chord
    DROP COLUMN created_at;
ALTER TABLE xj.instrument_audio_chord
    DROP COLUMN updated_at;

ALTER TABLE xj.instrument_audio_event
    DROP COLUMN created_at;
ALTER TABLE xj.instrument_audio_event
    DROP COLUMN updated_at;

ALTER TABLE xj.instrument_meme
    DROP COLUMN created_at;
ALTER TABLE xj.instrument_meme
    DROP COLUMN updated_at;

ALTER TABLE xj.library
    DROP COLUMN created_at;
ALTER TABLE xj.library
    DROP COLUMN updated_at;

ALTER TABLE xj.program
    DROP COLUMN created_at;
ALTER TABLE xj.program
    DROP COLUMN updated_at;

ALTER TABLE xj.program_meme
    DROP COLUMN created_at;
ALTER TABLE xj.program_meme
    DROP COLUMN updated_at;

ALTER TABLE xj.program_sequence
    DROP COLUMN created_at;
ALTER TABLE xj.program_sequence
    DROP COLUMN updated_at;

ALTER TABLE xj.program_sequence_binding
    DROP COLUMN created_at;
ALTER TABLE xj.program_sequence_binding
    DROP COLUMN updated_at;

ALTER TABLE xj.program_sequence_binding_meme
    DROP COLUMN created_at;
ALTER TABLE xj.program_sequence_binding_meme
    DROP COLUMN updated_at;

ALTER TABLE xj.program_sequence_chord
    DROP COLUMN created_at;
ALTER TABLE xj.program_sequence_chord
    DROP COLUMN updated_at;

ALTER TABLE xj.program_sequence_chord_voicing
    DROP COLUMN created_at;
ALTER TABLE xj.program_sequence_chord_voicing
    DROP COLUMN updated_at;

ALTER TABLE xj.program_sequence_pattern
    DROP COLUMN created_at;
ALTER TABLE xj.program_sequence_pattern
    DROP COLUMN updated_at;

ALTER TABLE xj.program_sequence_pattern_event
    DROP COLUMN created_at;
ALTER TABLE xj.program_sequence_pattern_event
    DROP COLUMN updated_at;

ALTER TABLE xj.program_voice
    DROP COLUMN created_at;
ALTER TABLE xj.program_voice
    DROP COLUMN updated_at;

ALTER TABLE xj.program_voice_track
    DROP COLUMN created_at;
ALTER TABLE xj.program_voice_track
    DROP COLUMN updated_at;

ALTER TABLE xj.user
    DROP COLUMN created_at;
ALTER TABLE xj.user
    DROP COLUMN updated_at;

ALTER TABLE xj.user_auth
    DROP COLUMN created_at;
ALTER TABLE xj.user_auth
    DROP COLUMN updated_at;

ALTER TABLE xj.user_auth_token
    DROP COLUMN created_at;
ALTER TABLE xj.user_auth_token
    DROP COLUMN updated_at;

ALTER TABLE xj.user_role
    DROP COLUMN created_at;
ALTER TABLE xj.user_role
    DROP COLUMN updated_at;

