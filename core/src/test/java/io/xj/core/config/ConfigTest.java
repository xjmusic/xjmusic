// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigTest {
  @Test
  public void setDefault_newProperty() throws Exception {
    Config.setDefault("bun", "tabby");
    assertEquals(("tabby"), System.getProperty("bun"));
  }

  @Test
  public void setDefault_alreadySetProperty() throws Exception {
    System.setProperty("bun", "jammy");
    Config.setDefault("bun", "tabby");
    assertEquals(("jammy"), System.getProperty("bun"));
  }

  @Test
  public void getRequiredProperty() throws Exception {
    assertEquals("http://mush/", Config.appBaseUrl());
  }

  @After
  public void cleanup() {
    System.clearProperty("bun");
    System.clearProperty("app.url.base");
  }

  @Before
  public void setup() {
    System.setProperty("app.url.base", "http://mush/");
  }
}
