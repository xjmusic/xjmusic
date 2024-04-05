// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.jsonapi;

import io.xj.hub.entity.EntityFactory;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.entity.Superwidget;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.util.ValueException;
import io.xj.hub.util.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.xj.hub.jsonapi.AssertPayloadObject.assertPayloadObject;
import static io.xj.hub.util.Assertion.assertSameItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 Payload object test
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class JsonapiJsonapiPayloadObjectTest {
  JsonapiPayloadFactory jsonapiPayloadFactory;
  EntityFactory entityFactory;
  JsonapiPayloadObject subject;

  @BeforeEach
  public void setUp() {
    var jsonProvider = new JsonProviderImpl();
    entityFactory = new EntityFactoryImpl(jsonProvider);
    jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    entityFactory.register(Widget.class);
    subject = jsonapiPayloadFactory.newPayloadObject();
  }

  @Test
  public void add() throws JsonapiException {
    Widget parentEntity1 = new Widget()
      .setId(UUID.randomUUID())
      .setName("Test Widget");
    subject.add("parentEntity", jsonapiPayloadFactory.setDataEntity(jsonapiPayloadFactory.newJsonapiPayload(), parentEntity1));

    assertTrue(subject.getRelationships().get("parentEntity").getDataOne().isPresent());
    assertEquals(parentEntity1.getId().toString(), subject.getRelationships().get("parentEntity").getDataOne().get().getId());
  }

  @Test
  public void addIfRelated() throws IOException, JsonapiException {
    Superwidget superwidget1 = new Superwidget()
      .setId(UUID.randomUUID())
      .setName("test.jpg");
    Widget widget2 = new Widget()
      .setId(UUID.randomUUID())
      .setName("sham")
      .setSuperwidgetId(superwidget1.getId());
    entityFactory.register(Superwidget.class);
    entityFactory.register(Widget.class).belongsTo(Superwidget.class);
    subject = jsonapiPayloadFactory.toPayloadObject(superwidget1);
    JsonapiPayloadObject rel = jsonapiPayloadFactory.toPayloadObject(widget2);

    jsonapiPayloadFactory.addIfRelated(subject, rel);

    assertPayloadObject(subject)
      .hasMany(Widget.class, List.of(widget2));
  }

  @Test
  public void getAttribute_setAttribute() {
    subject
      .setAttribute("swanky", 23)
      .setAttribute("times", 52);

    assertTrue(subject.getAttribute("swanky").isPresent());
    assertEquals(23, subject.getAttribute("swanky").get());
  }

  @Test
  public void toMinimal() throws JsonapiException {
    Widget parentEntity1 = new Widget()
      .setId(UUID.randomUUID())
      .setName("Test Widget");
    subject = jsonapiPayloadFactory.toPayloadObject(parentEntity1);

    JsonapiPayloadObject result = subject.toMinimal();

    assertEquals(subject.getType(), result.getType());
    assertEquals(subject.getId(), result.getId());
    assertFalse(result.getAttributes().containsKey("name"));
  }

  @Test
  public void getAttributes_setAttributes() throws ValueException {
    Map<String, Object> attr = Map.of(
      "kittens", "cute",
      "puppies", 5
    );
    subject.setAttributes(attr);

    assertSameItems(attr, subject.getAttributes());
  }

  @Test
  public void getId_setId() {
    subject.setId("15");

    assertEquals("15", subject.getId());
  }

  @Test
  public void getLinks_setLinks() throws ValueException {
    Map<String, String> links = Map.of(
      "kittens", "https://kittens.com/",
      "puppies", "https://puppies.com/"
    );
    subject.setLinks(links);

    assertSameItems(links, subject.getLinks());
  }

  @Test
  public void getRelationshipObject() throws JsonapiException {
    Widget parentEntity1 = new Widget()
      .setId(UUID.randomUUID())
      .setName("Test Widget");
    subject.add("parentEntity", jsonapiPayloadFactory.setDataEntity(jsonapiPayloadFactory.newJsonapiPayload(), parentEntity1));

    assertTrue(subject.getRelationshipDataOne("parentEntity").isPresent());
    assertEquals(parentEntity1.getId().toString(), subject.getRelationshipDataOne("parentEntity").get().getId());
  }

  @Test
  public void getRelationships_setRelationships() throws JsonapiException {
    subject.setRelationships(Map.of(
      "parentEntity", jsonapiPayloadFactory.setDataEntity(jsonapiPayloadFactory.newJsonapiPayload(), new Widget()
        .setId(UUID.randomUUID())
        .setName("Test Widget")
      ),
      "childEntity", jsonapiPayloadFactory.setDataEntity(jsonapiPayloadFactory.newJsonapiPayload(), new Widget()
        .setId(UUID.randomUUID())
        .setName("Test Widget")
      )
    ));

    assertEquals(2, subject.getRelationships().size());
  }

  @Test
  public void getType_setType() {
    subject.setType("widgets");

    assertEquals("widgets", subject.getType());
  }

  @Test
  public void setType_fromClass() {
    assertEquals("widgets", subject.setType(Widget.class).getType());
  }

  @Test
  public void setAttributes_nullValueSetsNullValueInstance() throws ValueException {
    Map<String, Object> attr = new HashMap<>();
    attr.put("kittens", "cute");
    attr.put("puppies", null);
    subject.setAttributes(attr);

    assertSameItems(attr, subject.getAttributes());
  }

  @Test
  public void isSame() {
    UUID id = UUID.randomUUID();
    subject.setId(id.toString());
    subject.setType("Widget");

    assertTrue(subject.isSame(new Widget().setId(id)));
    assertFalse(subject.isSame(new Widget().setId(UUID.randomUUID())));
  }

  @Test
  public void isSame_subjectStringId_compareToUuid() {
    UUID id = UUID.randomUUID();
    subject.setId(id.toString());
    subject.setType("Widget");
    Widget compareTo = new Widget().setId(id);

    assertTrue(subject.isSame(compareTo));
  }
}
