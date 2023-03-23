// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.json;

import com.google.common.collect.ImmutableMap;
import io.xj.lib.app.AppEnvironment;
import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiUrlProviderTest {
  ApiUrlProvider subject;

  @Before
  public void setUp() {
    subject = new ApiUrlProvider(AppEnvironment.from(ImmutableMap.of(
      "APP_BASE_URL", "https://lab.test.xj.io/"
    )));
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
