// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.ship.broadcast;

import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;

@Service
public class ChunkFactoryImpl implements ChunkFactory {
  final String shipChunkAudioEncoder;
  final int targetDurationSeconds;

  @Autowired
  public ChunkFactoryImpl(
    @Value("${ship.chunk.audio.encoder}") String shipChunkAudioEncoder,
    @Value("${ship.chunk.duration.seconds}") int targetDurationSeconds
  ) {
    this.shipChunkAudioEncoder = shipChunkAudioEncoder;
    this.targetDurationSeconds = targetDurationSeconds;
  }

  @Override
  public Chunk build(String shipKey, Long sequenceNumber, Long fromChainMicros, @Nullable Double actualDurationSeconds, @Nullable String fileExtension) {
    var durationMicros = (long) ((Objects.nonNull(actualDurationSeconds)
      ? actualDurationSeconds
      : targetDurationSeconds) * MICROS_PER_SECOND);
    return new Chunk(shipChunkAudioEncoder, shipKey, sequenceNumber, fromChainMicros, durationMicros, fileExtension);
  }
}
