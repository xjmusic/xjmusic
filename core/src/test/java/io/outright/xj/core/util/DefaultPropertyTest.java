// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.util;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultPropertyTest {

  @Test
  public void setIfNotAlready_newProperty() throws Exception {
    DefaultProperty.setIfNotAlready("bun","tabby");
    assert System.getProperty("bun").equals("tabby");
  }

  @Test
  public void setIfNotAlready_alreadySetProperty() throws Exception {
    System.setProperty("bun","jammy");
    DefaultProperty.setIfNotAlready("bun","tabby");
    assert System.getProperty("bun").equals("jammy");
  }

  @After
  public void cleanup() {
    System.clearProperty("bun");
  }

}
