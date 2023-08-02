/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

/* Percussive Loops can specify # beats in the instrument / audio #179959089 */

ALTER TABLE xj.instrument_audio
  RENAME COLUMN start TO transient_seconds;

ALTER TABLE xj.instrument_audio
  RENAME COLUMN length TO total_beats;

UPDATE xj.instrument_audio
SET total_beats = 1
WHERE instrument_id IN (SELECT id
                        FROM xj.instrument
                        WHERE type != 'PercLoop');

UPDATE xj.instrument_audio
SET total_beats = 4
WHERE instrument_id IN (SELECT id
                        FROM xj.instrument
                        WHERE type = 'PercLoop');
