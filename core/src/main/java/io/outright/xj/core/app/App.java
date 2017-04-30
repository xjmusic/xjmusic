// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.app;

import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.work.Leader;
import io.outright.xj.core.work.Worker;

import java.io.IOException;

public interface App {
  /**
   configure the server

   @param packages containing JAX-RS resources and providers
   */
  void configureServer(String... packages);

  /**
   configure a workload
   */
  void registerWorkload(String name, Leader leader, Worker worker) throws ConfigException;

  /**
   start App Server
   */
  void start() throws IOException, ConfigException;

  /**
   stop App Server
   */
  void stop();

  /**
   Base URI of App Server

   @return String
   */
  String baseURI();
}
