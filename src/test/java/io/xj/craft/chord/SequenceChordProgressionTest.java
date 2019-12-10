// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.craft.chord;// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableList;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.music.PitchClass;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class SequenceChordProgressionTest {

  private ProgramSequence programSequence1;
  private ProgramSequence programSequence2;

  // Assert two lists of pattern entities are equivalent
  private static void assertEquivalent(List<ProgramSequenceChord> o1, List<ProgramSequenceChord> o2) {
    int size = o1.size();
    assertEquals(size, o2.size());
    for (int n = 0; n < size; n++)
      assertEquivalent(o1.get(n), o2.get(n));
  }

  // Assert two pattern entities are equivalent
  private static void assertEquivalent(ProgramSequenceChord o1, ProgramSequenceChord o2) {
    assertEquals(o1.getProgramSequenceId(), o2.getProgramSequenceId());
    assertEquals(o1.getPosition(), o2.getPosition());
    assertEquals(o1.getName(), o2.getName());
  }

  @Before
  public void setUp() {
    programSequence1 = ProgramSequence.create();
    programSequence2 = ProgramSequence.create();
  }

  @Test
  public void getDescriptor_basic() {
    SequenceChordProgression subjectA = new SequenceChordProgression(programSequence1, ImmutableList.of(
      new ProgramSequenceChord(programSequence1).setPosition(0.0).setName("D").setProgramSequenceId(UUID.randomUUID()),
      new ProgramSequenceChord(programSequence1).setPosition(2.0).setName("A").setProgramSequenceId(UUID.randomUUID()),
      new ProgramSequenceChord(programSequence1).setPosition(7.0).setName("B").setProgramSequenceId(UUID.randomUUID())
    ));
    SequenceChordProgression subjectB = new SequenceChordProgression(programSequence2, ImmutableList.of(
      new ProgramSequenceChord(programSequence2).setPosition(4.0).setName("F").setProgramSequenceId(UUID.randomUUID()),
      new ProgramSequenceChord(programSequence2).setPosition(6.0).setName("C").setProgramSequenceId(UUID.randomUUID()),
      new ProgramSequenceChord(programSequence2).setPosition(11.0).setName("D").setProgramSequenceId(UUID.randomUUID())
    ));

    // Note: These two end up having identical descriptors, because the entities are positioned identically relative to each other
    assertEquals("Major:7|Major:2|Major", subjectA.getChordProgression().toString());
    assertEquals("Major:7|Major:2|Major", subjectB.getChordProgression().toString());
  }

  @Test
  public void getDescriptor_moreComplex() {
    SequenceChordProgression subjectA = new SequenceChordProgression(programSequence1, ImmutableList.of(
      new ProgramSequenceChord(programSequence1).setPosition(0.0).setName("D7+9").setProgramSequenceId(UUID.randomUUID()),
      new ProgramSequenceChord(programSequence1).setPosition(2.0).setName("Am7").setProgramSequenceId(UUID.randomUUID()),
      new ProgramSequenceChord(programSequence1).setPosition(7.0).setName("B7+5").setProgramSequenceId(UUID.randomUUID())
    ));
    SequenceChordProgression subjectB = new SequenceChordProgression(programSequence2, ImmutableList.of(
      new ProgramSequenceChord(programSequence2).setPosition(4.0).setName("F7+9").setProgramSequenceId(UUID.randomUUID()),
      new ProgramSequenceChord(programSequence2).setPosition(6.0).setName("Cm7").setProgramSequenceId(UUID.randomUUID()),
      new ProgramSequenceChord(programSequence2).setPosition(11.0).setName("D7+5").setProgramSequenceId(UUID.randomUUID())
    ));

    // Note: These two end up having identical descriptors, because the entities are positioned identically relative to each other
    assertEquals("Major Seventh Add Ninth:7|Minor Seventh:2|Major Seventh", subjectA.getChordProgression().toString());
    assertEquals("Major Seventh Add Ninth:7|Minor Seventh:2|Major Seventh", subjectB.getChordProgression().toString());
  }

  @Test
  public void getRootChord() {
    SequenceChordProgression subjectA = new SequenceChordProgression(programSequence1, ImmutableList.of(
      new ProgramSequenceChord(programSequence1).setPosition(0.0).setName("D").setProgramSequenceId(UUID.randomUUID()),
      new ProgramSequenceChord(programSequence1).setPosition(2.0).setName("A").setProgramSequenceId(UUID.randomUUID()),
      new ProgramSequenceChord(programSequence1).setPosition(7.0).setName("B").setProgramSequenceId(UUID.randomUUID())
    ));
    SequenceChordProgression subjectB = new SequenceChordProgression(programSequence2, ImmutableList.of(
      new ProgramSequenceChord(programSequence2).setPosition(4.0).setName("F").setProgramSequenceId(UUID.randomUUID()),
      new ProgramSequenceChord(programSequence2).setPosition(6.0).setName("C").setProgramSequenceId(UUID.randomUUID()),
      new ProgramSequenceChord(programSequence2).setPosition(11.0).setName("D").setProgramSequenceId(UUID.randomUUID())
    ));

    // Note: These two end up having identical descriptors, because the entities are positioned identically relative to each other
    assertEquals("D", subjectA.getRootChord().getName());
    assertEquals("F", subjectB.getRootChord().getName());
  }

  @Test
  public void createFromProgression() {
    assertEquivalent(
      ImmutableList.of(
        ProgramSequenceChord.create(programSequence1, 0.0, "F Major Seventh Add Ninth"),
        ProgramSequenceChord.create(programSequence1, 4.0, "C Minor Seventh"),
        ProgramSequenceChord.create(programSequence1, 8.0, "D Major Seventh"),
        ProgramSequenceChord.create(programSequence1, 12.0, "Bb Minor Flat Nine")
      ),
      new SequenceChordProgression(
        new ChordProgression("0|Major Seventh Add Ninth:7|Minor Seventh:9|Major Seventh:5|Minor Flat Nine"),
        programSequence1,
        PitchClass.F,
        4.0
      ).getChords());

    assertEquivalent(
      ImmutableList.of(
        ProgramSequenceChord.create(programSequence2, 0.0, "B Minor Flat Five"),
        ProgramSequenceChord.create(programSequence2, 4.0, "F# Major Seven"),
        ProgramSequenceChord.create(programSequence2, 8.0, "F Major Seven Flat Nine")
      ),
      new SequenceChordProgression(
        new ChordProgression("0|Minor Flat Five:7|Major Seven:6|Major Seven Flat Nine"),
        programSequence2,
        PitchClass.B,
        4.0
      ).getChords());
  }

}
