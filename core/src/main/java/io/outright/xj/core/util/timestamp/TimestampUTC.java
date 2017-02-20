package io.outright.xj.core.util.timestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

public interface TimestampUTC {

  static final String ZONE_ID = "UTC";

  /**
   * Timestamp for now, UTC
   *
   * @return timestamp
   */
  static Timestamp now() {
    return Timestamp.valueOf(LocalDateTime.now(ZoneId.of(ZONE_ID)));
  }

  /**
   * Timestamp for now (delta # seconds) UTC
   *
   * @param plusSeconds ahead of now
   * @return timestamp
   */
  static Timestamp nowPlusSeconds(long plusSeconds) {
    return Timestamp.valueOf(LocalDateTime.now(ZoneId.of(ZONE_ID)).plusSeconds(plusSeconds));
  }

  /**
   * Timestamp for now (delta # seconds) UTC
   *
   * @param minusSeconds before now
   * @return timestamp
   */
  static Timestamp nowMinusSeconds(long minusSeconds) {
    return Timestamp.valueOf(LocalDateTime.now(ZoneId.of(ZONE_ID)).minusSeconds(minusSeconds));
  }
}
