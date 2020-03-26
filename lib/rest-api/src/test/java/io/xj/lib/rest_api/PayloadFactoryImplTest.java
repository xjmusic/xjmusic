// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PayloadFactoryImplTest {
  private PayloadFactory subject;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Before
  public void setUp() {
    Injector injector = Guice.createInjector(new RestApiModule());
    subject = injector.getInstance(PayloadFactory.class);
  }

  @Test
  public void register_returnsSameSchema_forExistingType() throws RestApiException {
    subject.register("MockEntity").createdBy(MockEntity::new).belongsTo("OtherThing");

    assertEquals(ImmutableSet.of("otherThing"), subject.register("mock-entities").getBelongsTo());
  }

  @Test
  public void register_returnsSameSchema_forExistingTypeClass() throws RestApiException {
    subject.register(MockEntity.class).createdBy(MockEntity::new).belongsTo("OtherThing");

    assertEquals(ImmutableSet.of("otherThing"), subject.register(MockEntity.class).getBelongsTo());
  }

  @Test
  public void register_returnsSameSchema_forExisting_TypeThenClass() throws RestApiException {
    subject.register("MockEntity").createdBy(MockEntity::new).belongsTo("OtherThing");

    assertEquals(ImmutableSet.of("otherThing"), subject.register(MockEntity.class).getBelongsTo());
  }

  @Test
  public void register_returnsSameSchema_forExisting_ClassThenType() throws RestApiException {
    subject.register(MockEntity.class).createdBy(MockEntity::new).belongsTo("OtherThing");

    assertEquals(ImmutableSet.of("otherThing"), subject.register("MockEntity").getBelongsTo());
  }

  @Test
  public void register_basicTypeCreator() throws RestApiException {
    subject.register("MockEntity").createdBy(MockEntity::new);

    assertEquals(MockEntity.class, subject.getInstance("mock-entity").getClass());
  }

  @Test
  public void register_withBelongsTo() throws RestApiException {
    subject.register("MockEntity").belongsTo("FictionalEntity");

    assertEquals(ImmutableSet.of("fictionalEntity"), subject.getBelongsTo("mock-entities"));
  }

  @Test
  public void register_withAttributesAndBelongsTo() throws RestApiException {
    subject.register("MockEntity");
    subject.register("FictionalEntity").withAttribute("name").belongsTo("mockEntity").createdBy(MockEntity::new);

    assertEquals(ImmutableSet.of("mockEntity"),
            subject.getBelongsTo("fictional-entity"));

    assertEquals(ImmutableSet.of("name"),
            subject.getAttributes("fictional-entity"));
  }

  @Test
  public void register_withAttributes() throws RestApiException {
    subject.register("FictionalEntity").withAttribute("name").createdBy(MockEntity::new);

    assertEquals(ImmutableSet.of("name"), subject.getAttributes("fictional-entity"));
  }

  @Test
  public void getBelongsToType() throws RestApiException {
    subject.register("OtherEntity");
    subject.register("FakeEntity").belongsTo("otherEntity").createdBy(MockEntity::new);

    assertEquals(ImmutableSet.of("otherEntity"), subject.getBelongsTo("fake-entity"));
  }

  @Test
  public void getBelongsToType_emptyBelongsTo() throws RestApiException {
    subject.register("OtherEntity");

    assertTrue(subject.getBelongsTo("other-entity").isEmpty());
  }

  @Test
  public void getBelongsToType_exceptionIfDoesNotExist() throws RestApiException {
    failure.expect(RestApiException.class);
    failure.expectMessage("Cannot get belongs-to type unknown type: other-entities");

    subject.getBelongsTo("other-entity");
  }

  @Test
  public void getAttributes() throws RestApiException {
    subject.register("FalseEntity").withAttribute("yarn").createdBy(MockEntity::new);

    assertEquals(ImmutableSet.of("yarn"), subject.getAttributes("false-entity"));
  }

  @Test
  public void serialize() throws RestApiException {
    subject.register("MockEntity").createdBy(MockEntity::new).withAttribute("name");
    Payload payload = subject.newPayload();
    payload.setDataOne(subject.toPayloadObject(new MockEntity().setId("12").setName("Jams")));

    String result = subject.serialize(payload);

    assertEquals("{\"data\":{\"id\":\"12\",\"type\":\"mock-entities\",\"attributes\":{\"name\":\"Jams\"}}}", result);
  }

  @Test
  public void deserialize() throws RestApiException {
    subject.register("MockEntity").createdBy(MockEntity::new).withAttribute("name");
    Payload payload = subject.newPayload();
    payload.setDataOne(subject.toPayloadObject(new MockEntity().setId("12").setName("Jams")));

    Payload result = subject.deserialize("{\"data\":{\"id\":\"12\",\"type\":\"mock-entities\",\"attributes\":{\"name\":\"Jams\"}}}");

    assertTrue(result.getDataOne().isPresent());
    assertTrue(result.getDataOne().get().getAttribute("name").isPresent());
    assertEquals("Jams", result.getDataOne().get().getAttribute("name").get());
  }

  @Test
  public void deserialize_failsWithBadJson() throws RestApiException {
    subject.register("MockEntity").createdBy(MockEntity::new).withAttribute("name");
    Payload payload = subject.newPayload();
    payload.setDataOne(subject.toPayloadObject(new MockEntity().setId("12").setName("Jams")));

    failure.expect(RestApiException.class);
    failure.expectMessage("Failed to deserialize JSON");

    Payload result = subject.deserialize("this is absolutely not json");
  }
}
