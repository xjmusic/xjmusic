package io.xj.core.model.entity;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EntityTest {

  /**
   [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.
   */
  @Test
  public void roundPosition() throws Exception {
    assertEquals(5.35, Entity.roundPosition(5.35169988945), 0.0000001);
  }

}
