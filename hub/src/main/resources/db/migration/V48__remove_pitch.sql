/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */


/*
[#176770518] For Harmonic and Melodic Audios, Pitch should be automatically filled from a table of pitches-to-frequencies
*/

ALTER TABLE xj.instrument_audio
  DROP COLUMN pitch;
