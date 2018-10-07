//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app;

import io.xj.core.model.work.Work;
import org.json.JSONObject;

import java.util.Collection;

/**
 App heartbeat pulse
 */
@FunctionalInterface
public interface Heartbeat {
  String KEY_ONE = "heartbeat";

  /**
   Do heartbeat pulse
   @throws Exception if unable to pulse heart
   */
  JSONObject pulse() throws Exception;

}
