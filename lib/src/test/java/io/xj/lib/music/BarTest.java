// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.music;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class BarTest {

  @Test
  public void of_getBeats_setBeats() throws MusicalException {
    assertEquals(5, Bar.of(4).setBeats(5).getBeats());
  }

  @Test
  public void of_failsFromNull() {
    var e = assertThrows(MusicalException.class, () -> Bar.of(null));
    assertEquals("Bar must have beats!", e.getMessage());
  }

  @Test
  public void of_failsFromZero() {
    var e = assertThrows(MusicalException.class, () -> Bar.of(0));
    assertEquals("Bar must beats greater than zero!", e.getMessage());
  }

  @Test
  public void of_failsBelowZero() {
    var e = assertThrows(MusicalException.class, () -> Bar.of(-1));
    assertEquals("Bar must beats greater than zero!", e.getMessage());
  }

  @Test
  public void computeSubsectionBeats() throws MusicalException {
    var bar_3beat = Bar.of(3);
    var bar_4beat = Bar.of(4);
    assertEquals(12, bar_3beat.computeSubsectionBeats(12));
    assertEquals(12, bar_3beat.computeSubsectionBeats(12));
    assertEquals(12, bar_3beat.computeSubsectionBeats(24));
    assertEquals(12, bar_3beat.computeSubsectionBeats(48));
    assertEquals(12, bar_4beat.computeSubsectionBeats(12));
    assertEquals(12, bar_4beat.computeSubsectionBeats(12));
    assertEquals(12, bar_4beat.computeSubsectionBeats(24));
    assertEquals(16, bar_4beat.computeSubsectionBeats(16));
    assertEquals(16, bar_4beat.computeSubsectionBeats(16));
    assertEquals(12, bar_4beat.computeSubsectionBeats(48));
    assertEquals(16, bar_4beat.computeSubsectionBeats(64));
    assertEquals(16, bar_4beat.computeSubsectionBeats(64));
    assertEquals(2, bar_3beat.computeSubsectionBeats(2));
    assertEquals(4, bar_3beat.computeSubsectionBeats(4));
  }

}
