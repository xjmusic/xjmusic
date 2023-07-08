// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.mixer;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnvelopeProviderTest {
  EnvelopeProvider subject;

  @Before
  public void setup() {
    subject = new EnvelopeProviderImpl();
  }

  @Test
  public void in() {
    assertEquals(0.016, subject.length(62).in(0, 0.643), 0.001);
    assertEquals(0.032, subject.length(62).in(1, 0.643), 0.001);
    assertEquals(0.048, subject.length(62).in(2, 0.643), 0.001);
    assertEquals(0.065, subject.length(62).in(3, 0.643), 0.001);
    assertEquals(0.642, subject.length(62).in(60, 0.643), 0.001);
    assertEquals(0, subject.length(62).in(-1, 0.643), 0.001);
  }

  @Test
  public void out() {
    assertEquals(0.643, subject.length(62).out(-1, 0.643), 0.001);
    assertEquals(0.643, subject.length(62).out(0, 0.643), 0.001);
    assertEquals(0.6428, subject.length(62).out(1, 0.643), 0.0001);
    assertEquals(0.6422, subject.length(62).out(2, 0.643), 0.0001);
    assertEquals(0.641, subject.length(62).out(3, 0.643), 0.001);
    assertEquals(0.0325, subject.length(62).out(60, 0.643), 0.001);
    assertEquals(0.00, subject.length(62).out(65, 0.643), 0.001);
  }

}
