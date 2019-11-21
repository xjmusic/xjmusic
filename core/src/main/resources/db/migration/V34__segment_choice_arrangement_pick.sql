/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.segment_choice_arrangement_pick
(
  id                                UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  segment_id                        UUID         NOT NULL REFERENCES xj.segment (id),
  segment_choice_arrangement_id            UUID         NOT NULL REFERENCES xj.segment_choice_arrangement (id),
  instrument_audio_id               UUID         NOT NULL REFERENCES xj.instrument_audio (id),
  program_sequence_pattern_event_id UUID         NOT NULL REFERENCES xj.program_sequence_pattern_event (id),
  start                             real         not null,
  length                            real         not null,
  amplitude                         real         not null,
  pitch                             real         not null,
  name                              varchar(255) not null,
  created_at                        timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                        timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER segment_choice_arrangement_pick___updated
  BEFORE UPDATE
  ON xj.segment_choice_arrangement_pick
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
