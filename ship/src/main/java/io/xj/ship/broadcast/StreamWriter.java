// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import io.xj.ship.ShipException;

/**
 Ship service can be used to write N seconds to local .WAV file #181082015
 */
public interface StreamWriter {

  /**
   Publish a media segment to the output

   @param samples of audio to append
   */
  double[][] append(double[][] samples) throws ShipException;

  /**
   Close the writer and release resources
   */
  void close();
}
