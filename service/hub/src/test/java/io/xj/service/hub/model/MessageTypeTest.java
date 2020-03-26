// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.model;// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import io.xj.service.hub.entity.MessageType;
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
