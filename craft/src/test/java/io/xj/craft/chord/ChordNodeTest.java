// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.craft.chord;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import io.xj.core.CoreTest;
import io.xj.core.util.Value;
import io.xj.music.Key;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ChordNodeTest extends CoreTest {

  /**
   [#158715321] ChordEntity nodes able to parse No ChordEntity notation
   */
  @Test
  public void instantiate_NoChord_fromKey() {
    ChordNode noChord = new ChordNode(Key.of("C"), newSequenceChord(2.0, "NC"));
    assertNotNull(noChord);
    assertEquals(Value.CHORD_MARKER_NON_CHORD, noChord.toString());
  }

  /**
   [#158715321] ChordEntity nodes able to parse No ChordEntity notation
   */
  @Test
  public void instantiate_NoChord_fromPrevious() {
    ChordNode noChord = new ChordNode(newSequenceChord(2.0, "G"), newSequenceChord(2.0, "NC"));
    assertNotNull(noChord);
    assertEquals(Value.CHORD_MARKER_NON_CHORD, noChord.toString());
  }

  @Test
  public void getForm() {
    assertEquals("Major",
      new ChordNode("Major").getForm());

    assertEquals("Major",
      new ChordNode("7|Major").getForm());

    assertEquals("Major",
      new ChordNode(newSequenceChord(1.0, "C Minor"),
        newSequenceChord(2.0, "G Major")).getForm());

    assertEquals("Major",
      new ChordNode(newSequenceChord(2.0, "G Major")).getForm());
  }

  @Test
  public void getWeight() {
    assertEquals(Long.valueOf(1),
      new ChordNode("Major").getWeight());

    assertEquals(Long.valueOf(1),
      new ChordNode("7|Major").getWeight());

    assertEquals(Long.valueOf(1),
      new ChordNode(newSequenceChord(1.0, "C Minor"),
        newSequenceChord(2.0, "G Major")).getWeight());

    assertEquals(Long.valueOf(1),
      new ChordNode(newSequenceChord(2.0, "G Major")).getWeight());
  }

  @Test
  public void addWeight() {
    ChordNode subject = new ChordNode("7|Major");
    subject.addWeight(new ChordNode("5|Major"));
    subject.addWeight(new ChordNode("4|Minor"));
    assertEquals(Long.valueOf(3), subject.getWeight());
  }

  @Test
  public void getDelta() {
    assertNull(new ChordNode("Major").getDelta());

    assertNull(new ChordNode("NC").getDelta());

    assertEquals(Integer.valueOf(7),
      new ChordNode("7|Major").getDelta());

    assertEquals(Integer.valueOf(7),
      new ChordNode(newSequenceChord(1.0, "C Minor"),
        newSequenceChord(2.0, "G Major")).getDelta());

    assertNull(new ChordNode(newSequenceChord(2.0, "G Major")).getDelta());
  }

  @Test
  public void descriptorToString() {
    assertEquals("Major",
      new ChordNode("Major").toString());

    assertEquals("7|Major",
      new ChordNode("7|Major").toString());

    assertEquals("7|Major",
      new ChordNode(newSequenceChord(1.0, "C Minor"),
        newSequenceChord(2.0, "G Major")).toString());

    assertEquals("Major",
      new ChordNode(newSequenceChord(2.0, "G Major")).toString());
  }

  @Test
  public void isEquivalentTo() {
    assertTrue(new ChordNode("Major").isEquivalentTo(new ChordNode("Major")));
    assertTrue(new ChordNode("11|Major").isEquivalentTo(new ChordNode("Major")));
    assertTrue(new ChordNode("Major").isEquivalentTo(new ChordNode("9|Major")));
    assertFalse(new ChordNode("Minor").isEquivalentTo(new ChordNode("9|Major")));
    assertTrue(new ChordNode("7|Major").isEquivalentTo(new ChordNode("7|Major")));
    assertFalse(new ChordNode("8|Major").isEquivalentTo(new ChordNode("7|Major")));
    assertTrue(new ChordNode(newSequenceChord(1.0, "C Minor"),
      newSequenceChord(2.0, "G Major")).isEquivalentTo(new ChordNode("7|Major")));
    assertTrue(new ChordNode(newSequenceChord(2.0, "G Major")).isEquivalentTo(new ChordNode("Major")));
  }

  @Test
  public void bookendMarkerPatternHasEnded() {
    // This is used as a "bookend" marker, e.g. meaning "pattern has ended" during chord markov computation
    ChordNode subject = new ChordNode();

    assertNull(subject.getDelta());
    assertNull(subject.getForm());
    assertEquals("---", subject.toString());
  }

}
