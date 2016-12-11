// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.server;

import io.outright.xj.core.application.Application;
import io.outright.xj.core.application.logger.FileLogger;
import org.glassfish.jersey.filter.LoggingFilter;

import java.io.IOException;

public class LogFilterProviderImpl implements LogFilterProvider {
  private static LoggingFilter instance;

  @Override
  public void setup(String path, int maxEntitySize) throws IOException {
    instance = new LoggingFilter(FileLogger.getLogger(Application.class, path), maxEntitySize);
  }

  @Override
  public void setup(String path, boolean printEntity) throws IOException {
    instance = new LoggingFilter(FileLogger.getLogger(Application.class, path), printEntity);
  }

  @Override
  public LoggingFilter get() {
    return instance;
  }
}
