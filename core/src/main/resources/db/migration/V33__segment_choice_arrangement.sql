/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.segment_choice_arrangement
(
  id                UUID PRIMARY KEY DEFAULT uuid_generate_v1mc(),
  segment_id        UUID      NOT NULL REFERENCES xj.segment (id),
  segment_choice_id UUID      NOT NULL REFERENCES xj.segment_choice (id),
  program_voice_id  UUID      NOT NULL REFERENCES xj.program_voice (id),
  instrument_id     UUID      NOT NULL REFERENCES xj.instrument (id),
  created_at        timestamp NULL   DEFAULT CURRENT_TIMESTAMP,
  updated_at        timestamp NULL   DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER segment_choice_arrangement___updated
  BEFORE UPDATE
  ON xj.segment_choice_arrangement
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
