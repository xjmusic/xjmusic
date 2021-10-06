package io.xj.lib.music;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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
    assertTrue(Note.of("C3").sameAs(subject.getLow().orElseThrow()));
  }

  @Test
  public void getHigh() {
    assertTrue(Note.of("F6").sameAs(subject.getHigh().orElseThrow()));
  }

  @Test
  public void rangeFromNotes() {
    subject = new NoteRange(Note.of("C3"), Note.of("C4"));

    assertTrue(Note.of("C3").sameAs(subject.getLow().orElseThrow()));
    assertTrue(Note.of("C4").sameAs(subject.getHigh().orElseThrow()));
  }

  @Test
  public void rangeFromNotes_lowIsOptional() {
    subject = new NoteRange(null, Note.of("C4"));

    assertFalse(subject.getLow().isPresent());
    assertTrue(Note.of("C4").sameAs(subject.getHigh().orElseThrow()));
  }

  @Test
  public void rangeFromNotes_highIsOptional() {
    subject = new NoteRange(Note.of("C4"), null);

    assertTrue(Note.of("C4").sameAs(subject.getLow().orElseThrow()));
    assertFalse(subject.getHigh().isPresent());
  }

  @Test
  public void rangeFromStrings() {
    subject = new NoteRange("C3", "C4");

    assertTrue(Note.of("C3").sameAs(subject.getLow().orElseThrow()));
    assertTrue(Note.of("C4").sameAs(subject.getHigh().orElseThrow()));
  }

  @Test
  public void rangeFromStrings_lowOptional() {
    subject = new NoteRange(null, "C4");

    assertFalse(subject.getLow().isPresent());
    assertTrue(Note.of("C4").sameAs(subject.getHigh().orElseThrow()));
  }

  @Test
  public void rangeFromStrings_highOptional() {
    subject = new NoteRange("C4", null);

    assertTrue(Note.of("C4").sameAs(subject.getLow().orElseThrow()));
    assertFalse(subject.getHigh().isPresent());
  }

  @Test
  public void copyOf() {
    var cp = NoteRange.copyOf(subject);

    assertTrue(Note.of("C3").sameAs(cp.getLow().orElseThrow()));
    assertTrue(Note.of("F6").sameAs(cp.getHigh().orElseThrow()));
  }

  @Test
  public void outputToString() {
    assertEquals("C3-F6", subject.toString(AdjSymbol.None));
  }

  @Test
  public void expand() {
    subject.expand(Note.of("G2"));

    assertTrue(Note.of("G2").sameAs(subject.getLow().orElseThrow()));
    assertTrue(Note.of("F6").sameAs(subject.getHigh().orElseThrow()));
  }

  @Test
  public void expand_byNotes() {
    subject.expand(ImmutableSet.of(
      Note.of("G2"),
      Note.of("G6")
    ));

    assertTrue(Note.of("G2").sameAs(subject.getLow().orElseThrow()));
    assertTrue(Note.of("G6").sameAs(subject.getHigh().orElseThrow()));
  }

  @Test
  public void expandWithAnotherRange() {
    subject.expand(new NoteRange(ImmutableSet.of(
      "G2",
      "G6"
    )));

    assertTrue(Note.of("G2").sameAs(subject.getLow().orElseThrow()));
    assertTrue(Note.of("G6").sameAs(subject.getHigh().orElseThrow()));
  }
}
