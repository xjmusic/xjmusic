// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.music;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 Sticky buns v2 https://www.pivotaltracker.com/story/show/179153822 persisted for each randomly selected note in the series for any given event
 */
public class StickyBunTest {
  private final UUID eventId = UUID.randomUUID();
  StickyBun subject;
  UUID eventId0;

  @Before
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
   Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787
   */
  @Test
  public void computeMetaKey() {
    var eventId = UUID.fromString("0f650ae7-42b7-4023-816d-168759f37d2e");
    assertEquals("StickyBun_0f650ae7-42b7-4023-816d-168759f37d2e", new StickyBun(eventId).computeMetaKey());
  }

  /**
   Replace atonal members
   */
  @Test
  public void replaceAtonal() {
    var eventId = UUID.fromString("0f650ae7-42b7-4023-816d-168759f37d2e");
    var source = List.of(Note.of("Bb7"), Note.of("X"), Note.of("X"), Note.of("X"));
    var voicingNotes = List.of(Note.of("C4"), Note.of("E5"), Note.of("G6"), Note.of("Bb7"));
    var bun = new StickyBun(eventId, 4);

    var result = bun.replaceAtonal(source, voicingNotes);

    assertTrue(Note.of("Bb7").sameAs(result.get(0)));
    for (var n : result)
      assertTrue(voicingNotes.stream().anyMatch(vn -> vn.sameAs(n)));
  }
}
