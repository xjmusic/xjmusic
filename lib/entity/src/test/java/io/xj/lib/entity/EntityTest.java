// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.*;

public class EntityTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Before
  public void setUp() {
  }

  /**
   [#175602029] EventEntity Position persists exact floating point
   <p>
   DEPRECATED: [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.
   */
  @Test
  public void exactPosition() {
    assertEquals(1.25179957, new MockChordEntity().setPosition(1.25179957).getPosition(), 0.0000001);
  }

  @Test
  public void csvIdsOf() {
    assertEquals("4872f737-3526-4532-bb9f-358e3503db7e,333d6284-d7b9-4654-b79c-cafaf9330b6a",
      Entity.csvIdsOf(ImmutableList.of(
        MockEntity.create().setId(UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e")),
        MockEntity.create().setId(UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a"))
      )));
  }

  @Test
  public void idsOf() {
    assertEquals(ImmutableSet.of(
      UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e"),
      UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a")
      ),
      Entity.idsOf(ImmutableList.of(
        MockEntity.create().setId(UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e")),
        MockEntity.create().setId(UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a"))
      )));
  }

  @Test
  public void csvOf() {
    assertEquals("4872f737-3526-4532-bb9f-358e3503db7e,333d6284-d7b9-4654-b79c-cafaf9330b6a",
      Entity.csvOf(ImmutableList.of(
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
      Entity.idsFromCSV("4872f737-3526-4532-bb9f-358e3503db7e,333d6284-d7b9-4654-b79c-cafaf9330b6a"));
  }

  @Test
  public void isChild() {
    MockEntity parent = MockEntity.create();

    assertTrue(MockEntity.create().setMockEntityId(parent.getId()).isChild(parent));
    assertFalse(MockEntity.create().setMockEntityId(UUID.randomUUID()).isChild(parent));
    assertFalse(MockEntity.create().isChild(parent));
  }

  @Test
  public void isParent() {
    MockEntity parent = MockEntity.create();

    assertTrue(parent.isParent(MockEntity.create().setMockEntityId(parent.getId())));
    assertFalse(parent.isParent(MockEntity.create().setMockEntityId(UUID.randomUUID())));
    assertFalse(parent.isParent(MockEntity.create()));
  }

  @Test
  public void isSame() {
    MockEntity x = MockEntity.create();

    assertTrue(x.isSame(new MockEntity().setId(x.getId())));
    assertFalse(x.isSame(MockEntity.create()));
  }
}
