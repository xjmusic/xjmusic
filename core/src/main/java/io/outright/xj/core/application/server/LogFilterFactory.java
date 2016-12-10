// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.server;

import org.glassfish.jersey.filter.LoggingFilter;

import java.io.IOException;

public interface LogFilterFactory {
  LoggingFilter newFilter(String path, int maxEntitySize) throws IOException;

  LoggingFilter newFilter(String path, boolean printEntity) throws IOException;
}
