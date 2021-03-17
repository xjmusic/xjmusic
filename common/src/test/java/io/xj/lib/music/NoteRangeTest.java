package io.xj.lib.music;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NoteRangeTest {
  NoteRange subject;

  @Before
  public void setUp() {
    subject = new NoteRange(ImmutableList.of(
      Note.of("C3"),
      Note.of("E3"),
      Note.of("D4"),
      Note.of("E5"),
      Note.of("F6")
    ));
  }

  @Test
  public void getLow() {
    assertEquals("C3", subject.getLow().toString(AdjSymbol.None));
  }

  @Test
  public void getHigh() {
    assertEquals("F6", subject.getHigh().toString(AdjSymbol.None));
  }

  @Test
  public void outputToString() {
    assertEquals("C3-F6", subject.toString(AdjSymbol.None));
  }

}
