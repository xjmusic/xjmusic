// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import java.util.concurrent.RecursiveAction;

/**
 * Ship broadcast via HTTP Live Streaming #179453189
 */
public abstract class ChunkPrinter extends RecursiveAction {
  /**
   * Invoke the recursive action
   */
  public abstract void compute();

  /**
   * Get the output PCM data
   *
   * @return PCM data[frame][channel]
   */
  public abstract double[][] getOutputPcmData();

  /**
   * Get the file path of an MPEG2 TS file by bitrate
   *
   * @return path to .ts file
   */
  public abstract String getTsFilePath();

  /**
   * @return the .wav file path
   */
  public abstract String getWavFilePath();
}
