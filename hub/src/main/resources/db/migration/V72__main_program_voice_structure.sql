/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

-- Programs persist main chord/voicing structure sensibly
-- https://www.pivotaltracker.com/story/show/182220689


-- Part 1. alter sequence chord voicings to belong to program voice (null allowed)
ALTER TABLE xj.program_sequence_chord_voicing
  ADD COLUMN program_voice_id UUID REFERENCES xj.program_voice (id);



-- Part 2. create voices for each and assign all voicings belonging-to their new voice
-- 2a. create voice and store inserted id
-- 2b. assign all voicings belonging-to their new voice
DO
$$
  DECLARE
    program_id_arr uuid ARRAY;
    op_program_id  uuid;
    voice_type_arr xj.instrument_type ARRAY;
    op_voice_type  xj.instrument_type;
    inserted_id    UUID;
  BEGIN
    SELECT ARRAY(SELECT id FROM xj.program WHERE type = 'Main') INTO program_id_arr;
    FOREACH op_program_id IN ARRAY program_id_arr
      LOOP
        SELECT ARRAY(SELECT DISTINCT type
                     FROM xj.program_sequence_chord_voicing
                     WHERE xj.program_sequence_chord_voicing.program_id = op_program_id)
        INTO voice_type_arr;
        FOREACH op_voice_type IN ARRAY voice_type_arr
          LOOP
            INSERT INTO xj.program_voice(program_id, type, name)
            VALUES (op_program_id, op_voice_type, op_voice_type)
            RETURNING id into inserted_id;

            UPDATE xj.program_sequence_chord_voicing
            SET program_voice_id = inserted_id
            WHERE program_id = op_program_id
              AND xj.program_sequence_chord_voicing.type = op_voice_type;

          END LOOP;
      END LOOP;
  END;
$$ LANGUAGE plpgsql;


-- Part 3. delete voicings that still have no program voice id
DELETE
FROM xj.program_sequence_chord_voicing
WHERE program_voice_id IS NULL;


-- Part 3. alter sequence chord voicings to have no type, and require belonging-to voice
ALTER TABLE xj.program_sequence_chord_voicing
  ALTER COLUMN program_voice_id SET NOT NULL,
  DROP COLUMN type;

