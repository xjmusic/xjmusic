package io.xj.nexus.work;

public interface Work {
  /**
   Stop work
   */
  void finish();

  /**
   Do the work cycle-- this should skip if called more frequently than the cycle duration
   */
  void runCycle(long toChainMicros);

  /**
   Check whether the craft work is finished

   @return true if finished (not running)
   */
  boolean isFinished();
}
