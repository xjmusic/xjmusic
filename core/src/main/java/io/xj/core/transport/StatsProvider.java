// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import io.xj.core.payload.PayloadObject;

@FunctionalInterface
public interface StatsProvider {
  String KEY = "stats";

  /**
   Get platform status

   @return JSON object
   */
  PayloadObject toPayloadObject();
}
