// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import org.json.JSONObject;

@FunctionalInterface
public interface StatsProvider {

  /**
   Get platform status

   @return JSON object
   */
  JSONObject getJSON();

}
