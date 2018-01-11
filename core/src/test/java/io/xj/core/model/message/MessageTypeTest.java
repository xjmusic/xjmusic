package io.xj.core.model.message;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import org.junit.Test;

import static org.junit.Assert.*;

public class MessageTypeTest {
  @Test
  public void TypetoString() throws Exception {
    assertEquals("Debug", MessageType.Debug.toString());
    assertEquals("Warning", MessageType.Warning.toString());
    assertEquals("Info", MessageType.Info.toString());
    assertEquals("Error", MessageType.Error.toString());
  }

}
