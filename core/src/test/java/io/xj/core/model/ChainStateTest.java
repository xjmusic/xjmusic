// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model;// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChainStateTest {
  @Test
  public void TypeToString() throws Exception {
    assertEquals("Draft", ChainState.Draft.toString());
    assertEquals("Ready", ChainState.Ready.toString());
    assertEquals("Fabricate", ChainState.Fabricate.toString());
    assertEquals("Complete", ChainState.Complete.toString());
    assertEquals("Failed", ChainState.Failed.toString());
    assertEquals("Erase", ChainState.Erase.toString());
  }

  @Test
  public void validate() throws Exception {
    assertEquals(ChainState.Draft, ChainState.validate("draft"));
    assertEquals(ChainState.Draft, ChainState.validate("Draft"));
    assertEquals(ChainState.Draft, ChainState.validate("Draft5"));
    assertEquals(ChainState.Draft, ChainState.validate(null));
    assertEquals(ChainState.Ready, ChainState.validate("ready"));
    assertEquals(ChainState.Fabricate, ChainState.validate("fabricate"));
    assertEquals(ChainState.Complete, ChainState.validate("complete"));
    assertEquals(ChainState.Failed, ChainState.validate("failed"));
    assertEquals(ChainState.Erase, ChainState.validate("erase"));
  }

}
