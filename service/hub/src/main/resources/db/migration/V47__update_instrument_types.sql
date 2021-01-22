/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */


/*
[#176474314] Artist prefers instrument types 'sticky'
*/

UPDATE xj.program_sequence_chord_voicing
SET type='Sticky'
WHERE type = 'Common';

UPDATE xj.instrument
SET type='Sticky'
WHERE type = 'Common';

UPDATE xj.program_voice
SET type='Sticky'
WHERE type = 'Common';

/*
[#176603651] Artist prefers instrument types 'pad' and 'stab'
*/

UPDATE xj.program_sequence_chord_voicing
SET type='Pad'
WHERE type = 'Harmonic';

UPDATE xj.instrument
SET type='Pad'
WHERE type = 'Harmonic';

UPDATE xj.program_voice
SET type='Pad'
WHERE type = 'Harmonic';

/*
[#176603651] Migration to insert into production database, all Pad-type program sequence chord voicings, cloned into Stab-type voicings
*/

INSERT INTO xj.program_sequence_chord_voicing (type, notes, program_id, program_sequence_chord_id)
SELECT 'Stab', notes, program_id, program_sequence_chord_id
FROM xj.program_sequence_chord_voicing
WHERE type = 'Pad';
