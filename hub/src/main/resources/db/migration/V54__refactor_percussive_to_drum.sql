/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

--
-- Percussion Loops #179534065
--
-- Refactor instrument type Percussive -> Drum
--

UPDATE xj.instrument SET type='Drum' WHERE type='Percussive';

UPDATE xj.program_voice SET type='Drum' WHERE type='Percussive';

UPDATE xj.program_sequence_chord_voicing SET type='Drum' WHERE type='Percussive';
