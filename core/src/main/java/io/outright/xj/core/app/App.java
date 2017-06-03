// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.app;

import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.work.Worker;
import io.outright.xj.core.chain_gang.Follower;
import io.outright.xj.core.chain_gang.Leader;

import java.io.IOException;

public interface App {
  /**
   configure the server

   @param packages containing JAX-RS resources and providers
   */
  void configureServer(String... packages);

  /**
   configure a gang workload (with a leader and many workers)
   */
  void registerGangWorkload(String name, Leader leader, Follower follower) throws ConfigException;

  /**
   configure a simple workload (with just a worker)
   */
  void registerSimpleWorkload(String name, Worker worker) throws ConfigException;

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
