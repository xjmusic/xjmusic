package io.xj.lib.mixer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnvelopeTest {
  @Test
  public void at() {
    assertEquals(0.016, Envelope.at(0, 0.643), 0.001);
    assertEquals(0.032, Envelope.at(1, 0.643), 0.001);
    assertEquals(0.048, Envelope.at(2, 0.643), 0.001);
    assertEquals(0.065, Envelope.at(3, 0.643), 0.001);
    assertEquals(0.642, Envelope.at(60, 0.643), 0.001);
  }
}
