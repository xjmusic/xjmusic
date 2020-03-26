// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.ingest;

import io.xj.service.hub.HubException;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;

import java.util.List;
import java.util.Objects;

/**
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 */
public enum IngestState {
  Done;

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValues() {
    return Text.toStrings(values());
  }

  /**
   cast string to enum

   @param value to cast to enum
   @return enum
   @throws HubException on failure
   */
  public static IngestState validate(String value) throws HubException {
    if (Objects.isNull(value))
      return Done;

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new HubException("'" + value + "' is not a valid state (" + CSV.joinEnum(values()) + ").", e);
    }
  }

}
