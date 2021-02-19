/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

/*
[#165799913] Dubbed audio can begin before segment start
  - Segment has `waveform_preroll` field in order to offset the start of the audio
*/
ALTER TABLE xj.segment
  ADD COLUMN waveform_preroll float DEFAULT 0;
