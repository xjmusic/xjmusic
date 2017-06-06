// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.app.logger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 */
public class FileLogger {
  /**
   Get a file logger

   @param clazz           of Logger
   @param pathToWriteFile to write file output to
   @return Logger
   @throws IOException when there is a problem opening the file for writing
   */
  public static Logger getLogger(Class clazz, String pathToWriteFile) throws IOException {
    String name = clazz.getName();
    Logger logger = Logger.getLogger(name);
    logger.setUseParentHandlers(false);
    FileHandler fh = new FileHandler(pathToWriteFile);
    logger.addHandler(fh);
    SimpleFormatter formatter = new SimpleFormatter();
    fh.setFormatter(formatter);
    return logger;
  }
}
