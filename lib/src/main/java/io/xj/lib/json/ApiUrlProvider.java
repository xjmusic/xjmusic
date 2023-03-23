// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.json;

import io.xj.lib.app.AppEnvironment;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * Provider of URLs using the injected configuration and some custom formatting
 */
@Service
public class ApiUrlProvider {
  private final String appBaseUrl;

  private static final Pattern rgxStripLeadingSlash = Pattern.compile("^/");

  public ApiUrlProvider(AppEnvironment env) {
    this.appBaseUrl = env.getAppBaseUrl();
  }

  /**
   * Get URI object for a path within the API
   *
   * @param path within API
   * @return String
   */
  public URI getAppURI(String path) {
    return URI.create(getAppUrl(path));
  }

  /**
   * @return app base URL
   */
  public String getAppBaseUrl() {
    return appBaseUrl;
  }

  /**
   * Get the URL for a path in the app
   *
   * @param path to get URL for
   * @return RUL for given path
   */
  public String getAppUrl(String path) {
    return String.format("%s%s", appBaseUrl, stripLeadingSlash(path));
  }

  private static String stripLeadingSlash(String input) {
    return rgxStripLeadingSlash.matcher(input).replaceAll("");
  }
}
