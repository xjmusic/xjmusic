// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.music;

import org.junit.Test;

import static io.outright.xj.music.PitchClass.C;
import static io.outright.xj.music.PitchClass.G;
import static org.junit.Assert.assertEquals;

public class NoteTest {

  @Test
  public void NamedTest() {
    Note note = Note.of("G");
    assertEquals(G, note.getPitchClass());
  }

  @Test
  public void OfPitchClassTest() {
    Note note = Note.of(C, 5);
    assertEquals(Integer.valueOf(5), note.getOctave());
    assertEquals(C, note.getPitchClass());
  }

}
