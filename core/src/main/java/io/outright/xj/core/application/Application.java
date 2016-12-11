// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application;

import java.io.IOException;

public interface Application {
  /**
   * Configure Application Server
   */
  void Configure(String... packages);

  /**
   * Start Application Server
   */
  void Start() throws IOException;

  /**
   * Stop Application Server
   */
  void Stop();

  /**
   * Base URI of Application Server
   * @return String
   */
  String BaseURI();
}
