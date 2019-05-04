//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app;

import java.util.Collection;
import java.util.Map;

/**
 App heartbeat pulse
 */
@FunctionalInterface
public interface Heartbeat {
  String KEY_ONE = "heartbeat";

  /**
   Do heartbeat pulse
   @throws Exception if unable to pulse heart
   @return
   */
  Map<String, Collection> pulse() throws Exception;

}
