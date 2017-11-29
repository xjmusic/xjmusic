package io.xj.core.cache.audio;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

import io.xj.core.cache.audio.impl.ItemNumber;

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
    assertThat( ItemNumber.next(), is(1));
    assertThat( ItemNumber.next(), is(2));
    assertThat( ItemNumber.next(), is(3));
  }

}
