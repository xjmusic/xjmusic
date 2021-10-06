package io.xj.ship.persistence;

import java.time.Instant;

/**
 An HTTP Live Streaming Media Chunk
 <p>
 SEE: https://en.m.wikipedia.org/wiki/HTTP_Live_Streaming
 <p>
 SEE: https://developer.apple.com/documentation/http_live_streaming/hls_authoring_specification_for_apple_devices
 <p>
 Ship broadcast via HTTP Live Streaming #179453189
 */
public class Chunk {
  private final Instant fromInstant;
  private final Instant toInstant;
  private final Long fromSecondsUTC;
  private final Long lengthSeconds;
  private final String shipKey;
  private ChunkState state;

  public Chunk(String shipKey, long fromSecondsUTC, long lengthSeconds) {
    this.fromSecondsUTC = fromSecondsUTC;
    this.lengthSeconds = lengthSeconds;
    this.shipKey = shipKey;
    this.state = ChunkState.Pending;
    fromInstant = Instant.ofEpochSecond(fromSecondsUTC);
    toInstant = fromInstant.plusSeconds(lengthSeconds);
  }

  public static Chunk from(String shipKey, long fromSecondsUTC, long lengthSeconds) {
    return new Chunk(shipKey, fromSecondsUTC, lengthSeconds);
  }

  public Long getFromSecondsUTC() {
    return fromSecondsUTC;
  }

  public long getLengthSeconds() {
    return lengthSeconds;
  }

  public String getShipKey() {
    return shipKey;
  }

  public ChunkState getState() {
    return state;
  }

  public Chunk setState(ChunkState state) {
    this.state = state;
    return this;
  }

  public String getStreamOutputKey() {
    return String.format("%s-%d", shipKey, fromSecondsUTC);
  }

  public Instant getFromInstant() {
    return fromInstant;
  }

  public Instant getToInstant() {
    return toInstant;
  }

}
