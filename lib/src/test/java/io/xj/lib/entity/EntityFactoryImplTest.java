// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import io.xj.api.Program;
import io.xj.api.ProgramState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class EntityFactoryImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private EntityFactory subject;

  @Before
  public void setUp() {
    var injector = Guice.createInjector(new EntityModule());
    subject = injector.getInstance(EntityFactory.class);
  }

  @Test
  public void register_returnsSameSchema_forExistingType() {
    subject.register("Program").createdBy(Program::new).belongsTo("OtherThing");

    assertEquals(ImmutableSet.of("otherThing"), subject.register("programs").getBelongsTo());
  }

  @Test
  public void register_returnsSameSchema_forExistingTypeClass() {
    subject.register(Program.class).createdBy(Program::new).belongsTo("OtherThing");

    assertEquals(ImmutableSet.of("otherThing"), subject.register(Program.class).getBelongsTo());
  }

  @Test
  public void register_returnsSameSchema_forExisting_TypeThenClass() {
    subject.register("Program").createdBy(Program::new).belongsTo("OtherThing");

    assertEquals(ImmutableSet.of("otherThing"), subject.register(Program.class).getBelongsTo());
  }

  @Test
  public void register_returnsSameSchema_forExisting_ClassThenType() {
    subject.register(Program.class).createdBy(Program::new).belongsTo("OtherThing");

    assertEquals(ImmutableSet.of("otherThing"), subject.register("Program").getBelongsTo());
  }

  @Test
  public void register_basicTypeCreator() throws EntityException {
    subject.register("Program").createdBy(Program::new);

    assertEquals(Program.class, subject.getInstance("program").getClass());
  }

  @Test
  public void register_withBelongsTo() throws EntityException {
    subject.register("Program").belongsTo("FictionalEntity");

    assertEquals(ImmutableSet.of("fictionalEntity"), subject.getBelongsTo("programs"));
  }

  @Test
  public void register_withAttributesAndBelongsTo() throws EntityException {
    subject.register("Program");
    subject.register("FictionalEntity").withAttribute("name").belongsTo("library").createdBy(Program::new);

    assertEquals(ImmutableSet.of("library"),
      subject.getBelongsTo("fictional-entity"));

    assertEquals(ImmutableSet.of("name"),
      subject.getAttributes("fictional-entity"));
  }

  @Test
  public void register_withAttributes() throws EntityException {
    subject.register("FictionalEntity").withAttribute("name").createdBy(Program::new);

    assertEquals(ImmutableSet.of("name"), subject.getAttributes("fictional-entity"));
  }

  @Test
  public void getBelongsToType() throws EntityException {
    subject.register("OtherEntity");
    subject.register("FakeEntity").belongsTo("otherEntity").createdBy(Program::new);

    assertEquals(ImmutableSet.of("otherEntity"), subject.getBelongsTo("fake-entity"));
  }

  @Test
  public void getBelongsToType_emptyBelongsTo() throws EntityException {
    subject.register("OtherEntity");

    assertTrue(subject.getBelongsTo("other-entity").isEmpty());
  }

  @Test
  public void getBelongsToType_exceptionIfDoesNotExist() throws EntityException {
    failure.expect(EntityException.class);
    failure.expectMessage("Cannot get belongs-to type unknown type: other-entities");

    subject.getBelongsTo("other-entity");
  }

  @Test
  public void getAttributes() throws EntityException {
    subject.register("FalseEntity").withAttribute("yarn").createdBy(Program::new);

    assertEquals(ImmutableSet.of("yarn"), subject.getAttributes("false-entity"));
  }


  @Test
  public void testClone() throws EntityException {
    subject.register("Program")
      .withAttributes("name")
      .belongsTo("library")
      .createdBy(Program::new);
    Program from = new Program().name("Flight")
      .libraryId(UUID.randomUUID())
      ;

    Program result = subject.clone(from);

    Assert.assertEquals("Flight", result.getName());
    Assert.assertEquals(from.getLibraryId(), result.getLibraryId());
    Assert.assertEquals(from.getId(), result.getId());
    assertNotSame(result, from);
  }

  @Test
  public void testClone_withState() throws EntityException {
    subject.register("Program")
      .withAttributes("name", "state")
      .belongsTo("library")
      .createdBy(Program::new);
    Program from = new Program()
      .name("Flight")
      .state(ProgramState.PUBLISHED)
      ;

    Program result = subject.clone(from);

    Assert.assertEquals("Flight", result.getName());
    Assert.assertEquals(from.getState(), result.getState());
    Assert.assertEquals(from.getId(), result.getId());
    assertNotSame(result, from);
  }


  /**
   This should ostensibly be a test inside the Entity library-- and it is, except for this bug that
   at the time of this writing, we couldn't isolate to that library, and are therefore reproducing it here.

   @throws EntityException on failure
   */
  @Test
  public void internal_entityFactoryClonesProgramTypeOK() throws EntityException {
    // Some topology
    subject.register("Program");
    subject.register("Program")
      .withAttributes("name", "state")
      .belongsTo("library")
      .createdBy(Program::new);
    Program program = new Program()
      .id(UUID.fromString("ac5eba0a-f725-4831-9ff2-a8d92a73a09d"))
      .state(ProgramState.PUBLISHED);

    Program result = subject.clone(program);

    Assert.assertEquals(ProgramState.PUBLISHED, result.getState());
  }

  @Test
  public void testClone_withNullBelongsToId() throws EntityException {
    subject.register("Program").withAttribute("name").belongsTo("library").createdBy(Program::new);
    Program from = new Program()
      .name("Flight")
      ;

    Program result = subject.clone(from);

    Assert.assertEquals("Flight", result.getName());
    Assert.assertEquals(from.getId(), result.getId());
    assertNotSame(result, from);
  }

  @Test
  public void testCloneAll() throws EntityException {
    subject.register("Program")
      .withAttribute("name")
      .belongsTo("library")
      .createdBy(Program::new);
    Program fromA = new Program()
      .name("Air")
      .libraryId(UUID.randomUUID())
      ;
    Program fromB = new Program()
      .name("Ground")
      .libraryId(UUID.randomUUID())
      ;

    Collection<Program> result = subject.cloneAll(ImmutableList.of(fromA, fromB));

    assertEquals(2, result.size());
    Iterator<Program> resultIt = result.iterator();
    //
    Program resultA = resultIt.next();
    Assert.assertEquals("Air", resultA.getName());
    Assert.assertEquals(fromA.getLibraryId(), resultA.getLibraryId());
    Assert.assertEquals(fromA.getId(), resultA.getId());
    assertNotSame(resultA, fromA);
    //
    Program resultB = resultIt.next();
    Assert.assertEquals("Ground", resultB.getName());
    Assert.assertEquals(fromB.getLibraryId(), resultB.getLibraryId());
    Assert.assertEquals(fromB.getId(), resultB.getId());
    assertNotSame(resultB, fromB);
  }

}
