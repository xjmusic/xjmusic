//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.error;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ErrorTest {

  @Test
  public void detail_getSet() {
    assertEquals("test", new Error().setDetail("test").getDetail());
    assertEquals("test", new Error("test").getDetail());
  }

}
