// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.entity;

import io.xj.lib.entity.MessageType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MessageTypeTest {
  @Test
  public void TypeToString() throws Exception {
    assertEquals("Debug", MessageType.Debug.toString());
    assertEquals("Warning", MessageType.Warning.toString());
    assertEquals("Info", MessageType.Info.toString());
    assertEquals("Error", MessageType.Error.toString());
  }

}
