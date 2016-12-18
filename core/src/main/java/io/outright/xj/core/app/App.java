// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.app;

import io.outright.xj.core.app.exception.ConfigException;

import java.io.IOException;

public interface App {
  /**
   * configure App Server
   */
  void configure(String... packages);

  /**
   * start App Server
   */
  void start() throws IOException, ConfigException;

  /**
   * stop App Server
   */
  void stop();

  /**
   * Base URI of App Server
   * @return String
   */
  String baseURI();
}
