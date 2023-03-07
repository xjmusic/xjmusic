package io.xj.ship.broadcast;

import io.xj.lib.app.AppEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

@Service
public class ChunkFactoryImpl implements ChunkFactory {
  private final AppEnvironment env;

  @Autowired
  public ChunkFactoryImpl(
    AppEnvironment env
  ) {
    this.env = env;
  }

  @Override
  public Chunk build(String shipKey, Long sequenceNumber, @Nullable String fileExtension, @Nullable Double actualDuration) {
    return new Chunk(env, shipKey, sequenceNumber, fileExtension, actualDuration);
  }
}
