// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.util;

import io.outright.xj.core.application.exception.ConfigException;

abstract public class RequiredProperty {
  public static String get(String key) throws ConfigException {
    String value = System.getProperty(key);
    if (value == null) {
      throw new ConfigException("Must set system property: "+key);
    }
    return value;
  }
}
