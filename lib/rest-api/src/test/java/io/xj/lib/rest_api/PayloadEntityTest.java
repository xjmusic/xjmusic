// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import com.google.inject.Guice;
import com.google.inject.Injector;
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
 Entity test
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class PayloadEntityTest extends TestTemplate {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  MockEntity mockEntity;
  private PayloadFactory payloadFactory;

  @Before
  public void setUp() {
    Injector injector = Guice.createInjector(new RestApiModule());
    payloadFactory = injector.getInstance(PayloadFactory.class);
    payloadFactory.register(MockEntity.class);
    mockEntity = createMockEntity(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"), "Marv");
  }

  @Test
  public void set() throws RestApiException {
    PayloadEntity.set(mockEntity, "name", "Dave");

    assertEquals("Dave", mockEntity.getName());
  }

  @Test
  public void set_nonexistentAttribute() throws RestApiException {
    failure.expect(RestApiException.class);
    failure.expectMessage("MockEntity has no attribute 'turnip'");

    PayloadEntity.set(mockEntity, "turnip", 4.2);
  }

  @Test
  public void setAllAttributes() throws RestApiException {
    payloadFactory.setAllAttributes(mockEntity, createMockEntity("Marv"));

    assertEquals("Marv", mockEntity.getName());
  }

  @Test
  public void getResourceId() throws RestApiException {
    assertEquals("879802e8-5856-4b1f-8c7f-09fd7f4bcde6", PayloadEntity.getResourceId(mockEntity));
  }

  @Test
  public void getResourceId_exceptionIfEntityHasNoId() throws RestApiException {
    failure.expect(RestApiException.class);
    failure.expectMessage("Has no id");

    assertEquals("879802e8-5856-4b1f-8c7f-09fd7f4bcde6", PayloadEntity.getResourceId(new Object()));
  }

  @Test
  public void toPayloadReferenceObject() throws RestApiException {
    PayloadObject result = payloadFactory.toPayloadReferenceObject(mockEntity);

    assertEquals("mock-entities", result.getType());
    assertEquals(mockEntity.getId(), result.getId());
  }

  @Test
  public void toPayloadObject() throws RestApiException {
    PayloadObject result = payloadFactory.toPayloadObject(mockEntity);

    assertEquals("mock-entities", result.getType());
    assertEquals(mockEntity.getId(), result.getId());
  }

  @Test
  public void get_variousTypes() throws RestApiException {
    MockSuperEntity subject = new MockSuperEntity();

    subject.setPrimitiveBooleanValue(true);
    assertTrue(PayloadEntity.get(subject, "primitiveBooleanValue").isPresent());
    assertEquals(true, PayloadEntity.get(subject, "primitiveBooleanValue").get());

    subject.setPrimitiveIntValue(8907);
    assertTrue(PayloadEntity.get(subject, "primitiveIntValue").isPresent());
    assertEquals(8907, PayloadEntity.get(subject, "primitiveIntValue").get());

    subject.setPrimitiveShortValue((short) 45);
    assertTrue(PayloadEntity.get(subject, "primitiveShortValue").isPresent());
    assertEquals((short) 45, PayloadEntity.get(subject, "primitiveShortValue").get());

    subject.setPrimitiveLongValue(237L);
    assertTrue(PayloadEntity.get(subject, "primitiveLongValue").isPresent());
    assertEquals(237L, PayloadEntity.get(subject, "primitiveLongValue").get());

    subject.setPrimitiveDoubleValue(932.1876D);
    assertTrue(PayloadEntity.get(subject, "primitiveDoubleValue").isPresent());
    assertEquals(932.1876D, PayloadEntity.get(subject, "primitiveDoubleValue").get());

    subject.setPrimitiveFloatValue(12787.847F);
    assertTrue(PayloadEntity.get(subject, "primitiveFloatValue").isPresent());
    assertEquals(12787.847F, PayloadEntity.get(subject, "primitiveFloatValue").get());

    subject.setBooleanValue(true);
    assertTrue(PayloadEntity.get(subject, "booleanValue").isPresent());
    assertEquals(true, PayloadEntity.get(subject, "booleanValue").get());

    subject.setIntegerValue(8907);
    assertTrue(PayloadEntity.get(subject, "integerValue").isPresent());
    assertEquals(8907, PayloadEntity.get(subject, "integerValue").get());

    subject.setShortValue((short) 45);
    assertTrue(PayloadEntity.get(subject, "shortValue").isPresent());
    assertEquals((short) 45, PayloadEntity.get(subject, "shortValue").get());

    subject.setLongValue(237L);
    assertTrue(PayloadEntity.get(subject, "longValue").isPresent());
    assertEquals(237L, PayloadEntity.get(subject, "longValue").get());

    subject.setDoubleValue(932.1876D);
    assertTrue(PayloadEntity.get(subject, "doubleValue").isPresent());
    assertEquals(932.1876D, PayloadEntity.get(subject, "doubleValue").get());

    subject.setFloatValue(12787.847F);
    assertTrue(PayloadEntity.get(subject, "floatValue").isPresent());
    assertEquals(12787.847F, PayloadEntity.get(subject, "floatValue").get());

    subject.setInstantValue(Instant.ofEpochMilli(1583896837907L));
    assertTrue(PayloadEntity.get(subject, "instantValue").isPresent());
    assertEquals(Instant.ofEpochMilli(1583896837907L), PayloadEntity.get(subject, "instantValue").get());

    subject.setTimestampValue(Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")));
    assertTrue(PayloadEntity.get(subject, "timestampValue").isPresent());
    assertEquals(Instant.parse("2020-03-07T14:17:02Z"), PayloadEntity.get(subject, "timestampValue").get());

    subject.setBigIntegerValue(BigInteger.valueOf(309879125L));
    assertTrue(PayloadEntity.get(subject, "bigIntegerValue").isPresent());
    assertEquals(BigInteger.valueOf(309879125L), PayloadEntity.get(subject, "bigIntegerValue").get());

    subject.setStringValue(String.valueOf(4775));
    assertTrue(PayloadEntity.get(subject, "stringValue").isPresent());
    assertEquals(String.valueOf(4775), PayloadEntity.get(subject, "stringValue").get());

    subject.setUuidValue(UUID.fromString("f208b87d-12b3-4dc3-ae30-6d757e0db6a2"));
    assertTrue(PayloadEntity.get(subject, "uuidValue").isPresent());
    assertEquals(UUID.fromString("f208b87d-12b3-4dc3-ae30-6d757e0db6a2"), PayloadEntity.get(subject, "uuidValue").get());

    subject.setProprietaryValue(new MockEntity().setName("Inner Piece"));
    assertTrue(PayloadEntity.get(subject, "proprietaryValue").isPresent());
    assertEquals("Inner Piece", ((MockEntity) PayloadEntity.get(subject, "proprietaryValue").get()).getName());
  }

  @Test
  public void set_variousTypes() throws RestApiException {
    MockSuperEntity subject = new MockSuperEntity();

    PayloadEntity.set(subject, "primitiveBooleanValue", true);
    assertEquals(Boolean.TRUE, subject.getPrimitiveBooleanValue());

    PayloadEntity.set(subject, "primitiveIntValue", 8907);
    assertEquals(8907, subject.getPrimitiveIntValue());

    PayloadEntity.set(subject, "primitiveShortValue", (short) 45);
    assertEquals((short) 45, subject.getPrimitiveShortValue());

    PayloadEntity.set(subject, "primitiveLongValue", 237L);
    assertEquals(237L, subject.getPrimitiveLongValue());

    PayloadEntity.set(subject, "primitiveDoubleValue", 932.1876D);
    assertEquals(932.1876D, subject.getPrimitiveDoubleValue(), 0.01);

    PayloadEntity.set(subject, "primitiveFloatValue", 12787.847F);
    assertEquals(12787.847F, subject.getPrimitiveFloatValue(), 0.01);

    PayloadEntity.set(subject, "booleanValue", true);
    assertEquals(Boolean.TRUE, subject.getBooleanValue());

    PayloadEntity.set(subject, "integerValue", 8907);
    assertEquals(Integer.valueOf(8907), subject.getIntegerValue());

    PayloadEntity.set(subject, "shortValue", (short) 45);
    assertEquals(Short.valueOf((short) 45), subject.getShortValue());

    PayloadEntity.set(subject, "longValue", 237L);
    assertEquals(Long.valueOf(237L), subject.getLongValue());

    PayloadEntity.set(subject, "doubleValue", 932.1876D);
    assertEquals(Double.valueOf(932.1876D), subject.getDoubleValue());

    PayloadEntity.set(subject, "floatValue", 12787.847F);
    assertEquals(Float.valueOf(12787.847F), subject.getFloatValue());

    PayloadEntity.set(subject, "instantValue", Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")));
    assertEquals(Instant.parse("2020-03-07T14:17:02Z"), subject.getInstantValue());

    PayloadEntity.set(subject, "instantValue", Instant.ofEpochMilli(1583896837907L));
    assertEquals(Instant.ofEpochMilli(1583896837907L), subject.getInstantValue());

    PayloadEntity.set(subject, "instantValue", "2020-03-07T14:17:02Z");
    assertEquals(Instant.parse("2020-03-07T14:17:02Z"), subject.getInstantValue());

    PayloadEntity.set(subject, "timestampValue", Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")));
    assertEquals(Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")), subject.getTimestampValue());

    PayloadEntity.set(subject, "timestampValue", Instant.parse("2020-03-07T14:17:02Z"));
    assertEquals(Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")), subject.getTimestampValue());

    PayloadEntity.set(subject, "timestampValue", "2020-03-07T14:17:02Z");
    assertEquals(Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")), subject.getTimestampValue());

    PayloadEntity.set(subject, "bigIntegerValue", BigInteger.valueOf(309879125L));
    assertEquals(BigInteger.valueOf(309879125L), subject.getBigIntegerValue());

    PayloadEntity.set(subject, "stringValue", String.valueOf(4775));
    assertEquals(String.valueOf(4775), subject.getStringValue());

    PayloadEntity.set(subject, "stringValue", Timestamp.from(Instant.parse("2020-03-07T14:17:02Z")));
    assertEquals("2020-03-07T14:17:02Z", subject.getStringValue());

    PayloadEntity.set(subject, "uuidValue", UUID.fromString("f208b87d-12b3-4dc3-ae30-6d757e0db6a2"));
    assertEquals(UUID.fromString("f208b87d-12b3-4dc3-ae30-6d757e0db6a2"), subject.getUuidValue());

    PayloadEntity.set(subject, "proprietaryValue", new MockEntity().setName("Inner Piece"));
    assertEquals("Inner Piece", subject.getProprietaryValue().getName());

    PayloadEntity.set(subject, "stringValue", MockEnum.ValueA);
    assertEquals("ValueA", subject.getStringValue());
  }

  @Test
  public void set_willFailIfSetterAcceptsNoParameters() throws RestApiException {
    MockSuperEntity subject = new MockSuperEntity();

    failure.expect(RestApiException.class);
    failure.expectMessage("Setter accepts no parameters");

    PayloadEntity.set(subject, "willFailBecauseAcceptsNoParameters", true);
  }

  @Test
  public void set_willFailIfSetterHasProtectedAccess() throws RestApiException {
    MockSuperEntity subject = new MockSuperEntity();

    failure.expect(RestApiException.class);
    failure.expectMessage("MockSuperEntity has no attribute 'willFailBecauseNonexistent'");

    PayloadEntity.set(subject, "willFailBecauseNonexistent", "testing");
  }

  /**
   Mock enum value
   */
  private enum MockEnum {
    ValueA;
  }
}
