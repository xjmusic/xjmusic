package io.xj.ship.broadcast;

import com.google.api.client.util.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.util.Values;

import java.time.Instant;
import java.util.List;

/**
 * An HTTP Live Streaming Media Chunk
 * <p>
 * SEE: https://en.m.wikipedia.org/wiki/HTTP_Live_Streaming
 * <p>
 * SEE: https://developer.apple.com/documentation/http_live_streaming/hls_authoring_specification_for_apple_devices
 * <p>
 * Ship broadcast via HTTP Live Streaming #179453189
 */
public class Chunk {
  private final Instant fromInstant;
  private final Instant toInstant;
  private final List<String> streamOutputKeys;
  private final Long fromSecondsUTC;
  private final String shipKey;
  private final long index;
  private ChunkState state;
  private Instant updated;

  @Inject
  public Chunk(
    @Assisted("shipKey") String shipKey,
    @Assisted("fromSecondsUTC") long fromSecondsUTC,
    Environment env
  ) {
    this.fromSecondsUTC = fromSecondsUTC;
    this.shipKey = shipKey;
    this.state = ChunkState.Pending;
    fromInstant = Instant.ofEpochSecond(fromSecondsUTC);
    int chunkSeconds = env.getShipChunkSeconds();
    toInstant = fromInstant.plusSeconds(chunkSeconds);
    streamOutputKeys = Lists.newArrayList();
    index = (long) (Math.floor((double) fromSecondsUTC / chunkSeconds));
  }

  public Long getFromSecondsUTC() {
    return fromSecondsUTC;
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

  public String getKey(int bitrate) {
    return String.format("%s-%s-%d", shipKey, Values.kbps(bitrate), index);
  }

  public String getKey() {
    return String.format("%s-%d", shipKey, index);
  }


  public Instant getFromInstant() {
    return fromInstant;
  }

  public Instant getToInstant() {
    return toInstant;
  }

  public List<String> getStreamOutputKeys() {
    return streamOutputKeys;
  }

  public Instant getUpdated() {
    return updated;
  }

  public void setUpdated(Instant updated) {
    this.updated = updated;
  }

  public Chunk reset() {
    state = ChunkState.Pending;
    streamOutputKeys.clear();
    return this;
  }

  public Chunk addStreamOutputKey(String streamKey) {
    streamOutputKeys.add(streamKey);
    return this;
  }
}
