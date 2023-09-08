// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
public class VersionService {

  private String version;

  @PostConstruct
  public void init() {
    Properties properties = new Properties();
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("version.properties")) {
      if (inputStream != null) {
        properties.load(inputStream);
        version = properties.getProperty("version");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getVersion() {
    return version;
  }
}
