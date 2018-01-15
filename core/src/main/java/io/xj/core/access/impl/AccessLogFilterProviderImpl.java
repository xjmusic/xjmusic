// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access.impl;

import io.xj.core.access.AccessLogFilterProvider;
import io.xj.core.app.App;
import io.xj.core.config.Config;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class AccessLogFilterProviderImpl implements AccessLogFilterProvider {
  private static LoggingFilter instance;
  private final Logger log = LoggerFactory.getLogger(AccessLogFilterProviderImpl.class);
  private final String pathToWriteAccessLog = Config.logAccessFilename();
  private final Integer maxSizeEntitiesInAccessLog = Config.logAccessEntitiesMaxsize();
  private final Boolean showEntitiesInAccessLog = Config.logAccessEntitiesAll();

  /**
   Setup a path and max entity size

   @param path          to setup
   @param maxEntitySize to setup
   @throws IOException on failure
   */
  private static void setup(String path, int maxEntitySize) throws IOException {
    instance = new LoggingFilter(getLogger(App.class, path), maxEntitySize);
  }

  /**
   Setup a path and whether to print the entity

   @param path        to setup
   @param printEntity should print entity?
   @throws IOException on failure
   */
  private static void setup(String path, boolean printEntity) throws IOException {
    instance = new LoggingFilter(getLogger(App.class, path), printEntity);
  }

  /**
   Get a file logger

   @param clazz           of Logger
   @param pathToWriteFile to write file output to
   @return Logger
   @throws IOException when there is a problem opening the file for writing
   */
  private static java.util.logging.Logger getLogger(Class clazz, String pathToWriteFile) throws IOException {
    String name = clazz.getName();
    java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
    logger.setUseParentHandlers(false);
    FileHandler fh = new FileHandler(pathToWriteFile);
    logger.addHandler(fh);
    SimpleFormatter formatter = new SimpleFormatter();
    fh.setFormatter(formatter);
    return logger;
  }

  @Override
  public void registerTo(ResourceConfig resourceConfig) {
    try {
      if (0 < maxSizeEntitiesInAccessLog) {
        setup(pathToWriteAccessLog, maxSizeEntitiesInAccessLog);
      } else {
        setup(pathToWriteAccessLog, showEntitiesInAccessLog);
      }
      resourceConfig.register(instance);
      log.info("Writing access log to {}", pathToWriteAccessLog);
    } catch (IOException e) {
      log.warn("Failed to registerTo access log writer!", e);
    }
  }
}
