// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;


import io.xj.lib.app.AppEnvironment;
import io.xj.lib.util.Values;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Objects;

import static io.xj.lib.util.Values.NANOS_PER_SECOND;

/**
 * An HTTP Live Streaming Media Chunk
 * <p>
 * SEE: https://en.m.wikipedia.org/wiki/HTTP_Live_Streaming
 * <p>
 * SEE: https://developer.apple.com/documentation/http_live_streaming/hls_authoring_specification_for_apple_devices
 * <p>
 * Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 */
public class Chunk {
  private final Instant fromInstant;
  private final Instant toInstant;
  private final String fileExtension;
  private final String shipKey;
  private final double actualDuration;
  private final long fromSecondsUTC;
  private final long sequenceNumber;
  private final long toSecondsUTC;

  public Chunk(
    AppEnvironment env, String shipKey,
    Long sequenceNumber,
    @Nullable String fileExtension,
    @Nullable Double actualDuration
  ) {
    this.sequenceNumber = sequenceNumber;
    this.fileExtension = Objects.nonNull(fileExtension)
      ? fileExtension
      : env.getShipChunkAudioEncoder();
    this.shipKey = shipKey;
    this.actualDuration = Objects.nonNull(actualDuration)
      ? actualDuration
      : env.getShipChunkTargetDuration();

    fromSecondsUTC = sequenceNumber * env.getShipChunkTargetDuration(); // seq num to time ratio always based on target duration, not actual
    toSecondsUTC = fromSecondsUTC + env.getShipChunkTargetDuration();
    fromInstant = Instant.ofEpochSecond(fromSecondsUTC);
    toInstant = fromInstant.plusNanos((long) (this.actualDuration * NANOS_PER_SECOND));
  }

  public Instant getFromInstant() {
    return fromInstant;
  }

  public Long getFromSecondsUTC() {
    return fromSecondsUTC;
  }

  public String getKey() {
    return String.format("%s-%d", shipKey, sequenceNumber);
  }

  public String getKey(int bitrate) {
    return String.format("%s-%s-%d", shipKey, Values.k(bitrate), sequenceNumber);
  }

  public String getKeyTemplate(int bitrate) {
    return String.format("%s-%s-%%d", shipKey, Values.k(bitrate));
  }

  public Instant getToInstant() {
    return toInstant;
  }

  public double getActualDuration() {
    return actualDuration;
  }

  public long getSequenceNumber() {
    return sequenceNumber;
  }

  public String getShipKey() {
    return shipKey;
  }

  public String getFilename() {
    return String.format("%s-%d.%s", shipKey, sequenceNumber, fileExtension);
  }

  public long getToSecondsUTC() {
    return toSecondsUTC;
  }

  public int compare(Chunk chunk) {
    return Long.compare(sequenceNumber, chunk.getSequenceNumber());
  }
}
