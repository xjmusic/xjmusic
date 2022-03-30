// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.music;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static io.xj.lib.music.NoteTest.assertNote;
import static org.junit.Assert.*;

/**
 Sticky buns v2 https://www.pivotaltracker.com/story/show/179153822 persisted for each randomly selected note in the series for any given pattern
 */
public class StickyBunTest {
  private final UUID patternId = UUID.randomUUID();
  StickyBun subject;
  UUID eventId0;

  @Before
  public void setUp() throws Exception {
    eventId0 = UUID.randomUUID();

    subject = new StickyBun(patternId, Note.of("C5"));
  }

  /**
   super-key on program-sequence-pattern id, measuring delta from the first event seen in that pattern
   */
  @Test
  public void getParentId() {
    assertEquals(patternId, subject.getParentId());
  }

  /**
   key on program-sequence-pattern-event id, persisting only the first value seen for any given event
   */
  @Test
  public void addNotes_getOffsets() {
    UUID eventId1 = UUID.randomUUID();
    UUID eventId2 = UUID.randomUUID();

    subject.put(eventId0, 0.0, Note.of("C5"));
    subject.put(eventId1, 1.0, Note.of("G5"));
    subject.put(eventId2, 1.5, Note.of("F5"));
    subject.put(eventId2, 1.5, Note.of("Bb5"));

    assertEquals(List.of(5, 10), subject.getOffsets(eventId2));
  }

  /**
   Sticky bun member only remembers offsets from the first occurrence of any given event
   */
  @Test
  public void addNotes_discardsLaterNotes() {
    subject.put(eventId0, 0.0, Note.of("C5"));
    subject.put(eventId0, 0.0, Note.of("E5"));
    subject.put(eventId0, 0.0, Note.of("G5"));
    subject.put(eventId0, 1.0, Note.of("D5"));
    subject.put(eventId0, 1.0, Note.of("F#5"));
    subject.put(eventId0, 1.0, Note.of("A5"));

    assertEquals(List.of(0, 4, 7), subject.getOffsets(eventId0));
  }

  @Test
  public void replaceAtonal() {
    subject.put(eventId0, 0.0, Note.of("C5"));
    subject.put(eventId0, 0.0, Note.of("E5"));
    subject.put(eventId0, 0.0, Note.of("G5"));

    var result = subject.replaceAtonal(eventId0, Note.of("F3"), List.of(
      Note.of("F2"),
      Note.of("X"),
      Note.of("X")
    ));

    assertNote("F2", result.get(0));
    assertNote("A3", result.get(1));
    assertNote("C4", result.get(2));
  }

  @Test
  public void isTonal() {
    subject.put(eventId0, 0.0, Note.of("C5"));

    assertTrue(subject.isTonal(eventId0));
  }

  @Test
  public void isTonal_notIfAtonal() {
    subject.put(eventId0, 0.0, Note.of("X"));

    assertFalse(subject.isTonal(eventId0));
  }
}
