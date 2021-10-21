// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.broadcast;

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
  double[][] getOutputPcmData();

  /**
   @return the .wav file path
   */
  String getWavFilePath();

  /**
   @return the m4s file path
   */
  String getM4sFilePath();

  /**
   @return the mp4 init file path
   */
  String getMp4InitFilePath();
}
