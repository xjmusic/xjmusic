// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model.music;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.xj.model.music.NoteTest.assertNote;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 Sticky buns v2 persisted for each randomly selected note in the series for any given event https://github.com/xjmusic/xjmusic/issues/231
 */
public class StickyBunTest {
  final UUID eventId = UUID.randomUUID();
  StickyBun subject;
  UUID eventId0;

  @BeforeEach
  public void setUp() throws Exception {
    eventId0 = UUID.randomUUID();

    subject = new StickyBun(eventId, 3);
  }

  @Test
  public void getValues() {
    assertEquals(3, subject.getValues().size());
  }

  /**
   super-key on program-sequence-event id, measuring delta from the first event seen in that event
   */
  @Test
  public void getParentId() {
    assertEquals(eventId, subject.getEventId());
  }

  /**
   Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://github.com/xjmusic/xjmusic/issues/222
   */
  @Test
  public void computeMetaKey() {
    var eventId = UUID.fromString("0f650ae7-42b7-4023-816d-168759f37d2e");
    assertEquals("StickyBun_0f650ae7-42b7-4023-816d-168759f37d2e", new StickyBun(eventId, 1).computeMetaKey());
    assertEquals("StickyBun_0f650ae7-42b7-4023-816d-168759f37d2e", StickyBun.computeMetaKey(eventId));
  }

  /**
   Replace any number of members of the set, when atonal, by computing the sticky bun
   */
  @Test
  public void replaceAtonal() {
    var eventId = UUID.fromString("0f650ae7-42b7-4023-816d-168759f37d2e");
    var source = List.of(Note.of("Bb7"), Note.of("X"), Note.of("X"), Note.of("X"));
    var voicingNotes = List.of(Note.of("C4"), Note.of("E5"), Note.of("G6"), Note.of("Bb7"));
    var bun = new StickyBun(eventId, List.of(42, 67, 100, 0));

    var result = bun.replaceAtonal(source, voicingNotes);

    assertNote("Bb7", result.get(0));
    assertNote("G6", result.get(1));
    assertNote("Bb7", result.get(2));
    assertNote("C4", result.get(3));
  }

  /**
   Pick one
   */
  @Test
  public void compute() {
    var eventId = UUID.fromString("0f650ae7-42b7-4023-816d-168759f37d2e");
    var voicingNotes = List.of(Note.of("C4"), Note.of("E5"), Note.of("G6"), Note.of("Bb7"));
    var bun = new StickyBun(eventId, List.of(42, 67, 100, 0));

    assertNote("E5", bun.compute(voicingNotes, 0));
    assertNote("G6", bun.compute(voicingNotes, 1));
    assertNote("Bb7", bun.compute(voicingNotes, 2));
    assertNote("C4", bun.compute(voicingNotes, 3));
  }

  /**
   Construct empty
   */
  @Test
  public void constructEmpty() {
    var b = UUID.randomUUID();

    var e = new StickyBun();
    e.setEventId(b);

    assertEquals(b, e.getEventId());
  }
}
