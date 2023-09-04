// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LabUrlProviderTest {
  LabUrlProvider subject;

  @BeforeEach
  public void setUp() {
    subject = new LabUrlProvider("https://lab.test.xj.io/");
  }

  @Test
  public void getAppURI() {
    var result = subject.computeUri("test");
    assertEquals("lab.test.xj.io", result.getHost());
  }

  @Test
  public void getAppUrl() {
    assertEquals("https://lab.test.xj.io/test", subject.computeUrl("test"));
    assertEquals("https://lab.test.xj.io/test/123", subject.computeUrl("test/123"));
    assertEquals("https://lab.test.xj.io/test", subject.computeUrl("/test")); // strips leading slash
  }
}
