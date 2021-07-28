// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

import io.xj.ProgramSequenceChord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class EntityTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Before
  public void setUp() {
  }

  /**
   [#175602029] EventEntity Position persists exact floating point
   <p>
   DEPRECATED: [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.
   */
  @Test
  public void exactPosition() {
    Assert.assertEquals(1.25179957, ProgramSequenceChord.newBuilder()
      .setPosition(1.25179957)
      .build()
      .getPosition(), 0.0000001);
  }
}
