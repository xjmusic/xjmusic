// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.doc;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DocTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Doc()
      .setKey("chain-info")
      .validate();
  }

  @Test
  public void nameGeneratedWhenKeySet() throws Exception {
    assertEquals("Chain Info", new Doc()
      .setKey("chain-info")
      .getName());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Doc().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    assertNotNull(new Doc().updatableFieldValueMap());
  }

}
