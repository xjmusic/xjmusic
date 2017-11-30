// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.pulse.util;

import java.util.Objects;

public class EnvironmentProvider {

  /**
   Get a required environment variable

   @param name to get
   @return value
   */
  public String getRequired(String name) throws Exception {
    String value = System.getenv(name);
    if (Objects.isNull(value) || value.isEmpty()) {
      throw new Exception(String.format("Environment property required: '%s'", name));
    }
    return value;
  }

}
