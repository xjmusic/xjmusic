// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.entity;// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import io.xj.core.exception.CoreException;
import io.xj.core.model.Library;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramSequenceBindingMeme;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

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

  @Test
  public void set() throws CoreException {
    UUID newId = UUID.randomUUID();

    sequenceBinding0.set("programSequenceId", newId);
    sequenceA.set("name", "Funky Chicken");

    assertEquals(newId, sequenceBinding0.getProgramSequenceId());
    assertEquals("Funky Chicken", sequenceA.getName());
  }

  @Test
  public void set_nonexistentAttribute() throws CoreException {
    failure.expect(CoreException.class);
    failure.expectMessage("Sequence has no attribute 'turnip'");

    sequenceA.set("turnip", 4.2);
  }

  @Test
  public void setAllResourceAttributes() {
    sequenceA.setAllResourceAttributes(sequenceB);

    assertEquals("Exploding", sequenceA.getName());
    assertEquals(4L, (long) sequenceA.getTotal());
    assertEquals(0.5, sequenceA.getDensity(), 0.01);
    assertEquals("B", sequenceA.getKey());
    assertEquals(123.0, sequenceA.getTempo(), 0.01);
  }

  /**
   [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.
   */
  @Test
  public void roundPosition() {
    SegmentChord chord = SegmentChord.create(Segment.create(), 5.3589897490, "C");

    assertEquals(5.35, chord.getPosition(), 0.0000001);
  }


/*

FUTURE tests



  @Test
  public void extractPrimaryObject() {
  }

  @Test
  public void add() {
  }

  @Test
  public void belongsTo() {
  }

  @Test
  public void consume() {
  }

  @Test
  public void consume1() {
  }

  @Test
  public void get() {
  }

  @Test
  public void get1() {
  }

  @Test
  public void getAllSubEntities() {
  }

  @Test
  public void getErrors() {
  }

  @Test
  public void getResourceAttributes() {
  }

  @Test
  public void getResourceBelongsTo() {
  }

  @Test
  public void getResourceHasMany() {
  }

  @Test
  public void getResourceType() {
  }

  @Test
  public void getURI() {
  }

  @Test
  public void isEmpty() {
  }

  @Test
  public void require() {
  }

  @Test
  public void require1() {
  }

  @Test
  public void requireNo() {
  }

  @Test
  public void set1() {
  }

  @Test
  public void toPayloadObject() {
  }

  @Test
  public void toPayloadObject1() {
  }

  @Test
  public void toPayloadReferenceObject() {
  }

 */

}
