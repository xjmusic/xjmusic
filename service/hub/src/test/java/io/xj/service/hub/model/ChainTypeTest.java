// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.model;// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChainTypeTest {
  @Test
  public void TypetoString() throws Exception {
    assertEquals("Preview", ChainType.Preview.toString());
    assertEquals("Production", ChainType.Production.toString());
  }

  @Test
  public void validate() throws Exception {
    assertEquals(ChainType.Preview, ChainType.validate("preview"));
    assertEquals(ChainType.Preview, ChainType.validate("Preview"));
    assertEquals(ChainType.Preview, ChainType.validate("Preview5"));
    assertEquals(ChainType.Preview, ChainType.validate(null));
    assertEquals(ChainType.Production, ChainType.validate("production"));
  }

}
