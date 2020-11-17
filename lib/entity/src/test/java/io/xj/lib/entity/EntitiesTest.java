// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.InstrumentMeme;
import io.xj.Library;
import io.xj.Program;
import io.xj.ProgramMeme;
import io.xj.ProgramSequenceBindingMeme;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 Tests for text utilities
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class EntitiesTest extends TestTemplate {

  @Test
  public void toResourceBelongsTo() {
    assertEquals("entity", Entities.toBelongsTo("Entity"));
    assertEquals("libraryProgram", Entities.toBelongsTo("LibraryProgram"));
    assertEquals("program", Entities.toBelongsTo("program"));
    assertEquals("program", Entities.toBelongsTo("programs"));
    assertEquals("program", Entities.toBelongsTo(createProgram("Ding")));
    assertEquals("program", Entities.toBelongsTo(Program.class));
    assertEquals("library", Entities.toBelongsTo("Library"));
  }

  @Test
  public void toResourceHasMany() {
    assertEquals("entities", Entities.toHasMany("Entity"));
    assertEquals("libraryPrograms", Entities.toHasMany("LibraryProgram"));
    assertEquals("programs", Entities.toHasMany("program"));
    assertEquals("programs", Entities.toHasMany(createProgram("Ding")));
    assertEquals("programs", Entities.toHasMany(Program.class));
    assertEquals("libraries", Entities.toHasMany("Library"));
  }

  @Test
  public void toResourceHasManyFromType() {
    assertEquals("entities", Entities.toHasManyFromType("entities"));
    assertEquals("libraryPrograms", Entities.toHasManyFromType("library-programs"));
    assertEquals("libraryPrograms", Entities.toHasManyFromType("library-program"));
    assertEquals("libraries", Entities.toHasManyFromType("library"));
    assertEquals("libraries", Entities.toHasManyFromType("Libraries"));
  }

  @Test
  public void toResourceType() {
    assertEquals("entities", Entities.toType("Entity"));
    assertEquals("library-programs", Entities.toType("LibraryProgram"));
    assertEquals("library-programs", Entities.toType("libraryProgram"));
    assertEquals("library-programs", Entities.toType("libraryPrograms"));
    assertEquals("programs", Entities.toType(createProgram("Ding")));
    assertEquals("programs", Entities.toType(Program.class));
    assertEquals("libraries", Entities.toType("Library"));
  }

  @Test
  public void toIdAttribute() {
    assertEquals("bilgeWaterId", Entities.toIdAttribute("BilgeWater"));
    assertEquals("programId", Entities.toIdAttribute(createProgram("Ding")));
    assertEquals("programId", Entities.toIdAttribute(Program.class));
  }

  @Test
  public void toAttributeName() {
    assertEquals("dancingAbility", Entities.toAttributeName("DancingAbility"));
  }

  @Rule
  public ExpectedException failure = ExpectedException.none();

  Program program;
  private EntityFactory entityFactory;

  @Before
  public void setUp() {
    Injector injector = Guice.createInjector(new EntityModule());
    entityFactory = injector.getInstance(EntityFactory.class);
    entityFactory.register(Program.class)
      .withAttribute("name");
    program = Program.newBuilder()
      .setId("879802e8-5856-4b1f-8c7f-09fd7f4bcde6")
      .setName("Marv")
      .build();
  }

  @Test
  public void set() throws EntityException {
    Program.Builder program5 = Program.newBuilder()
      .setId("879802e8-5856-4b1f-8c7f-09fd7f4bcde6")
      .setName("Marv");

    Entities.set(program5, "name", "Dave");

    Assert.assertEquals("Dave", program5.getName());
  }

  @Test
  public void set_nonexistentAttribute() throws EntityException {
    failure.expect(EntityException.class);
    failure.expectMessage("Program has no attribute 'turnip'");

    Entities.set(program, "turnip", 4.2);
  }

  @Test
  public void setAllAttributes() throws EntityException {
    entityFactory.setAllAttributes(program, createProgram("Marv"));

    Assert.assertEquals("Marv", program.getName());
  }

  @Test
  public void getResourceId() throws EntityException {
    assertEquals("879802e8-5856-4b1f-8c7f-09fd7f4bcde6", Entities.getId(program));
  }

  @Test
  public void getResourceId_nullIfEntityHasNoId() throws EntityException {
    assertNull(Entities.getId(new Object()));
  }

  @Test
  public void set_willFailIfSetterAcceptsNoParameters() throws EntityException {
    Program subject = Program.newBuilder().build();

    failure.expect(EntityException.class);
    failure.expectMessage("Program has no attribute 'willFailBecauseAcceptsNoParameters'");

    Entities.set(subject, "willFailBecauseAcceptsNoParameters", true);
  }

  @Test
  public void set_willFailIfSetterHasProtectedAccess() throws EntityException {
    Program subject = Program.newBuilder().build();

    failure.expect(EntityException.class);
    failure.expectMessage("Program has no attribute 'willFailBecauseNonexistent'");

    Entities.set(subject, "willFailBecauseNonexistent", "testing");
  }

  @Test
  public void testGetBelongsToId() throws EntityException {
    Program parent = createProgram("Parent");
    Program child = createProgram(parent.getId(), "Child");

    assertEquals(parent.getId(), Entities.getBelongsToId(child, "library").orElseThrow());
  }

  @Test
  public void isChild() {
    Library parent = Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();

    assertTrue(Entities.isChild(
      Program.newBuilder()
        .setLibraryId(parent.getId()),
      parent));
    assertFalse(Entities.isChild(Program.newBuilder()
      .setLibraryId(UUID.randomUUID().toString()), parent));
    assertFalse(Entities.isChild(Program.newBuilder(), parent));
  }


  @Test
  public void csvIdsOf() {
    assertEquals("4872f737-3526-4532-bb9f-358e3503db7e,333d6284-d7b9-4654-b79c-cafaf9330b6a",
      Entities.csvIdsOf(ImmutableList.of(
        Program.newBuilder().setId("4872f737-3526-4532-bb9f-358e3503db7e"),
        Program.newBuilder().setId("333d6284-d7b9-4654-b79c-cafaf9330b6a")
      )));
  }

  @Test
  public void idsOf() {
    assertEquals(ImmutableSet.of(
      "4872f737-3526-4532-bb9f-358e3503db7e",
      "333d6284-d7b9-4654-b79c-cafaf9330b6a"
      ),
      Entities.idsOf(ImmutableList.of(
        Program.newBuilder().setId("4872f737-3526-4532-bb9f-358e3503db7e"),
        Program.newBuilder().setId("333d6284-d7b9-4654-b79c-cafaf9330b6a")
      )));
  }

  @Test
  public void csvOf() {
    assertEquals("4872f737-3526-4532-bb9f-358e3503db7e,333d6284-d7b9-4654-b79c-cafaf9330b6a",
      Entities.csvOf(ImmutableList.of(
        "4872f737-3526-4532-bb9f-358e3503db7e",
        "333d6284-d7b9-4654-b79c-cafaf9330b6a"
      )));
  }

  @Test
  public void idsFromCSV() {
    assertEquals(
      ImmutableList.of(
        "4872f737-3526-4532-bb9f-358e3503db7e",
        "333d6284-d7b9-4654-b79c-cafaf9330b6a"
      ),
      Entities.idsFromCSV("4872f737-3526-4532-bb9f-358e3503db7e,333d6284-d7b9-4654-b79c-cafaf9330b6a"));
  }

  @Test
  public void isParent() {
    Library parent = Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();

    assertTrue(Entities.isParent(parent, Program.newBuilder().setLibraryId(parent.getId())));
    assertFalse(Entities.isParent(parent, Program.newBuilder().setLibraryId(UUID.randomUUID().toString())));
    assertFalse(Entities.isParent(parent, Program.newBuilder()));
  }

  @Test
  public void isSame() {
    Program x = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();

    assertTrue(Entities.isSame(x, Program.newBuilder().setId(x.getId()).build()));
    assertFalse(Entities.isSame(x, Program.newBuilder().build()));
  }

  @Test
  public void flatMapIds() {
    List<String> result =
      ImmutableList.of(
        Program.newBuilder().setId("4872f737-3526-4532-bb9f-358e3503db7e"),
        Program.newBuilder().setId("333d6284-d7b9-4654-b79c-cafaf9330b6a"),
        Program.newBuilder().setId("e23fb542-b0fc-4773-9848-772f64cbc5a4")
      ).stream()
        .flatMap(Entities::flatMapIds)
        .collect(Collectors.toList());

    assertEquals(
      ImmutableList.of(
        "4872f737-3526-4532-bb9f-358e3503db7e",
        "333d6284-d7b9-4654-b79c-cafaf9330b6a",
        "e23fb542-b0fc-4773-9848-772f64cbc5a4"
      ), result);
  }

  @Test
  public void namesOf() {
    Collection<String> result =
      Entities.namesOf(ImmutableList.of(
        ProgramMeme.newBuilder().setName("Apples").build(),
        ProgramSequenceBindingMeme.newBuilder().setName("Bananas").build(),
        InstrumentMeme.newBuilder().setName("Chips").build()
      ));

    assertEquals(
      ImmutableList.of(
        "Apples",
        "Bananas",
        "Chips"
      ), result);
  }

}
