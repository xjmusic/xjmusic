// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

/**
 This process is run directly in the hard loop (not in a Fork/Join pool)
 <p>
 Ship broadcast via HTTP Live Streaming #179453189
 */
public interface ChunkPrinter {
  /**
   Invoke the recursive action
   */
  void print();

  /**
   Get the output PCM data

   @return PCM data[frame][channel]
   */
  public abstract double[][] getOutputPcmData();

  /**
   Get the file path of an MPEG2 TS file by bitrate

   @return path to .ts file
   */
  public abstract String getTsFilePath();

  /**
   @return the .wav file path
   */
  public abstract String getWavFilePath();
}
