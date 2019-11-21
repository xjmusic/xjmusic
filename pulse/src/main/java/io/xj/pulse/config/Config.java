// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.pulse.config;

import java.util.Objects;

public class Config {
  private static final Integer DEFAULT_TIMEOUT_MILLIS = 20000;
  private final String heartbeatKey;
  private final String heartbeatURL;
  private final Integer timeoutMillis;

  /**
   * Instantiate a configuration
   *
   * @throws Exception if an environment parameter is not set
   */
  public Config() throws Exception {
    heartbeatKey = getRequired("platform_heartbeat_key");
    heartbeatURL = getRequired("platform_heartbeat_url");
    timeoutMillis = getIntOrDefault("timeout_millis", DEFAULT_TIMEOUT_MILLIS);
  }

  /**
   * Get Heartbeat key from environment parameter
   *
   * @return heartbeat key
   */
  public String getHeartbeatKey() {
    return heartbeatKey;
  }

  /**
   * Get Heartbeat URL from environment parameter
   *
   * @return heartbeat URL
   */
  public String getHeartbeatURL() {
    return heartbeatURL;
  }

  /**
   * Get Timeout milliseconds from environment parameter, or default if not set
   *
   * @return timeout in milliseconds
   */
  public Integer getTimeoutMillis() {
    return timeoutMillis;
  }

  /**
   * Get a required environment variable
   *
   * @param name to get
   * @return value
   */
  private static String getRequired(String name) throws Exception {
    String value = System.getenv(name);
    if (Objects.isNull(value) || value.isEmpty()) {
      throw new Exception(String.format("Config property required: '%s'", name));
    }
    return value;
  }

  /**
   * Get a required environment variable
   *
   * @param name to get
   * @return value
   */
  private static Integer getIntOrDefault(String name, Integer defaultValue) {
    String valueString = System.getenv(name);
    if (Objects.isNull(valueString) || valueString.isEmpty()) {
      return defaultValue;
    }
    return Integer.valueOf(valueString);
  }

}
