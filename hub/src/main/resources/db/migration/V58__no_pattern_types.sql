/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

/* Program Sequence Pattern has no type (all are loops) #180059740 */

DELETE
FROM xj.program_sequence_pattern_event
WHERE xj.program_sequence_pattern_event.id IN (SELECT xj.program_sequence_pattern_event.id
                                               FROM xj.program_sequence_pattern_event
                                                      JOIN xj.program_sequence_pattern
                                                           ON xj.program_sequence_pattern.id =
                                                              xj.program_sequence_pattern_event.program_sequence_pattern_id
                                               WHERE xj.program_sequence_pattern.type NOT IN ('Loop'));

DELETE
FROM xj.program_sequence_pattern
WHERE xj.program_sequence_pattern.id IN (SELECT xj.program_sequence_pattern.id
                                         FROM xj.program_sequence_pattern
                                         WHERE xj.program_sequence_pattern.type NOT IN ('Loop'));

ALTER TABLE xj.program_sequence_pattern
  DROP COLUMN type;

DROP TYPE xj.program_sequence_pattern_type;
