// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
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
    assertEquals("mockEntity", Entities.toBelongsTo("mockEntity"));
    assertEquals("mockEntity", Entities.toBelongsTo("mockEntities"));
    assertEquals("mockEntity", Entities.toBelongsTo(createMockEntity("Ding")));
    assertEquals("mockEntity", Entities.toBelongsTo(MockEntity.class));
    assertEquals("library", Entities.toBelongsTo("Library"));
  }

  @Test
  public void toResourceHasMany() {
    assertEquals("entities", Entities.toHasMany("Entity"));
    assertEquals("libraryPrograms", Entities.toHasMany("LibraryProgram"));
    assertEquals("mockEntities", Entities.toHasMany("mockEntity"));
    assertEquals("mockEntities", Entities.toHasMany(createMockEntity("Ding")));
    assertEquals("mockEntities", Entities.toHasMany(MockEntity.class));
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
    assertEquals("mock-entities", Entities.toType(createMockEntity("Ding")));
    assertEquals("mock-entities", Entities.toType(MockEntity.class));
    assertEquals("libraries", Entities.toType("Library"));
  }

  @Test
  public void toIdAttribute() {
    assertEquals("bilgeWaterId", Entities.toIdAttribute("BilgeWater"));
    assertEquals("mockEntityId", Entities.toIdAttribute(createMockEntity("Ding")));
    assertEquals("mockEntityId", Entities.toIdAttribute(MockEntity.class));
  }

  @Test
  public void toAttributeName() {
    assertEquals("dancingAbility", Entities.toAttributeName("DancingAbility"));
  }

  @Rule
  public ExpectedException failure = ExpectedException.none();

  MockEntity mockEntity;
  private EntityFactory entityFactory;

  @Before
  public void setUp() {
    Injector injector = Guice.createInjector(new EntityModule());
    entityFactory = injector.getInstance(EntityFactory.class);
    entityFactory.register(MockEntity.class);
    mockEntity = createMockEntity(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"), "Marv");
  }

  @Test
  public void set() throws EntityException {
    Entities.set(mockEntity, "name", "Dave");

    Assert.assertEquals("Dave", mockEntity.getName());
  }

  @Test
  public void set_nonexistentAttribute() throws EntityException {
    failure.expect(EntityException.class);
    failure.expectMessage("MockEntity has no attribute 'turnip'");

    Entities.set(mockEntity, "turnip", 4.2);
  }

  @Test
  public void setAllAttributes() throws EntityException {
    entityFactory.setAllAttributes(mockEntity, createMockEntity("Marv"));

    Assert.assertEquals("Marv", mockEntity.getName());
  }

  @Test
  public void getResourceId() throws EntityException {
    assertEquals("879802e8-5856-4b1f-8c7f-09fd7f4bcde6", Entities.getId(mockEntity));
  }

  @Test
  public void getResourceId_exceptionIfEntityHasNoId() throws EntityException {
    failure.expect(EntityException.class);
    failure.expectMessage("Has no id");

    assertEquals("879802e8-5856-4b1f-8c7f-09fd7f4bcde6", Entities.getId(new Object()));
  }

  @Test
  public void get_variousTypes() throws EntityException {
    MockSuperEntity subject = new MockSuperEntity();

    subject.setPrimitiveBooleanValue(true);
    assertTrue(Entities.get(subject, "primitiveBooleanValue").isPresent());
    assertEquals(true, Entities.get(subject, "primitiveBooleanValue").get());

    subject.setPrimitiveIntValue(8907);
    assertTrue(Entities.get(subject, "primitiveIntValue").isPresent());
    assertEquals(8907, Entities.get(subject, "primitiveIntValue").get());

    subject.setPrimitiveShortValue((short) 45);
    assertTrue(Entities.get(subject, "primitiveShortValue").isPresent());
    assertEquals((short) 45, Entities.get(subject, "primitiveShortValue").get());

    subject.setPrimitiveLongValue(237L);
    assertTrue(Entities.get(subject, "primitiveLongValue").isPresent());
    assertEquals(237L, Entities.get(subject, "primitiveLongValue").get());

    subject.setPrimitiveDoubleValue(932.1876D);
    assertTrue(Entities.get(subject, "primitiveDoubleValue").isPresent());
    assertEquals(932.1876D, Entities.get(subject, "primitiveDoubleValue").get());

    subject.setPrimitiveFloatValue(12787.847F);
    assertTrue(Entities.get(subject, "primitiveFloatValue").isPresent());
    assertEquals(12787.847F, Entities.get(subject, "primitiveFloatValue").get());

    subject.setBooleanValue(true);
    assertTrue(Entities.get(subject, "booleanValue").isPresent());
    assertEquals(true, Entities.get(subject, "booleanValue").get());

    subject.setIntegerValue(8907);
    assertTrue(Entities.get(subject, "integerValue").isPresent());
    assertEquals(8907, Entities.get(subject, "integerValue").get());

    subject.setShortValue((short) 45);
    assertTrue(Entities.get(subject, "shortValue").isPresent());
    assertEquals((short) 45, Entities.get(subject, "shortValue").get());

    subject.setLongValue(237L);
    assertTrue(Entities.get(subject, "longValue").isPresent());
    assertEquals(237L, Entities.get(subject, "longValue").get());

    subject.setDoubleValue(932.1876D);
    assertTrue(Entities.get(subject, "doubleValue").isPresent());
    assertEquals(932.1876D, Entities.get(subject, "doubleValue").get());

    subject.setFloatValue(12787.847F);
    assertTrue(Entities.get(subject, "floatValue").isPresent());
    assertEquals(12787.847F, Entities.get(subject, "floatValue").get());

    subject.setInstantValue(Instant.ofEpochMilli(1583896837907L));
    assertTrue(Entities.get(subject, "instantValue").isPresent());
    assertEquals(Instant.ofEpochMilli(1583896837907L), Entities.get(subject, "instantValue").get());

    subject.setTimestampValue(Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")));
    assertTrue(Entities.get(subject, "timestampValue").isPresent());
    assertEquals(Instant.parse("2020-03-07T14:17:02Z"), Entities.get(subject, "timestampValue").get());

    subject.setBigIntegerValue(BigInteger.valueOf(309879125L));
    assertTrue(Entities.get(subject, "bigIntegerValue").isPresent());
    assertEquals(BigInteger.valueOf(309879125L), Entities.get(subject, "bigIntegerValue").get());

    Entities.set(subject, "stringValue", Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")));
    assertEquals(Instant.parse("2020-03-07T14:17:02Z"), Timestamp.valueOf(subject.getStringValue()).toInstant());

    Entities.set(subject, "uuidValue", UUID.fromString("f208b87d-12b3-4dc3-ae30-6d757e0db6a2"));
    Assert.assertEquals(UUID.fromString("f208b87d-12b3-4dc3-ae30-6d757e0db6a2"), subject.getUuidValue());

    Entities.set(subject, "proprietaryValue", new MockEntity().setName("Inner Piece"));
    Assert.assertEquals("Inner Piece", subject.getProprietaryValue().getName());

    Entities.set(subject, "stringValue", MockEnumValue.Apples);
    Assert.assertEquals("Apples", subject.getStringValue());

    Entities.set(subject, "enumValue", MockEnumValue.Apples);
    Assert.assertEquals(MockEnumValue.Apples, subject.getEnumValue());

    Entities.set(subject, "enumValue", "Apples");
    Assert.assertEquals(MockEnumValue.Apples, subject.getEnumValue());

    subject.setProprietaryValue(new MockEntity().setName("Inner Piece"));
    assertTrue(Entities.get(subject, "proprietaryValue").isPresent());
    Assert.assertEquals("Inner Piece", ((MockEntity) Entities.get(subject, "proprietaryValue").get()).getName());

    subject.setEnumValue(MockEnumValue.Apples);
    assertTrue(Entities.get(subject, "enumValue").isPresent());
    Assert.assertEquals(MockEnumValue.Apples, ((MockEnumValue) Entities.get(subject, "enumValue").get()));
  }

  @Test
  public void set_variousTypes() throws EntityException {
    MockSuperEntity subject = new MockSuperEntity();

    Entities.set(subject, "primitiveBooleanValue", true);
    Assert.assertEquals(Boolean.TRUE, subject.getPrimitiveBooleanValue());

    Entities.set(subject, "primitiveIntValue", 8907);
    Assert.assertEquals(8907, subject.getPrimitiveIntValue());

    Entities.set(subject, "primitiveShortValue", (short) 45);
    Assert.assertEquals((short) 45, subject.getPrimitiveShortValue());

    Entities.set(subject, "primitiveLongValue", 237L);
    Assert.assertEquals(237L, subject.getPrimitiveLongValue());

    Entities.set(subject, "primitiveDoubleValue", 932.1876D);
    Assert.assertEquals(932.1876D, subject.getPrimitiveDoubleValue(), 0.01);

    Entities.set(subject, "primitiveFloatValue", 12787.847F);
    Assert.assertEquals(12787.847F, subject.getPrimitiveFloatValue(), 0.01);

    Entities.set(subject, "booleanValue", true);
    Assert.assertEquals(Boolean.TRUE, subject.getBooleanValue());

    Entities.set(subject, "integerValue", 8907);
    Assert.assertEquals(Integer.valueOf(8907), subject.getIntegerValue());

    Entities.set(subject, "shortValue", (short) 45);
    Assert.assertEquals(Short.valueOf((short) 45), subject.getShortValue());

    Entities.set(subject, "longValue", 237L);
    Assert.assertEquals(Long.valueOf(237L), subject.getLongValue());

    Entities.set(subject, "doubleValue", 932.1876D);
    Assert.assertEquals(Double.valueOf(932.1876D), subject.getDoubleValue());

    Entities.set(subject, "floatValue", 12787.847F);
    Assert.assertEquals(Float.valueOf(12787.847F), subject.getFloatValue());

    Entities.set(subject, "instantValue", Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")));
    Assert.assertEquals(Instant.parse("2020-03-07T14:17:02Z"), subject.getInstantValue());

    Entities.set(subject, "instantValue", Instant.ofEpochMilli(1583896837907L));
    Assert.assertEquals(Instant.ofEpochMilli(1583896837907L), subject.getInstantValue());

    Entities.set(subject, "instantValue", "2020-03-07T14:17:02Z");
    Assert.assertEquals(Instant.parse("2020-03-07T14:17:02Z"), subject.getInstantValue());

    Entities.set(subject, "timestampValue", Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")));
    Assert.assertEquals(Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")), subject.getTimestampValue());

    Entities.set(subject, "timestampValue", Instant.parse("2020-03-07T14:17:02Z"));
    Assert.assertEquals(Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")), subject.getTimestampValue());

    Entities.set(subject, "timestampValue", "2020-03-07T14:17:02Z");
    Assert.assertEquals(Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")), subject.getTimestampValue());

    Entities.set(subject, "bigIntegerValue", BigInteger.valueOf(309879125L));
    Assert.assertEquals(BigInteger.valueOf(309879125L), subject.getBigIntegerValue());

    Entities.set(subject, "stringValue", String.valueOf(4775));
    Assert.assertEquals(String.valueOf(4775), subject.getStringValue());

    Entities.set(subject, "stringValue", Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")));
    assertEquals(Instant.parse("2020-03-07T14:17:02Z"), Timestamp.valueOf(subject.getStringValue()).toInstant());

    Entities.set(subject, "uuidValue", UUID.fromString("f208b87d-12b3-4dc3-ae30-6d757e0db6a2"));
    Assert.assertEquals(UUID.fromString("f208b87d-12b3-4dc3-ae30-6d757e0db6a2"), subject.getUuidValue());

    Entities.set(subject, "proprietaryValue", new MockEntity().setName("Inner Piece"));
    Assert.assertEquals("Inner Piece", subject.getProprietaryValue().getName());

    Entities.set(subject, "stringValue", MockEnum.ValueA);
    Assert.assertEquals("ValueA", subject.getStringValue());
  }

  @Test
  public void set_willFailIfSetterAcceptsNoParameters() throws EntityException {
    MockSuperEntity subject = new MockSuperEntity();

    failure.expect(EntityException.class);
    failure.expectMessage("Setter accepts no parameters");

    Entities.set(subject, "willFailBecauseAcceptsNoParameters", true);
  }

  @Test
  public void set_willFailIfSetterHasProtectedAccess() throws EntityException {
    MockSuperEntity subject = new MockSuperEntity();

    failure.expect(EntityException.class);
    failure.expectMessage("MockSuperEntity has no attribute 'willFailBecauseNonexistent'");

    Entities.set(subject, "willFailBecauseNonexistent", "testing");
  }

  @Test
  public void testGetBelongsToId() throws EntityException {
    MockEntity parent = createMockEntity("Parent");
    MockEntity child = createMockEntity("Child", parent);

    assertEquals(parent.getId(), Entities.getBelongsToId(child, "mockEntity").orElseThrow());
  }

  /**
   Create a new MockEntity with the given name

   @param name of mock entity
   @return new mock entity
   */
  protected MockEntity createMockEntity(String name) {
    MockEntity e = createMockEntity(UUID.randomUUID());
    e.setName(name);
    return e;
  }

  /**
   Create a new MockEntity with the given id and name

   @param id   of mock entity
   @param name of mock entity
   @return new mock entity
   */
  protected MockEntity createMockEntity(UUID id, String name) {
    MockEntity e = createMockEntity(id);
    e.setName(name);
    return e;
  }

  /**
   Create a new MockEntity with the given name

   @param id of mock entity
   @return new mock entity
   */
  protected MockEntity createMockEntity(UUID id) {
    MockEntity e = new MockEntity();
    e.setId(id);
    return e;
  }

  /**
   Create a new MockEntity with the given name and belongs-to relationship

   @param name      of mock entity
   @param belongsTo mockEntity
   @return new mock entity
   */
  protected MockEntity createMockEntity(String name, MockEntity belongsTo) {
    MockEntity e = createMockEntity(name);
    e.setMockEntityId(belongsTo.getId());
    return e;
  }

  /**
   Mock enum value
   */
  private enum MockEnum {
    ValueA;
  }

}
