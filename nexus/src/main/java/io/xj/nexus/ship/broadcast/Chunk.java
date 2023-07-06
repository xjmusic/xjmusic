// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.ship.broadcast;


import io.xj.lib.util.Values;

import javax.annotation.Nullable;
import java.util.Objects;

import static io.xj.lib.util.Values.MICROS_PER_SECOND;

/**
 * An HTTP Live Streaming Media Chunk
 * <p>
 * SEE: https://en.m.wikipedia.org/wiki/HTTP_Live_Streaming
 * <p>
 * SEE: https://developer.apple.com/documentation/http_live_streaming/hls_authoring_specification_for_apple_devices
 * <p>
 * Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 * <p>
 * Nexus timing is entirely in chain/segment microseconds https://www.pivotaltracker.com/story/show/185515194
 */
public class Chunk {
  private final Long fromChainMicros;
  private final Long durationMicros;
  private final String fileExtension;
  private final String shipKey;
  private final long sequenceNumber;

  public Chunk(
    String shipChunkAudioEncoder,
    String shipKey,
    Long sequenceNumber,
    Long fromChainMicros,
    Long durationMicros,
    @Nullable String fileExtension
  ) {
    this.sequenceNumber = sequenceNumber;
    this.fileExtension = Objects.nonNull(fileExtension)
      ? fileExtension
      : shipChunkAudioEncoder;
    this.shipKey = shipKey;
    this.fromChainMicros = fromChainMicros;
    this.durationMicros = durationMicros;
  }

  public Long getFromChainMicros() {
    return fromChainMicros;
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

  public Long getDurationMicros() {
    return durationMicros;
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

  public int compare(Chunk chunk) {
    return Long.compare(sequenceNumber, chunk.getSequenceNumber());
  }

  public double getDurationSeconds() {
    return (double) durationMicros / MICROS_PER_SECOND;
  }

  public long getToChainMicros() {
    return fromChainMicros + durationMicros;
  }
}
