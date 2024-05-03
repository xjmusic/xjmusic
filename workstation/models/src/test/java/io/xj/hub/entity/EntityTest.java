// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.entity;

import io.xj.hub.util.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityTest {

  @BeforeEach
  public void setUp() {
  }

  /**
   EventEntity Position persists exact floating point https://github.com/xjmusic/workstation/issues/223
   <p>
   Limit the floating point precision of chord and event position, in order to limit obsession over the position of things. https://github.com/xjmusic/workstation/issues/223
   */
  @Test
  public void exactPosition() {
    assertEquals(1.25179957, new Widget()
      .setPosition(1.25179957)
      .getPosition(), 0.0000001);
  }
}
