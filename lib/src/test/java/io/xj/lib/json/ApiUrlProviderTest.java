// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiUrlProviderTest {
  ApiUrlProvider subject;

  @BeforeEach
  public void setUp() {
    subject = new ApiUrlProvider("https://lab.test.xj.io/");
  }

  @Test
  public void getAppURI() {
    var result = subject.getAppURI("test");
    assertEquals("lab.test.xj.io", result.getHost());
  }

  @Test
  public void getAppUrl() {
    assertEquals("https://lab.test.xj.io/test", subject.getAppUrl("test"));
    assertEquals("https://lab.test.xj.io/test/123", subject.getAppUrl("test/123"));
    assertEquals("https://lab.test.xj.io/test", subject.getAppUrl("/test")); // strips leading slash
  }

  @Test
  public void getAppBaseUrl() {
    assertEquals("https://lab.test.xj.io/", subject.getAppBaseUrl());
  }

}
