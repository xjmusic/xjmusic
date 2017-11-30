// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.timestamp;

import io.xj.core.util.Text;

import com.google.common.base.Objects;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

public interface TimestampUTC {
  String ZONE_ID = "UTC";

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
   @throws Exception on failure
   */
  static Timestamp valueOf(String value) throws Exception {
    if (null != value && !value.isEmpty()) {
      if (Objects.equal("now", Text.toLowerSlug(value))) {
        return now();
      } else {
        return Timestamp.valueOf(value);
      }
    }
    return null;
  }

}
