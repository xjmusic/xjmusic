// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.entity;

import io.xj.hub.util.Widget;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EntityTest {

  @BeforeEach
  public void setUp() {
  }

  /**
   * EventEntity Position persists exact floating point https://www.pivotaltracker.com/story/show/175602029
   * <p>
   * DEPRECATED: Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things. https://www.pivotaltracker.com/story/show/154976066
   */
  @Test
  public void exactPosition() {
    Assert.assertEquals(1.25179957, new Widget()
      .setPosition(1.25179957)
      .getPosition(), 0.0000001);
  }
}
