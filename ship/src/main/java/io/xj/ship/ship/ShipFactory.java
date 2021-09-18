// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.ship;

import io.xj.ship.ShipException;

/**
 Ship broadcast via HTTP Live Streaming #179453189 */
public interface ShipFactory {

  Ship ship(
  ) throws ShipException;

}
