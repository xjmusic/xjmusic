/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

--
-- Instruments have InstrumentMode #181134085
--

ALTER TABLE xj.instrument
  DROP COLUMN mode;

DROP TYPE instrument_mode;

CREATE TYPE instrument_mode AS ENUM (
  'NoteEvent', -- Multiple note-based audios to fulfill each multi-note event of detail program
  'VoicingEvent', -- One multi-note-based audio to fulfill each multi-note event of detail program
  'ChordEvent', -- One chord-based audio to fulfill each chord event of detail program
  'VoicingPart', -- One multi-note-based audio to fulfill each main program chord voicing
  'ChordPart', -- One chord-based audio to fulfill each main program chord
  'MainPart' -- One audio based directly on a main program sequence
  );

ALTER TABLE xj.instrument
  ADD COLUMN mode instrument_mode DEFAULT 'NoteEvent';

