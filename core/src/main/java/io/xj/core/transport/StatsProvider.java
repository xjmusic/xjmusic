// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import io.xj.core.model.payload.PayloadObject;

@FunctionalInterface
public interface StatsProvider {
  String KEY = "stats";

  /**
   Get platform status

   @return JSON object
   */
  PayloadObject toPayloadObject();
}
