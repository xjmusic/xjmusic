package io.xj.nexus.work;

public interface Work {
  /**
   Stop work
   */
  void finish();

  /**
   Run the work cycle
   */
  void runCycle(long toChainMicros);

  /**
   Check whether the craft work is finished

   @return true if finished (not running)
   */
  boolean isFinished();
}
