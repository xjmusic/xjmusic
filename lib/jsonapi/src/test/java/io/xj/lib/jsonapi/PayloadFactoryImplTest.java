// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.Program;
import io.xj.lib.entity.EntityFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PayloadFactoryImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private PayloadFactory subject;
  private EntityFactory entityFactory;

  @Before
  public void setUp() {
    var injector = Guice.createInjector(new JsonApiModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(ConfigFactory.empty());
      }
    });
    entityFactory = injector.getInstance(EntityFactory.class);
    subject = injector.getInstance(PayloadFactory.class);
  }

  @Test
  public void serialize() throws JsonApiException {
    entityFactory.register("Program").createdBy(Program::getDefaultInstance).withAttribute("name");
    Payload payload = subject.newPayload();
    payload.setDataOne(subject.toPayloadObject(
      Program.newBuilder()
        .setId("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7")
        .setName("Jams")
        .build()));

    String result = subject.serialize(payload);

    assertEquals("{\"data\":{\"id\":\"6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7\",\"type\":\"programs\",\"attributes\":{\"name\":\"Jams\"}}}", result);
  }

  @Test
  public void deserialize() throws JsonApiException {
    entityFactory.register("Program").createdBy(Program::getDefaultInstance).withAttribute("name");
    Payload payload = subject.newPayload();
    payload.setDataOne(subject.toPayloadObject(Program.newBuilder()
      .setId("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7")
      .setName("Jams")
      .build()));

    Payload result = subject.deserialize("{\"data\":{\"id\":\"6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7\",\"type\":\"programs\",\"attributes\":{\"name\":\"Jams\"}}}");

    assertTrue(result.getDataOne().isPresent());
    assertTrue(result.getDataOne().get().getAttribute("name").isPresent());
    assertEquals("Jams", result.getDataOne().get().getAttribute("name").get());
  }

  @Test
  public void toMany_withIncluded() throws JsonApiException {
    entityFactory.register("Program").createdBy(Program::getDefaultInstance).withAttribute("name");
    Payload payload = subject.newPayload();
    payload.setDataMany(ImmutableList.of(subject.toPayloadObject(Program.newBuilder().setId("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7").setName("Jams").build())));
    payload.setIncluded(ImmutableList.of(subject.toPayloadObject(Program.newBuilder().setId("9ec331cc-f987-4682-9975-f569949680a1").setName("Stones").build())));

    Collection<?> result = subject.toMany(payload);

    assertEquals(2, result.size());
  }

  @Test
  public void deserialize_failsWithBadJson() throws JsonApiException {
    entityFactory.register("Program").createdBy(Program::getDefaultInstance).withAttribute("name");
    Payload payload = subject.newPayload();
    payload.setDataOne(subject.toPayloadObject(
      Program.newBuilder()
        .setId("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7")
        .setName("Jams")
        .build()));

    failure.expect(JsonApiException.class);
    failure.expectMessage("Failed to deserialize JSON");

    subject.deserialize("this is absolutely not json");
  }

  @Test
  public void toInstance() throws JsonApiException {
    entityFactory.register(Program.class)
      .createdBy(Program::getDefaultInstance)
      .withAttribute("name");
    PayloadObject payloadObject = subject.toPayloadObject(
      Program.newBuilder()
        .setId("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7")
        .setName("Jams")
        .build());

    Program result = subject.toOne(payloadObject);

    assertEquals("Jams", result.getName());
  }

  @Test
  public void toInstance_failsWithNoInstanceProvider() throws JsonApiException {
    entityFactory.register(Program.class)
      .withAttribute("name");
    PayloadObject payloadObject = subject.toPayloadObject(
      Program.newBuilder()
        .setId("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7")
        .setName("Jams")
        .build());

    failure.expect(JsonApiException.class);
    failure.expectMessage("Failed to locate instance provider for programs");

    subject.toOne(payloadObject);
  }

}
