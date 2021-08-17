package io.xj.lib.entity;

// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import io.xj.api.Library;
import io.xj.api.Program;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class EntityStoreImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private EntityStore subject;

  @Before
  public void setUp() {
    var injector = Guice.createInjector(new EntityModule());
    var entityFactory = injector.getInstance(EntityFactory.class);
    subject = injector.getInstance(EntityStore.class);

    // Some topology
    entityFactory.register("Program")
      .withAttribute("name")
      .belongsTo(Library.class)
      .createdBy(Program::new);

    // Instantiate the test subject and put the payload
    subject = injector.getInstance(EntityStore.class);
  }

  @Test
  public void put_get_Program() throws EntityStoreException {
    Program program = new Program()
      .id(UUID.randomUUID())
      .name("bingo");

    subject.put(program);
    Program result = subject.get(Program.class, program.getId()).orElseThrow();

    Assert.assertEquals(program.getId(), result.getId());
    Assert.assertEquals("bingo", result.getName());
  }

  @Test
  public void putAll_getAll() throws EntityStoreException {
    Library library = new Library()
      .id(UUID.randomUUID())
      .name("parent5");
    Program program2 = new Program()
      .id(UUID.randomUUID())
      .libraryId(library.getId())
      .name("Test2");
    Program program3 = new Program()
      .id(UUID.randomUUID())
      .libraryId(library.getId())
      .name("Test7");
    Program program2_program0 = new Program()
      .id(UUID.randomUUID())
      .libraryId(program2.getId())
      .name("Test2_A");
    Program program3_program0 = new Program()
      .id(UUID.randomUUID())
      .libraryId(program3.getId())
      .name("Test7_B");
    Program program3_program1 = new Program()
      .id(UUID.randomUUID())
      .libraryId(program3.getId())
      .name("Test7_C");
    assertEquals(5, subject.putAll(ImmutableList.of(program2, program3, program2_program0, program3_program0, program3_program1)).size());

    Collection<Program> result = subject.getAll(Program.class, Library.class, ImmutableList.of(library.getId()));
    assertEquals(2, result.size());
  }

}
