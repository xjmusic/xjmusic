package io.xj.core.model.message;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

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
