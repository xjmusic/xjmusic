--  Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
--
-- [#173912302] Hub persists Voice and Track order
--   - Hub persists `order` field for **ProgramVoice** and **ProgramVoiceTrack**
--   - Hub return **ProgramVoice** and **ProgramVoiceTrack** in order by `order` (not alphabetical order)
--   - Hub enables modification of `order` for a **ProgramVoice** and **ProgramVoiceTrack**
--

ALTER TABLE xj.program_voice
    ADD COLUMN "order" float DEFAULT 1000.0;

CREATE INDEX ON xj.program_voice ("order");

ALTER TABLE xj.program_voice_track
    ADD COLUMN "order" float DEFAULT 1000.0;

CREATE INDEX ON xj.program_voice_track ("order");
