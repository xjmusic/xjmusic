package io.xj.ship.persistence;

import java.util.Collection;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public interface ChunkManager {

  /**
   Compute the farthest-future millis to which we have a contiguous series of streamed chunks

   @return millis to which we've assembled the contiguous series of streamed chunks
   */
  long computeAssembledToMillis();

  /**
   Compute the expected contiguous series of chunks
   <p>

   @param shipKey of chain for which to compute planned chunks
   @return list of planned chunk
   */
  Collection<Chunk> computeAll(String shipKey);

  /**
   Compute the expected contiguous series of chunks to be exported right now as an HTTP live stream.
   <p>

   @param shipKey of chain for which to compute planned chunks
   @return list of planned chunk
   */
  Collection<Chunk> computeAllContiguousDone(String shipKey);

  /**
   Store a Chunk

   @return stored chunk
   @param chunk   to store
   */
  Chunk put(Chunk chunk);

  /**
   Whether the chunks are assembled far enough ahead into the future

   @return true if assembled far enough ahead
   */
  boolean isAssembledFarEnoughAhead();
}
