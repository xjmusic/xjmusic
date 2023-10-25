package io.xj.nexus.work;

public interface Work {
  /**
   This initializes the work, ready to run the work cycle
   */
  void start();

  /**
   Stop work
   */
  void finish();

  /**
   Do the work cycle-- this should skip if called more frequently than the cycle duration
   */
  void runCycle();

  /**
   Check whether the craft work is finished

   @return true if finished (not running)
   */
  boolean isFinished();
}
