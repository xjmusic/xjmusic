// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import io.xj.api.InstrumentMeme;
import io.xj.api.Library;
import io.xj.api.Program;
import io.xj.api.ProgramMeme;
import io.xj.api.ProgramSequenceBindingMeme;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 Tests for text utilities
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class EntitiesTest extends TestTemplate {
  Program program;
  private EntityFactory entityFactory;

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

  @Before
  public void setUp() {
    var injector = Guice.createInjector(new EntityModule());
    entityFactory = injector.getInstance(EntityFactory.class);
    entityFactory.register(Program.class)
      .withAttribute("name");
    program = new Program()
      .id(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"))
      .name("Marv");
  }

  @Test
  public void set() throws EntityException {
    Program program5 = new Program()
      .id(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"))
      .name("Marv");

    Entities.set(program5, "name", "Dave");

    Assert.assertEquals("Dave", program5.getName());
  }

  @Test
  public void set_nonexistentAttribute() {
    var e = assertThrows(EntityException.class, () -> Entities.set(program, "turnip", 4.2));
    assertEquals("Program has no attribute 'turnip'", e.getMessage());
  }

  @Test
  public void setAllAttributes() throws EntityException {
    entityFactory.setAllAttributes(program, createProgram("Marv"));

    Assert.assertEquals("Marv", program.getName());
  }

  @Test
  public void getResourceId() throws EntityException {
    assertEquals(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"), Entities.getId(program));
  }

  @Test
  public void getResourceId_nullIfEntityHasNoId() throws EntityException {
    assertNull(Entities.getId(new Object()));
  }

  @Test
  public void set_willFailIfSetterAcceptsNoParameters() {
    Program subject = new Program();

    var e = assertThrows(EntityException.class, () -> Entities.set(subject, "willFailBecauseAcceptsNoParameters", true));
    assertEquals("Program has no attribute 'willFailBecauseAcceptsNoParameters'", e.getMessage());
  }

  @Test
  public void set_willFailIfSetterHasProtectedAccess() {
    Program subject = new Program();

    var e = assertThrows(EntityException.class, () -> Entities.set(subject, "willFailBecauseNonexistent", "testing"));
    assertEquals("Program has no attribute 'willFailBecauseNonexistent'", e.getMessage());
  }

  @Test
  public void testGetBelongsToId() throws EntityException {
    Program parent = createProgram("Parent");
    Program child = createProgram(parent.getId(), "Child");

    assertEquals(parent.getId(), Entities.getBelongsToId(child, "library").orElseThrow());
  }

  @Test
  public void isChild() {
    Library parent = new Library()
      .id(UUID.randomUUID());

    assertTrue(Entities.isChild(
      new Program()
        .libraryId(parent.getId()),
      parent));
    assertFalse(Entities.isChild(new Program()
      .libraryId(UUID.randomUUID()), parent));
    assertFalse(Entities.isChild(new Program(), parent));
  }


  @Test
  public void csvIdsOf() {
    assertEquals("4872f737-3526-4532-bb9f-358e3503db7e,333d6284-d7b9-4654-b79c-cafaf9330b6a",
      Entities.csvIdsOf(ImmutableList.of(
        new Program().id(UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e")),
        new Program().id(UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a"))
      )));
  }

  @Test
  public void idsOf() {
    assertEquals(ImmutableSet.of(
        UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e"),
        UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a")
      ),
      Entities.idsOf(ImmutableList.of(
        new Program().id(UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e")),
        new Program().id(UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a"))
      )));
  }

  @Test
  public void csvOf() {
    assertEquals("4872f737-3526-4532-bb9f-358e3503db7e, 333d6284-d7b9-4654-b79c-cafaf9330b6a",
      Entities.csvOf(ImmutableList.of(
        UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e"),
        UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a")
      )));
  }

  @Test
  public void idsFromCSV() {
    assertEquals(
      ImmutableList.of(
        UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e"),
        UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a")
      ),
      Entities.idsFromCSV("4872f737-3526-4532-bb9f-358e3503db7e,333d6284-d7b9-4654-b79c-cafaf9330b6a"));
  }

  @Test
  public void isParent() {
    Library parent = new Library()
      .id(UUID.randomUUID());

    assertTrue(Entities.isParent(parent, new Program().libraryId(parent.getId())));
    assertFalse(Entities.isParent(parent, new Program().libraryId(UUID.randomUUID())));
    assertFalse(Entities.isParent(parent, new Program()));
  }

  @Test
  public void isSame() {
    Program x = new Program()
      .id(UUID.randomUUID());

    assertTrue(Entities.isSame(x, new Program().id(x.getId())));
    assertFalse(Entities.isSame(x, new Program()));
  }

  @Test
  public void flatMapIds() {
    List<UUID> result =
      ImmutableList.of(
          new Program().id(UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e")),
          new Program().id(UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a")),
          new Program().id(UUID.fromString("e23fb542-b0fc-4773-9848-772f64cbc5a4"))
        ).stream()
        .flatMap(Entities::flatMapIds)
        .collect(Collectors.toList());

    assertEquals(
      ImmutableList.of(
        UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e"),
        UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a"),
        UUID.fromString("e23fb542-b0fc-4773-9848-772f64cbc5a4")
      ), result);
  }

  @Test
  public void namesOf() {
    Collection<String> result =
      Entities.namesOf(ImmutableList.of(
        new ProgramMeme().name("Apples"),
        new ProgramSequenceBindingMeme().name("Bananas"),
        new InstrumentMeme().name("Chips")
      ));

    assertEquals(
      ImmutableList.of(
        "Apples",
        "Bananas",
        "Chips"
      ), result);
  }

}
