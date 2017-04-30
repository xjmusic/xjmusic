package io.outright.xj.core.app.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

// Copyright Outright Mental, Inc. All Rights Reserved.
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
