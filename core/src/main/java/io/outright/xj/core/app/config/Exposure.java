// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.config;

import java.net.URI;

/**
 * ALL APPLICATION CONSTANTS MUST IMPLEMENT THIS CENTRAL CLASS.
 */
public abstract class Exposure {

  /**
   * wrapError message as JSON output payload.
   */
  public static final String ERRORS_KEY = "errors";
  public static final String ERROR_DETAIL_KEY = "detail";

  /**
   * Get URL String for a path within the API
   * @param path within API
   * @return String
   */
  public static String apiUrlString(String path) {
    return Config.appBaseUrl() + Config.apiPath() + path;
  }

  /**
   * Get URI object for a path within the API
   * @param path within API
   * @return String
   */
  public static URI apiURI(String path) {
    return URI.create(apiUrlString(path));
  }

}
