package io.xj.core.model.entity.impl;

import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.program.sub.SequenceBindingMeme;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ResourceEntityImplTest extends CoreTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  Program program;
  Sequence sequenceA;
  Sequence sequenceB;
  SequenceBinding sequenceBinding0;
  SequenceBinding sequenceBinding1;
  SequenceBinding sequenceBinding2;
  SequenceBindingMeme sequenceBindingMeme0;
  SequenceBindingMeme sequenceBindingMeme1;
  SequenceBindingMeme sequenceBindingMeme2;

  @Before
  public void setUp() {
    program = newProgram(12, 101, 3, ProgramType.Main, ProgramState.Published, "Earth to Fire", "Ebm", 121.0, now());
    sequenceA = program.add(newSequence(8, "Passion Volcano", 0.6, "Ebm", 121.0));
    sequenceB = program.add(newSequence(4, "Exploding", 0.5, "B", 123.0));
    sequenceBinding0 = program.add(newSequenceBinding(sequenceA, 0));
    sequenceBinding1 = program.add(newSequenceBinding(sequenceB, 1));
    sequenceBinding2 = program.add(newSequenceBinding(sequenceA, 2));
    sequenceBindingMeme0 = program.add(newSequenceBindingMeme(sequenceBinding0, "Earth"));
    sequenceBindingMeme1 = program.add(newSequenceBindingMeme(sequenceBinding1, "Fire"));
    sequenceBindingMeme2 = program.add(newSequenceBindingMeme(sequenceBinding2, "Fire"));
  }

  @Test
  public void set() throws CoreException {
    UUID newId = UUID.randomUUID();

    sequenceBinding0.set("sequenceId", newId);
    sequenceA.set("name", "Funky Chicken");

    assertEquals(newId, sequenceBinding0.getSequenceId());
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
