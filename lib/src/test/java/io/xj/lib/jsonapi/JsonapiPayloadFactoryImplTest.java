// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.lib.Widget;
import io.xj.lib.entity.EntityFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonapiPayloadFactoryImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private JsonapiPayloadFactory subject;
  private EntityFactory entityFactory;

  @Before
  public void setUp() {
    var injector = Guice.createInjector(new JsonapiModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(ConfigFactory.empty());
      }
    });
    entityFactory = injector.getInstance(EntityFactory.class);
    subject = injector.getInstance(JsonapiPayloadFactory.class);
  }

  @Test
  public void serialize() throws JsonapiException {
    entityFactory.register("Widget").createdBy(Widget::new).withAttribute("name");
    JsonapiPayload jsonapiPayload = subject.newJsonapiPayload();
    jsonapiPayload.setDataOne(subject.toPayloadObject(
      new Widget()
        .setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7"))
        .setName("Jams")));

    String result = subject.serialize(jsonapiPayload);

    assertEquals("{\"data\":{\"id\":\"6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7\",\"type\":\"widgets\",\"attributes\":{\"name\":\"Jams\",\"position\":0.0}}}", result);
  }

  @Test
  public void deserialize() throws JsonapiException {
    entityFactory.register("Widget").createdBy(Widget::new).withAttribute("name");
    JsonapiPayload jsonapiPayload = subject.newJsonapiPayload();
    jsonapiPayload.setDataOne(subject.toPayloadObject(new Widget()
      .setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7"))
      .setName("Jams")));

    JsonapiPayload result = subject.deserialize("{\"data\":{\"id\":\"6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7\",\"type\":\"widgets\",\"attributes\":{\"name\":\"Jams\"}}}");

    assertTrue(result.getDataOne().isPresent());
    assertTrue(result.getDataOne().get().getAttribute("name").isPresent());
    assertEquals("Jams", result.getDataOne().get().getAttribute("name").get());
  }

  @Test
  public void toMany_withIncluded() throws JsonapiException {
    entityFactory.register("Widget").createdBy(Widget::new).withAttribute("name");
    JsonapiPayload jsonapiPayload = subject.newJsonapiPayload();
    jsonapiPayload.setDataMany(ImmutableList.of(subject.toPayloadObject(new Widget().setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7")).setName("Jams"))));
    jsonapiPayload.setIncluded(ImmutableList.of(subject.toPayloadObject(new Widget().setId(UUID.fromString("9ec331cc-f987-4682-9975-f569949680a1")).setName("Stones"))));

    Collection<?> result = subject.toMany(jsonapiPayload);

    assertEquals(2, result.size());
  }

  @Test
  public void deserialize_failsWithBadJson() throws JsonapiException {
    entityFactory.register("Widget").createdBy(Widget::new).withAttribute("name");
    JsonapiPayload jsonapiPayload = subject.newJsonapiPayload();
    jsonapiPayload.setDataOne(subject.toPayloadObject(
      new Widget()
        .setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7"))
        .setName("Jams")));

    failure.expect(JsonapiException.class);
    failure.expectMessage("Failed to deserialize JSON");

    subject.deserialize("this is absolutely not json");
  }

  @Test
  public void toInstance() throws JsonapiException {
    entityFactory.register(Widget.class)
      .createdBy(Widget::new)
      .withAttribute("name");
    JsonapiPayloadObject jsonapiPayloadObject = subject.toPayloadObject(
      new Widget()
        .setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7"))
        .setName("Jams"));

    Widget result = subject.toOne(jsonapiPayloadObject);

    assertEquals("Jams", result.getName());
  }

  @Test
  public void toInstance_failsWithNoInstanceProvider() throws JsonapiException {
    entityFactory.register(Widget.class)
      .withAttribute("name");
    JsonapiPayloadObject jsonapiPayloadObject = subject.toPayloadObject(
      new Widget()
        .setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7"))
        .setName("Jams"));

    failure.expect(JsonapiException.class);
    failure.expectMessage("Failed to locate instance provider for widgets");

    subject.toOne(jsonapiPayloadObject);
  }

}
