// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.util;

import io.outright.xj.core.application.exception.ConfigException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RequiredPropertyTest {

  @Test
  public void setIfNotAlready_newProperty() throws Exception {
    assert RequiredProperty.get("bun").equals("tabby");
  }

  @Test(expected = ConfigException.class)
  public void setIfNotAlready_alreadySetProperty() throws Exception {
    assert RequiredProperty.get("jam").equals("tabby");
  }

  @Before
  public void before() {
    System.setProperty("bun", "tabby");
  }

  @After
  public void after() {
    System.clearProperty("bun");
  }

}
