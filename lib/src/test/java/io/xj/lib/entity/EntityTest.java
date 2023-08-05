// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.entity;

import io.xj.lib.Widget;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EntityTest {

  @Before
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
