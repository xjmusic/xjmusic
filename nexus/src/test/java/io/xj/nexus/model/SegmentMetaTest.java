// Copyright (c) 1999-2022, XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.model;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class SegmentMetaTest {
  SegmentMeta subject;

  @Before
  public void setUp() throws Exception {
    subject = new SegmentMeta()
      .id(UUID.randomUUID())
      .segmentId(UUID.randomUUID())
      .key("testing")
      .value("123");
  }

  @Test
  public void setId_getId() {
    var newId = UUID.randomUUID();
    subject.setId(newId);
    assertEquals(newId, subject.getId());
  }

  @Test
  public void setSegmentId_getSegmentId() {
    var newSegmentId = UUID.randomUUID();
    subject.setSegmentId(newSegmentId);
    assertEquals(newSegmentId, subject.getSegmentId());
  }

  @Test
  public void setKey_getKey() {
    var newKey = "bingo";
    subject.setKey(newKey);
    assertEquals(newKey, subject.getKey());
  }

  @Test
  public void setValue_getValue() {
    var newValue = "smorgasbord";
    subject.setValue(newValue);
    assertEquals(newValue, subject.getValue());
  }

}
