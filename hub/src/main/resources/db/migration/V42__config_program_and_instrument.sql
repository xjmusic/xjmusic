/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

ALTER TABLE xj.program
    ADD COLUMN config TEXT NOT NULL DEFAULT '';

ALTER TABLE xj.instrument
    ADD COLUMN config TEXT NOT NULL DEFAULT '';
