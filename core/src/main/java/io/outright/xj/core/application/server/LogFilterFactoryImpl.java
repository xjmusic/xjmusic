// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.server;

import io.outright.xj.core.application.Application;
import io.outright.xj.core.application.logger.FileLogger;
import org.glassfish.jersey.filter.LoggingFilter;

import java.io.IOException;

public class LogFilterFactoryImpl implements LogFilterFactory {
  @Override
  public LoggingFilter newFilter(String path, int maxEntitySize) throws IOException {
    return new LoggingFilter(FileLogger.getLogger(Application.class, path), maxEntitySize);
  }

  @Override
  public LoggingFilter newFilter(String path, boolean printEntity) throws IOException {
    return new LoggingFilter(FileLogger.getLogger(Application.class, path), printEntity);
  }
}
