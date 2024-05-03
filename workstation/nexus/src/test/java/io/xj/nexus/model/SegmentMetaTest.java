// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SegmentMetaTest {
  SegmentMeta subject;

  @BeforeEach
  public void setUp() throws Exception {
    subject = new SegmentMeta()
      .id(UUID.randomUUID())
      .segmentId(123)
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
    var newSegmentId = 123;
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
