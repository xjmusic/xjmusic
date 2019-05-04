//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.util;

import io.xj.core.model.segment.SegmentTestHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestUuidCacheTest {
  SegmentTestHelper subject;

  @Before
  public void setUp() throws Exception {
    subject = new SegmentTestHelper();
  }

  @Test
  public void get() {
    UUID uuidOne = subject.getUuid(1);
    UUID uuidTwo = subject.getUuid(2);
    assertNotEquals(uuidTwo, uuidOne);
    assertEquals(uuidOne, subject.getUuid(1));
    assertEquals(uuidTwo, subject.getUuid(2));
  }
}
