// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.hub.TemplateConfig;
import io.xj.lib.app.Environment;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;

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
  private final String shipKey;
  private final TemplateConfig templateConfig;
  private final int lengthSeconds;
  private final int index;
  private final int sequenceNumber;
  private Instant updated;

  @Inject
  public Chunk(
    @Assisted("shipKey") String shipKey,
    @Assisted("fromSecondsUTC") long fromSecondsUTC,
    Environment env,
    ChainManager chains
  ) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ValueException {
    this.fromSecondsUTC = fromSecondsUTC;
    this.shipKey = shipKey;
    templateConfig = new TemplateConfig(chains.readOneByShipKey(shipKey).getTemplateConfig());
    fromInstant = Instant.ofEpochSecond(fromSecondsUTC);
    lengthSeconds = env.getShipMixChunkSeconds();
    toInstant = fromInstant.plusSeconds(lengthSeconds);
    index = (int) (Math.floor((double) fromSecondsUTC / lengthSeconds));
    sequenceNumber = index + 1;
  }

  public Long getFromSecondsUTC() {
    return fromSecondsUTC;
  }

  public String getShipKey() {
    return shipKey;
  }

  public String getKey(int bitrate) {
    return String.format("%s-%s-%d", shipKey, Values.k(bitrate), index);
  }

  public String getKeyTemplate(int bitrate) {
    return String.format("%s-%s-%%d", shipKey, Values.k(bitrate));
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

  public int getIndex() {
    return index;
  }
}
