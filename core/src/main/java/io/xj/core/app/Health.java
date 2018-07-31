// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app;

/**
 App health check
 */
public interface Health {

  /**
   Do health check
   @throws Exception if unhealthy
   */
  void check() throws Exception;

}
