/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

-- Simpler Instrument type/mode scheme
-- https://www.pivotaltracker.com/story/show/182354708

-- Before: change instrument mode and type to text columns
ALTER TABLE xj.instrument
  ALTER mode TYPE TEXT;
ALTER TABLE xj.instrument
  ALTER type TYPE TEXT;
ALTER TABLE xj.program_voice
  ALTER type TYPE TEXT;

-- Drop old enum types
DROP TYPE xj.instrument_mode CASCADE;
DROP TYPE xj.instrument_type CASCADE;

-- Instrument rename PercLoop -> Percussion
UPDATE xj.instrument
SET type='Percussion'
WHERE type = 'PercLoop';

-- instrument update modes to Event
UPDATE xj.instrument
SET mode='Event'
WHERE mode IN ('NoteEvent', 'VoicingEvent', 'ChordEvent');

-- Instrument update modes to Chord
UPDATE xj.instrument
SET mode='Chord'
WHERE mode IN ('VoicingPart', 'ChordPart', 'MainPart');

-- Update instrument modes to Loop
UPDATE xj.instrument
SET mode='Loop'
WHERE mode = 'VoicingLoop';

-- Instrument Noise-type Background-mode (formerly background-type)
UPDATE xj.instrument
SET type='Noise',
    mode='Background'
WHERE type = 'Background';

-- Instrument Sweep-type Transition-mode (formerly transition-type)
UPDATE xj.instrument
SET type='Sweep',
    mode='Transition'
WHERE type = 'Transition';

-- Instrument rename PercLoop -> Percussion
UPDATE xj.program_voice
SET type='Percussion'
WHERE type = 'PercLoop';

-- Instrument rename PercLoop -> Percussion
UPDATE xj.program_voice
SET type='Noise'
WHERE type NOT IN (
                   'Bass',
                   'Drum',
                   'Hook',
                   'Noise',
                   'Pad',
                   'Percussion',
                   'Stab',
                   'Sticky',
                   'Stripe',
                   'Sweep'
  );

-- Create new enum types
CREATE TYPE xj.instrument_mode AS ENUM (
  'Event',
  'Chord',
  'Loop',
  'Transition',
  'Background'
  );
CREATE TYPE xj.instrument_type AS ENUM (
  'Bass',
  'Drum',
  'Hook',
  'Noise',
  'Pad',
  'Percussion',
  'Stab',
  'Sticky',
  'Stripe',
  'Sweep'
  );

-- After: change instrument mode and type back to enum columns
ALTER TABLE xj.instrument
  ALTER mode TYPE xj.instrument_mode using mode::xj.instrument_mode;
ALTER TABLE xj.instrument
  ALTER type TYPE xj.instrument_type using type::xj.instrument_type;
ALTER TABLE xj.program_voice
  ALTER type TYPE xj.instrument_type using type::xj.instrument_type;
