/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

/* Trigger to update updated_at column */
CREATE OR REPLACE FUNCTION xj.updated_at_now()
  RETURNS TRIGGER AS
$$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ language 'plpgsql';
