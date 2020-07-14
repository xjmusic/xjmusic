// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.ingest;

import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;

import java.util.List;
import java.util.Objects;

/**
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 [#154350346] Architect wants a universal HubIngest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 */
public enum HubIngestState {
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
   @throws HubIngestException on failure
   */
  public static HubIngestState validate(String value) throws HubIngestException {
    if (Objects.isNull(value))
      return Done;

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new HubIngestException("'" + value + "' is not a valid state (" + CSV.joinEnum(values()) + ").", e);
    }
  }

}
