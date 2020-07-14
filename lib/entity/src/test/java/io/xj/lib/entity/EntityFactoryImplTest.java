// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
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
    Injector injector = Guice.createInjector(new EntityModule());
    subject = injector.getInstance(EntityFactory.class);
  }

  @Test
  public void register_returnsSameSchema_forExistingType() {
    subject.register("MockEntity").createdBy(MockEntity::new).belongsTo("OtherThing");

    assertEquals(ImmutableSet.of("otherThing"), subject.register("mock-entities").getBelongsTo());
  }

  @Test
  public void register_returnsSameSchema_forExistingTypeClass() {
    subject.register(MockEntity.class).createdBy(MockEntity::new).belongsTo("OtherThing");

    assertEquals(ImmutableSet.of("otherThing"), subject.register(MockEntity.class).getBelongsTo());
  }

  @Test
  public void register_returnsSameSchema_forExisting_TypeThenClass() {
    subject.register("MockEntity").createdBy(MockEntity::new).belongsTo("OtherThing");

    assertEquals(ImmutableSet.of("otherThing"), subject.register(MockEntity.class).getBelongsTo());
  }

  @Test
  public void register_returnsSameSchema_forExisting_ClassThenType() {
    subject.register(MockEntity.class).createdBy(MockEntity::new).belongsTo("OtherThing");

    assertEquals(ImmutableSet.of("otherThing"), subject.register("MockEntity").getBelongsTo());
  }

  @Test
  public void register_basicTypeCreator() throws EntityException {
    subject.register("MockEntity").createdBy(MockEntity::new);

    assertEquals(MockEntity.class, subject.getInstance("mock-entity").getClass());
  }

  @Test
  public void register_withBelongsTo() throws EntityException {
    subject.register("MockEntity").belongsTo("FictionalEntity");

    assertEquals(ImmutableSet.of("fictionalEntity"), subject.getBelongsTo("mock-entities"));
  }

  @Test
  public void register_withAttributesAndBelongsTo() throws EntityException {
    subject.register("MockEntity");
    subject.register("FictionalEntity").withAttribute("name").belongsTo("mockEntity").createdBy(MockEntity::new);

    assertEquals(ImmutableSet.of("mockEntity"),
      subject.getBelongsTo("fictional-entity"));

    assertEquals(ImmutableSet.of("name"),
      subject.getAttributes("fictional-entity"));
  }

  @Test
  public void register_withAttributes() throws EntityException {
    subject.register("FictionalEntity").withAttribute("name").createdBy(MockEntity::new);

    assertEquals(ImmutableSet.of("name"), subject.getAttributes("fictional-entity"));
  }

  @Test
  public void getBelongsToType() throws EntityException {
    subject.register("OtherEntity");
    subject.register("FakeEntity").belongsTo("otherEntity").createdBy(MockEntity::new);

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
    subject.register("FalseEntity").withAttribute("yarn").createdBy(MockEntity::new);

    assertEquals(ImmutableSet.of("yarn"), subject.getAttributes("false-entity"));
  }

  @Test
  public void serialize() throws EntityException {
    subject.register("MockEntity").createdBy(MockEntity::new).withAttribute("name");
    MockEntity entity = subject.getInstance(MockEntity.class).setId(UUID.fromString("63c30864-db29-4138-a867-32afa01da884")).setName("Jams");

    String result = subject.serialize(entity);

    assertEquals("{\"id\":\"63c30864-db29-4138-a867-32afa01da884\",\"createdAt\":null,\"updatedAt\":null,\"name\":\"Jams\",\"mockEntityId\":null,\"parentId\":null}", result);
  }

  @Test
  public void deserialize() throws EntityException {
    subject.register("MockEntity").createdBy(MockEntity::new).withAttribute("name");

    MockEntity result = subject.deserialize(MockEntity.class, "{\"id\":\"63c30864-db29-4138-a867-32afa01da884\",\"createdAt\":null,\"updatedAt\":null,\"name\":\"Jams\",\"mockEntityId\":null,\"parentId\":null}");

    assertEquals("Jams", result.getName());
  }

  @Test
  public void deserialize_failsWithBadJson() throws EntityException {
    subject.register("MockEntity").createdBy(MockEntity::new).withAttribute("name");

    failure.expect(EntityException.class);
    failure.expectMessage("Failed to deserialize JSON");

    subject.deserialize("this is absolutely not json");
  }

  @Test
  public void testClone() throws EntityException {
    subject.register("MockEntity")
      .withAttributes("name")
      .belongsTo("mockEntity")
      .createdBy(MockEntity::new);
    MockEntity from = MockEntity.create().setName("Flight").setMockEntityId(UUID.randomUUID());

    MockEntity result = subject.clone(from);

    assertEquals("Flight", result.getName());
    assertEquals(from.getMockEntityId(), result.getMockEntityId());
    assertEquals(from.getId(), result.getId());
    assertNotSame(result, from);
  }

  @Test
  public void testClone_withEnumValue() throws EntityException {
    subject.register("MockSuperEntity")
      .withAttributes("stringValue", "enumValue")
      .belongsTo("mockEntity")
      .createdBy(MockSuperEntity::new);
    MockSuperEntity from = MockSuperEntity.create()
      .setStringValue("Flight")
      .setEnumValue(MockEnumValue.Apples);

    MockSuperEntity result = subject.clone(from);

    assertEquals("Flight", result.getStringValue());
    assertEquals(from.getEnumValue(), result.getEnumValue());
    assertEquals(from.getId(), result.getId());
    assertNotSame(result, from);
  }


  @Test
  public void testClone_withNullBelongsToId() throws EntityException {
    subject.register("MockEntity").withAttribute("name").belongsTo("mockEntity").createdBy(MockEntity::new);
    MockEntity from = MockEntity.create().setName("Flight");

    MockEntity result = subject.clone(from);

    assertEquals("Flight", result.getName());
    assertEquals(from.getId(), result.getId());
    assertNotSame(result, from);
  }

  @Test
  public void testCloneAll() throws EntityException {
    subject.register("MockEntity").withAttribute("name").belongsTo("mockEntity").createdBy(MockEntity::new);
    MockEntity fromA = MockEntity.create().setName("Air").setMockEntityId(UUID.randomUUID());
    MockEntity fromB = MockEntity.create().setName("Ground").setMockEntityId(UUID.randomUUID());

    Collection<MockEntity> result = subject.cloneAll(ImmutableList.of(fromA, fromB));

    assertEquals(2, result.size());
    Iterator<MockEntity> resultIt = result.iterator();
    //
    MockEntity resultA = resultIt.next();
    assertEquals("Air", resultA.getName());
    assertEquals(fromA.getMockEntityId(), resultA.getMockEntityId());
    assertEquals(fromA.getId(), resultA.getId());
    assertNotSame(resultA, fromA);
    //
    MockEntity resultB = resultIt.next();
    assertEquals("Ground", resultB.getName());
    assertEquals(fromB.getMockEntityId(), resultB.getMockEntityId());
    assertEquals(fromB.getId(), resultB.getId());
    assertNotSame(resultB, fromB);
  }

}
