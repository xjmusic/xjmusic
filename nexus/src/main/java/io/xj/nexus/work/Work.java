package io.xj.nexus.work;

import io.xj.nexus.ship.ShipException;

import java.io.IOException;

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
   Check whether the craft work is running

   @return true if running
   */
  boolean isRunning();
}
