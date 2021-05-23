package io.xj.lib.music;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NoteRangeTest {
  NoteRange subject;

  @Before
  public void setUp() {
    subject = new NoteRange(ImmutableList.of(
      "C3",
      "E3",
      "D4",
      "E5",
      "F6"
    ));
  }

  @Test
  public void getLow() {
    assertEquals("C3", subject.getLow().orElseThrow().toString(AdjSymbol.None));
  }

  @Test
  public void getHigh() {
    assertEquals("F6", subject.getHigh().orElseThrow().toString(AdjSymbol.None));
  }

  @Test
  public void copyOf() {
    var cp = NoteRange.copyOf(subject);

    assertEquals("C3", cp.getLow().orElseThrow().toString(AdjSymbol.None));
    assertEquals("F6", cp.getHigh().orElseThrow().toString(AdjSymbol.None));
  }

  @Test
  public void outputToString() {
    assertEquals("C3-F6", subject.toString(AdjSymbol.None));
  }

  @Test
  public void expand() {
    subject.expand(Note.of("G2"));

    assertEquals("G2", subject.getLow().orElseThrow().toString(AdjSymbol.None));
    assertEquals("F6", subject.getHigh().orElseThrow().toString(AdjSymbol.None));
  }

  @Test
  public void expand_byNotes() {
    subject.expand(ImmutableSet.of(
      Note.of("G2"),
      Note.of("G6")
    ));

    assertEquals("G2", subject.getLow().orElseThrow().toString(AdjSymbol.None));
    assertEquals("G6", subject.getHigh().orElseThrow().toString(AdjSymbol.None));
  }

  @Test
  public void expandWithAnotherRange() {
    subject.expand(new NoteRange(ImmutableSet.of(
      "G2",
      "G6"
    )));

    assertEquals("G2", subject.getLow().orElseThrow().toString(AdjSymbol.None));
    assertEquals("G6", subject.getHigh().orElseThrow().toString(AdjSymbol.None));
  }
}
