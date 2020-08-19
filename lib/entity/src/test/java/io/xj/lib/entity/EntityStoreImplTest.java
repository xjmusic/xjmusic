package io.xj.lib.entity;

// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
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
  private EntityFactory entityFactory;

  @Before
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new EntityModule());
    entityFactory = injector.getInstance(EntityFactory.class);
    subject = injector.getInstance(EntityStore.class);

    // Some topology
    entityFactory.register("MockEntity")
      .withAttribute("name")
      .belongsTo(MockEntity.class)
      .createdBy(MockEntity::new);

    // Instantiate the test subject and put the payload
    subject = injector.getInstance(EntityStore.class);
  }

  @Test
  public void put_get_MockEntity() throws EntityStoreException {
    UUID mockEntityId = UUID.randomUUID();
    MockEntity mockEntity = MockEntity.create()
      .setName("bingo");

    subject.put(mockEntity);
    MockEntity result = subject.get(MockEntity.class, mockEntity.getId()).orElseThrow();

    assertEquals(mockEntity.getId(), result.getId());
    assertEquals("bingo", result.getName());
  }

  @Test
  public void put_cantBeMutated() throws EntityStoreException {
    MockEntity mockEntity1 = MockEntity.create()
      .setName("fish");
    MockEntity mockEntity3 = MockEntity.create()
      .setMockEntityId(mockEntity1.getId())
      .setName("Test57");
    subject.put(mockEntity3);
    // and then after putting the entity, change the object that was sent-- this should NOT mutate the store
    mockEntity3.setName("FunkyTown");

    MockEntity result = subject.get(MockEntity.class, mockEntity3.getId()).orElseThrow();
    assertEquals("Test57", result.getName());
  }

  @Test
  public void get_cantBeMutated() throws EntityStoreException {
    MockEntity mockEntity1 = MockEntity.create()
      .setName("fish");
    MockEntity mockEntity3 = subject.put(MockEntity.create()
      .setMockEntityId(mockEntity1.getId())
      .setName("Test25"));
    MockEntity got = subject.get(MockEntity.class, mockEntity3.getId()).orElseThrow();
    // and then after getting an entity, change the object we got-- this should NOT mutate the store
    got.setName("FunkyTown");

    MockEntity result = subject.get(MockEntity.class, mockEntity3.getId()).orElseThrow();
    assertEquals("Test25", result.getName());
  }

  @Test
  public void getAll_cantBeMutated() throws EntityStoreException {
    MockEntity mockEntity1 = MockEntity.create()
      .setName("fish");
    MockEntity mockEntity3 = subject.put(MockEntity.create()
      .setMockEntityId(mockEntity1.getId())
      .setName("TestXP"));
    Collection<MockEntity> got = subject.getAll(MockEntity.class);
    // and then after getting the entities, change one of the objects-- this should NOT mutate the store
    got.iterator().next().setName("FunkyTown");

    MockEntity result = subject.get(MockEntity.class, mockEntity3.getId()).orElseThrow();
    assertEquals("TestXP", result.getName());
  }

  @Test
  public void getAllBelongingTo_cantBeMutated() throws EntityStoreException {
    MockEntity mockEntity1 = MockEntity.create()
      .setName("fish");
    MockEntity mockEntity3 = subject.put(MockEntity.create()
      .setMockEntityId(mockEntity1.getId())
      .setName("Test98"));
    Collection<MockEntity> got = subject.getAll(MockEntity.class, MockEntity.class, ImmutableList.of(mockEntity1.getId()));
    // and then after getting the entities, change one of the objects-- this should NOT mutate the store
    got.iterator().next().setName("FunkyTown");

    MockEntity result = subject.get(MockEntity.class, mockEntity3.getId()).orElseThrow();
    assertEquals("Test98", result.getName());
  }

  @Test
  public void putWithoutId_getHasNewId() throws EntityStoreException {
    MockEntity mockEntity1 = MockEntity.create().setName("fish");
    MockEntity mockEntity3 = MockEntity.create()
      .setMockEntityId(mockEntity1.getId())
      .setName("Test1");
    mockEntity3.setId(null);
    MockEntity mockEntity3_mockEntity0 = MockEntity.create()
      .setMockEntityId(mockEntity3.getId())
      .setName("Test567");
    subject.put(mockEntity3_mockEntity0);

    MockEntity result = subject.get(MockEntity.class, mockEntity3_mockEntity0.getId()).orElseThrow();
    assertEquals(mockEntity3_mockEntity0.getId(), result.getId());
    assertEquals(mockEntity3.getId(), result.getMockEntityId());
    assertEquals("Test567", result.getName());
  }

  @Test
  public void putAll_getAll() throws EntityStoreException {
    MockEntity parentEntity = MockEntity.create()
      .setName("parent5");
    MockEntity mockEntity2 = MockEntity.create()
      .setName("Test2")
      .setMockEntityId(parentEntity.getId());
    MockEntity mockEntity3 = MockEntity.create()
      .setName("Test7")
      .setMockEntityId(parentEntity.getId());
    MockEntity mockEntity2_mockEntity0 = MockEntity.create()
      .setMockEntityId(mockEntity2.getId())
      .setName("Test2_A");
    MockEntity mockEntity3_mockEntity0 = MockEntity.create()
      .setMockEntityId(mockEntity3.getId())
      .setName("Test7_B");
    MockEntity mockEntity3_mockEntity1 = MockEntity.create()
      .setMockEntityId(mockEntity3.getId())
      .setName("Test7_C");
    assertEquals(5, subject.putAll(ImmutableList.of(mockEntity2, mockEntity3, mockEntity2_mockEntity0, mockEntity3_mockEntity0, mockEntity3_mockEntity1)).size());

    Collection<MockEntity> result = subject.getAll(MockEntity.class, MockEntity.class, ImmutableList.of(mockEntity3.getId()));
    assertEquals(2, result.size());
  }

}
