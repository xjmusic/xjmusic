// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.entity;// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import io.xj.service.hub.model.Library;
import io.xj.service.hub.model.Program;
import io.xj.service.hub.model.ProgramSequence;
import io.xj.service.hub.model.ProgramSequenceBinding;
import io.xj.service.hub.model.ProgramSequenceBindingMeme;
import io.xj.service.hub.model.ProgramState;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChord;
import io.xj.service.hub.model.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class EntityTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();
  Program program;
  ProgramSequence sequenceA;
  ProgramSequence sequenceB;
  ProgramSequenceBinding sequenceBinding0;
  ProgramSequenceBinding sequenceBinding1;
  ProgramSequenceBinding sequenceBinding2;
  ProgramSequenceBindingMeme sequenceBindingMeme0;
  ProgramSequenceBindingMeme sequenceBindingMeme1;
  ProgramSequenceBindingMeme sequenceBindingMeme2;

  @Before
  public void setUp() {
    program = Program.create(User.create(), Library.create(), ProgramType.Main, ProgramState.Published, "Earth to Fire", "Ebm", 121.0, 0.6);
    sequenceA = ProgramSequence.create(program, 4, "Passion Volcano", 0.6, "Ebm", 121.0);
    sequenceB = ProgramSequence.create(program, 4, "Exploding", 0.5, "B", 123.0);
    sequenceBinding0 = ProgramSequenceBinding.create(sequenceA, 0);
    sequenceBinding1 = ProgramSequenceBinding.create(sequenceB, 1);
    sequenceBinding2 = ProgramSequenceBinding.create(sequenceA, 2);
    sequenceBindingMeme0 = ProgramSequenceBindingMeme.create(sequenceBinding0, "Earth");
    sequenceBindingMeme1 = ProgramSequenceBindingMeme.create(sequenceBinding1, "Fire");
    sequenceBindingMeme2 = ProgramSequenceBindingMeme.create(sequenceBinding2, "Fire");
  }

  /**
   [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.
   */
  @Test
  public void roundPosition() {
    SegmentChord chord = SegmentChord.create(Segment.create(), 5.3589897490, "C");

    assertEquals(5.35, chord.getPosition(), 0.0000001);
  }

}
