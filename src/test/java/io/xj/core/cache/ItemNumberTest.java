// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.cache;// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ItemNumberTest {
  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void next() throws Exception {
    assertThat(ItemNumber.next(), is(1));
    assertThat(ItemNumber.next(), is(2));
    assertThat(ItemNumber.next(), is(3));
  }

}
