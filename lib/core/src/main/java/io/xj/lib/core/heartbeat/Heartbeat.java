// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.heartbeat;

import io.xj.lib.core.payload.Payload;

/**
 App heartbeat pulse
 */
@FunctionalInterface
public interface Heartbeat {

  /**
   Do heartbeat pulse

   @return payload object containing report data
   @throws Exception if unable to pulse heart
   */
  Payload pulse() throws Exception;

}
