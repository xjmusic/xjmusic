// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.access;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.core.util.TempFile;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

class AccessLogFilterProviderImpl implements AccessLogFilterProvider {
  private static LoggingFeature instance;
  private final Logger log = LoggerFactory.getLogger(AccessLogFilterProviderImpl.class);
  private final String pathToWriteAccessLog;

  @Inject
  public AccessLogFilterProviderImpl(
    Config config
  ) {
    pathToWriteAccessLog = config.hasPath("app.accessLogFile") ?
      config.getString("app.accessLogFile") :
      String.format("%s%saccess.log", TempFile.getTempFilePathPrefix(), File.separator);
  }

  /**
   Setup a path and max entity size

   @param path to setup
   @throws IOException on failure
   */
  private static void setup(String path) throws IOException {
    instance = new LoggingFeature(getLogger(path));
  }

  /**
   Get a file logger

   @param pathToWriteFile to write file output to
   @return Logger
   @throws IOException when there is a problem opening the file for writing
   */
  private static java.util.logging.Logger getLogger(String pathToWriteFile) throws IOException {
    String name = "Jersey";
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
      setup(pathToWriteAccessLog);
      resourceConfig.register(instance);
      log.info("Writing access log to {}", pathToWriteAccessLog);
    } catch (IOException e) {
      log.warn("Failed to registerTo access log writer!", e);
    }
  }
}
