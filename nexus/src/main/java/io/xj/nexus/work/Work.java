package io.xj.nexus.work;

public interface Work {
  /**
   Stop work
   */
  void finish();

  /**
   Check whether the craft work is finished

   @return true if finished (not running)
   */
  boolean isFinished();
}
