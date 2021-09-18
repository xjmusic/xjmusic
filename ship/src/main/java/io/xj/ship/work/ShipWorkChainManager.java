package io.xj.ship.work;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public interface ShipWorkChainManager {
  /**
   Check current status, change states if necessary, perform actions or blow up.
   Called from within the Ship Work loop.
   */
  void poll();

  /**
   Test whether all expected chains are healthy, depending on chain manager mode

   @return true if all chains are healthy
   */
  boolean isHealthy();
}
