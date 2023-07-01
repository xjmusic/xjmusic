package io.xj.ship.broadcast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

@Service
public class ChunkFactoryImpl implements ChunkFactory {
  private final String shipChunkAudioEncoder;
  private final int shipChunkTargetDuration;

  @Autowired
  public ChunkFactoryImpl(
    @Value("${ship.chunk.audio.encoder}") String shipChunkAudioEncoder,
    @Value("${ship.chunk.target.duration}") int shipChunkTargetDuration
  ) {
    this.shipChunkAudioEncoder = shipChunkAudioEncoder;
    this.shipChunkTargetDuration = shipChunkTargetDuration;
  }

  @Override
  public Chunk build(String shipKey, Long sequenceNumber, @Nullable String fileExtension, @Nullable Double actualDuration) {
    return new Chunk(shipKey, sequenceNumber, fileExtension, actualDuration, shipChunkAudioEncoder, shipChunkTargetDuration);
  }
}
