// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PayloadFactoryImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private PayloadFactory subject;
  private EntityFactory entityFactory;

  @Before
  public void setUp() {
    Injector injector = Guice.createInjector(new JsonApiModule());
    entityFactory = injector.getInstance(EntityFactory.class);
    subject = injector.getInstance(PayloadFactory.class);
  }

  @Test
  public void serialize() throws JsonApiException {
    entityFactory.register("MockEntity").createdBy(MockEntity::new).withAttribute("name");
    Payload payload = subject.newPayload();
    payload.setDataOne(subject.toPayloadObject(new MockEntity().setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7")).setName("Jams")));

    String result = subject.serialize(payload);

    assertEquals("{\"data\":{\"id\":\"6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7\",\"type\":\"mock-entities\",\"attributes\":{\"name\":\"Jams\"}}}", result);
  }

  @Test
  public void deserialize() throws JsonApiException {
    entityFactory.register("MockEntity").createdBy(MockEntity::new).withAttribute("name");
    Payload payload = subject.newPayload();
    payload.setDataOne(subject.toPayloadObject(new MockEntity().setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7")).setName("Jams")));

    Payload result = subject.deserialize("{\"data\":{\"id\":\"6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7\",\"type\":\"mock-entities\",\"attributes\":{\"name\":\"Jams\"}}}");

    assertTrue(result.getDataOne().isPresent());
    assertTrue(result.getDataOne().get().getAttribute("name").isPresent());
    assertEquals("Jams", result.getDataOne().get().getAttribute("name").get());
  }

  @Test
  public void toMany_withIncluded() throws JsonApiException {
    entityFactory.register("MockEntity").createdBy(MockEntity::new).withAttribute("name");
    Payload payload = subject.newPayload();
    payload.setDataMany(ImmutableList.of(subject.toPayloadObject(new MockEntity().setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7")).setName("Jams"))));
    payload.setIncluded(ImmutableList.of(subject.toPayloadObject(new MockEntity().setId(UUID.fromString("9ec331cc-f987-4682-9975-f569949680a1")).setName("Stones"))));

    Collection<Entity> result = subject.toMany(payload);

    assertEquals(2, result.size());
  }

  @Test
  public void deserialize_failsWithBadJson() throws JsonApiException {
    entityFactory.register("MockEntity").createdBy(MockEntity::new).withAttribute("name");
    Payload payload = subject.newPayload();
    payload.setDataOne(subject.toPayloadObject(new MockEntity().setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7")).setName("Jams")));

    failure.expect(JsonApiException.class);
    failure.expectMessage("Failed to deserialize JSON");

    Payload result = subject.deserialize("this is absolutely not json");
  }

  @Test
  public void toInstance() throws JsonApiException {
    entityFactory.register("MockEntity").createdBy(MockEntity::new).withAttribute("name");
    PayloadObject payloadObject = subject.toPayloadObject(new MockEntity().setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7")).setName("Jams"));

    MockEntity result = subject.toOne(payloadObject);

    assertEquals("Jams", result.getName());
  }

  @Test
  public void toInstance_failsWithNoInstanceProvider() throws JsonApiException {
    entityFactory.register("MockEntity").withAttribute("name");
    PayloadObject payloadObject = subject.toPayloadObject(new MockEntity().setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7")).setName("Jams"));

    failure.expect(JsonApiException.class);
    failure.expectMessage("Failed to locate instance provider for mock-entities");

    MockEntity result = subject.toOne(payloadObject);
  }

}
