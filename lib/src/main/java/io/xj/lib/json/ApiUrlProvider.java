// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.json;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * Provider of URLs using the injected configuration and some custom formatting
 */
@Service
public class ApiUrlProvider {
  final String appBaseUrl;

  static final Pattern rgxStripLeadingSlash = Pattern.compile("^/");

  @Autowired
  public ApiUrlProvider(@Value("${app.base.url}") String appBaseUrl) {
    this.appBaseUrl = appBaseUrl;
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

  static String stripLeadingSlash(String input) {
    return rgxStripLeadingSlash.matcher(input).replaceAll("");
  }
}
