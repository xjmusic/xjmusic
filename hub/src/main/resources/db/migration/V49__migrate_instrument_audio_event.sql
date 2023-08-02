/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

/*
[#178357397] Get rid of InstrumentAudioEvent in favor of metadata annotation of InstrumentAudio
*/

--- Add columns to Instrument Audio
ALTER TABLE xj.instrument_audio
  ADD COLUMN event  varchar(255) DEFAULT NULL,
  ADD COLUMN volume real         DEFAULT NULL,
  ADD COLUMN note   varchar(255) DEFAULT NULL;

--- Migrate content of instrument_audio_event records to new columns of instrument_audio
UPDATE xj.instrument_audio
SET event=subquery.name,
    volume=subquery.velocity,
    note=subquery.note
FROM (SELECT instrument_audio_id, name, velocity, note
      FROM xj.instrument_audio_event) AS subquery
WHERE xj.instrument_audio.id = subquery.instrument_audio_id;

--- Drop legacy tables
DROP TABLE xj.instrument_audio_event;
DROP TABLE xj.instrument_audio_chord;
