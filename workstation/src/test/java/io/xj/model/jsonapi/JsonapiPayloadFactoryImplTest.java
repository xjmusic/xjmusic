// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model.jsonapi;

import io.xj.model.entity.EntityFactory;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.util.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonapiPayloadFactoryImplTest {
  JsonapiPayloadFactory subject;
  EntityFactory entityFactory;

  @BeforeEach
  public void setUp() {
    var jsonProvider = new JsonProviderImpl();
    entityFactory = new EntityFactoryImpl(jsonProvider);
    subject = new JsonapiPayloadFactoryImpl(entityFactory);
  }

  @Test
  public void serialize_deserialize() throws JsonapiException {
    entityFactory.register("Widget").createdBy(Widget::new).withAttribute("name");
    JsonapiPayload jsonapiPayload = subject.newJsonapiPayload();
    jsonapiPayload.setDataOne(subject.toPayloadObject(
        new Widget()
            .setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7"))
            .setName("Jams")));

    String result = subject.serialize(jsonapiPayload);

    JsonapiPayload deserialized = subject.deserialize(result);

    assertTrue(deserialized.getDataOne().isPresent());
    assertTrue(deserialized.getDataOne().get().getAttribute("name").isPresent());
    assertEquals("Jams", deserialized.getDataOne().get().getAttribute("name").get());
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
    jsonapiPayload.setDataMany(List.of(subject.toPayloadObject(new Widget().setId(UUID.fromString("6dfb9b9a-28df-4dd8-bdd5-22652e47a0d7")).setName("Jams"))));
    jsonapiPayload.setIncluded(List.of(subject.toPayloadObject(new Widget().setId(UUID.fromString("9ec331cc-f987-4682-9975-f569949680a1")).setName("Stones"))));

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

    var e = assertThrows(JsonapiException.class, () -> subject.deserialize("this is absolutely not json"));
    assertTrue(e.getMessage().contains("Failed to deserialize JSON"));
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

    var e = assertThrows(JsonapiException.class, () -> subject.toOne(jsonapiPayloadObject));
    assertTrue(e.getMessage().contains("Failed to locate instance provider for widgets"));
  }

}
