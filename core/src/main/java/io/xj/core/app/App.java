// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.app;

import io.xj.core.exception.ConfigException;
import io.xj.core.work.WorkManager;
import net.greghaines.jesque.worker.JobFactory;

import java.io.IOException;

public interface App {
  /**
   configure the server

   @param packages containing JAX-RS resources and providers
   */
  void configureServer(String... packages);

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

  /**
   Set job factory for resque client

   @param jobFactory for app
   */
  void setJobFactory(JobFactory jobFactory);

  /**
   Get the current work manager
   @return work manager
   */
  WorkManager getWorkManager();
}
