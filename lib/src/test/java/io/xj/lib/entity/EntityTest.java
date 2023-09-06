// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.entity;

import io.xj.lib.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityTest {

  @BeforeEach
  public void setUp() {
  }

  /**
   EventEntity Position persists exact floating point https://www.pivotaltracker.com/story/show/175602029
   <p>
   DEPRECATED: Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things. https://www.pivotaltracker.com/story/show/154976066
   */
  @Test
  public void exactPosition() {
    assertEquals(1.25179957, new Widget()
      .setPosition(1.25179957)
      .getPosition(), 0.0000001);
  }
}
