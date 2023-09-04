// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.regex.Pattern;

/**
 Provider of URLs using the injected configuration and some custom formatting
 */
@Service
public class LabUrlProvider {
  final String appBaseUrl;

  static final Pattern rgxStripLeadingSlash = Pattern.compile("^/");

  @Autowired
  public LabUrlProvider(@Value("${app.base.url}") String appBaseUrl) {
    this.appBaseUrl = appBaseUrl;
  }

  /**
   Get URI object for a path within the API

   @param path within API
   @return String
   */
  public URI computeUri(String path) {
    return URI.create(computeUrl(path));
  }

  /**
   Get the URL for a path in the app

   @param path to get URL for
   @return RUL for given path
   */
  public String computeUrl(String path) {
    return String.format("%s%s", appBaseUrl, rgxStripLeadingSlash.matcher(path).replaceAll(""));
  }
}
