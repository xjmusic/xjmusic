// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.util;

import io.xj.core.exception.CoreException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.regex.Pattern;

public interface TimestampUTC {
  String ZONE_ID = "UTC";
  Pattern TIMESTAMP_TRAILING_Z = Pattern.compile("Z$");

  /**
   Timestamp for now, UTC

   @return timestamp
   */
  static Timestamp now() {
    return Timestamp.valueOf(LocalDateTime.now(ZoneId.of(ZONE_ID)));
  }

  /**
   Timestamp for now (delta # seconds) UTC

   @param plusSeconds ahead valueOf now
   @return timestamp
   */
  static Timestamp nowPlusSeconds(long plusSeconds) {
    return Timestamp.valueOf(LocalDateTime.now(ZoneId.of(ZONE_ID)).plusSeconds(plusSeconds));
  }

  /**
   Timestamp for now (delta # seconds) UTC

   @param minusSeconds before now
   @return timestamp
   */
  static Timestamp nowMinusSeconds(long minusSeconds) {
    return Timestamp.valueOf(LocalDateTime.now(ZoneId.of(ZONE_ID)).minusSeconds(minusSeconds));
  }

  /**
   Get timestamp valueOf string value
   [#150279615] For any timestamp field, enter "now" for current timestamp utc

   @param value string
   @return timestamp
   */
  static Timestamp valueOf(String value) throws CoreException {
    if (Objects.isNull(value) || value.isEmpty())
      throw new CoreException("Can't parse null value for timestamp UTC");

    if (Objects.equals("now", Text.toLowerSlug(value)))
      return now();

    try {
      return Timestamp.valueOf(TIMESTAMP_TRAILING_Z.matcher(value).replaceAll(""));

    } catch (IllegalArgumentException e) {
      throw new CoreException(String.format("Bad value for timestamp UTC: %s", value), e);
    }
  }

}
