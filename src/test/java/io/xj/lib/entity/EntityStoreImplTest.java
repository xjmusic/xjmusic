package io.xj.lib.entity;

// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import io.xj.Library;
import io.xj.Program;
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
      .createdBy(Program::getDefaultInstance);

    // Instantiate the test subject and put the payload
    subject = injector.getInstance(EntityStore.class);
  }

  @Test
  public void put_get_Program() throws EntityStoreException {
    Program program = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("bingo")
      .build();

    subject.put(program);
    Program result = subject.get(Program.class, program.getId()).orElseThrow();

    Assert.assertEquals(program.getId(), result.getId());
    Assert.assertEquals("bingo", result.getName());
  }

  @Test
  public void putAll_getAll() throws EntityStoreException {
    Library library = Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("parent5")
      .build();
    Program program2 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(library.getId())
      .setName("Test2")
      .build();
    Program program3 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(library.getId())
      .setName("Test7")
      .build();
    Program program2_program0 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(program2.getId())
      .setName("Test2_A")
      .build();
    Program program3_program0 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(program3.getId())
      .setName("Test7_B")
      .build();
    Program program3_program1 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(program3.getId())
      .setName("Test7_C")
      .build();
    assertEquals(5, subject.putAll(ImmutableList.of(program2, program3, program2_program0, program3_program0, program3_program1)).size());

    Collection<Program> result = subject.getAll(Program.class, Library.class, ImmutableList.of(library.getId()));
    assertEquals(2, result.size());
  }

}
