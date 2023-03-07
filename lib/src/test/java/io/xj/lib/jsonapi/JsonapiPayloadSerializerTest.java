// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.xj.lib.Widget;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.JsonProviderImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;

/**
 * Payload serializer test
 * <p>
 * Created by Charney Kaye on 2020/03/09
 */
public class JsonapiPayloadSerializerTest {
  private JsonapiPayloadFactory jsonapiPayloadFactory;
  private EntityFactory entityFactory;

  @Before
  public void setUp() {
    var jsonProvider = new JsonProviderImpl();
    entityFactory = new EntityFactoryImpl(jsonProvider);
    jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    entityFactory.register(Widget.class);
  }

  @Test
  public void serialize() throws JsonapiException {
    Widget widget = new Widget()
      .setId(UUID.randomUUID())
      .setName("test_print");
    JsonapiPayload jsonapiPayload = jsonapiPayloadFactory.setDataEntity(jsonapiPayloadFactory.newJsonapiPayload(), widget);

    String result = jsonapiPayloadFactory.serialize(jsonapiPayload);

    AssertPayload.assertPayload(jsonapiPayloadFactory.deserialize(result))
      .hasDataOne("widgets", widget.getId().toString());
  }

  @Test
  public void serializeOne() throws JsonapiException {
    JsonapiPayload jsonapiPayload = jsonapiPayloadFactory.newJsonapiPayload();
    Widget superwidget = new Widget()
      .setId(UUID.randomUUID())
      .setName("Test Widget");
    jsonapiPayloadFactory.setDataEntity(jsonapiPayload, superwidget);

    String result = jsonapiPayloadFactory.serialize(jsonapiPayload);

    AssertPayload.assertPayload(jsonapiPayloadFactory.deserialize(result))
      .hasDataOne("widgets", superwidget.getId().toString());
  }

  @Test
  public void serializeOne_withBelongsTo() throws JsonapiException {
    entityFactory.register("Superwidget");
    entityFactory.register("Widget").belongsTo("Superwidget");
    JsonapiPayload jsonapiPayload = jsonapiPayloadFactory.newJsonapiPayload();
    Widget superwidget = new Widget()
      .setId(UUID.randomUUID())
      .setName("y");
    Widget widget = new Widget()
      .setId(UUID.randomUUID())
      .setSuperwidgetId(superwidget.getId())
      .setName("x");
    jsonapiPayloadFactory.setDataEntity(jsonapiPayload, widget);

    String result = jsonapiPayloadFactory.serialize(jsonapiPayload);

    AssertPayload.assertPayload(jsonapiPayloadFactory.deserialize(result))
      .hasDataOne("widgets", widget.getId().toString())
      .belongsTo("Superwidget", superwidget.getId().toString());
  }

  /**
   * JSON:API serializer must not include relationship payload where there is none https://www.pivotaltracker.com/story/show/175792528
   */
  @Test
  public void serializeOne_withBelongsTo_empty() throws JsonapiException {
    entityFactory.register("Superwidget");
    entityFactory.register("Widget").belongsTo("Superwidget");
    JsonapiPayload jsonapiPayload = jsonapiPayloadFactory.newJsonapiPayload();
    Widget widget = new Widget()
      .setId(UUID.randomUUID())
      .setSuperwidgetId(null)
      .setName("x");
    jsonapiPayloadFactory.setDataEntity(jsonapiPayload, widget);

    String result = jsonapiPayloadFactory.serialize(jsonapiPayload);

    assertFalse(result.contains("superwidgets"));
  }

  @Test
  public void serializeOne_withHasMany() throws JsonapiException {
    entityFactory.register("Widget").hasMany("Widget");
    Widget widget0 = new Widget()
      .setId(UUID.randomUUID())
      .setName("y");
    Widget widget1 = new Widget()
      .setId(UUID.randomUUID())
      .setSuperwidgetId(widget0.getId())
      .setName("x");
    Widget widget2 = new Widget()
      .setId(UUID.randomUUID())
      .setSuperwidgetId(widget1.getId())
      .setName("b");
    Widget widget3 = new Widget()
      .setId(UUID.randomUUID())
      .setSuperwidgetId(widget1.getId())
      .setName("c");
    JsonapiPayloadObject mainObj = jsonapiPayloadFactory.toPayloadObject(widget1, ImmutableSet.of(widget2, widget3));
    JsonapiPayload jsonapiPayload = jsonapiPayloadFactory.newJsonapiPayload().setDataOne(mainObj);
    jsonapiPayloadFactory.addIncluded(jsonapiPayload, jsonapiPayloadFactory.toPayloadObject(widget2));
    jsonapiPayloadFactory.addIncluded(jsonapiPayload, jsonapiPayloadFactory.toPayloadObject(widget3));

    String result = jsonapiPayloadFactory.serialize(jsonapiPayload);

    JsonapiPayload resultJsonapiPayload = jsonapiPayloadFactory.deserialize(result);
    AssertPayload.assertPayload(resultJsonapiPayload)
      .hasIncluded("widgets", ImmutableList.of(widget2, widget3))
      .hasDataOne("widgets", widget1.getId().toString());
  }

  @Test
  public void serializeMany() throws JsonapiException {
    JsonapiPayload jsonapiPayload = jsonapiPayloadFactory.newJsonapiPayload();
    Widget accountA = new Widget()
      .setId(UUID.randomUUID())
      .setName("Test Widget A");
    Widget accountB = new Widget()
      .setId(UUID.randomUUID())
      .setName("Test Widget B");
    Widget accountC = new Widget()
      .setId(UUID.randomUUID())
      .setName("Test Widget C");
    jsonapiPayloadFactory.setDataEntities(jsonapiPayload, ImmutableList.of(accountA, accountB, accountC));

    String result = jsonapiPayloadFactory.serialize(jsonapiPayload);

    AssertPayload.assertPayload(jsonapiPayloadFactory.deserialize(result))
      .hasDataMany("widgets", ImmutableList.of(
        accountA.getId().toString(),
        accountB.getId().toString(),
        accountC.getId().toString()))
      .hasIncluded("widgets", ImmutableList.of());
  }

}
