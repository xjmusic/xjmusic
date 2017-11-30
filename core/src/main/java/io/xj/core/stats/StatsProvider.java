// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.stats;

import org.json.JSONObject;

@FunctionalInterface
public interface StatsProvider {

  /**
   Get platform status

   @return JSON object
   */
  JSONObject getJSON();

}
