/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

--
-- Use "tones" attribute (instead of note) for instrument audios and events
-- https://www.pivotaltracker.com/story/show/181801064
--

ALTER TABLE xj.instrument_audio
  RENAME COLUMN note TO tones;

ALTER TABLE xj.program_sequence_pattern_event
  RENAME COLUMN note TO tones;

