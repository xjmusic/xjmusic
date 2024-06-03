// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model.entity;

import io.xj.model.util.Widget;
import io.xj.model.json.JsonProviderImpl;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityStoreImplTest {
  EntityStore subject;

  @BeforeEach
  public void setUp() {
    var jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);

    // Some topology
    entityFactory.register("Widget")
      .withAttribute("name")
      .belongsTo(Superwidget.class)
      .createdBy(Widget::new);

    subject = new EntityStoreImpl();
  }

  @Test
  public void put_get_Widget() throws EntityStoreException {
    Widget widget = new Widget()
      .setId(UUID.randomUUID())
      .setName("bingo");

    subject.put(widget);
    Widget result = subject.get(Widget.class, widget.getId()).orElseThrow();

    Assert.assertEquals(widget.getId(), result.getId());
    Assert.assertEquals("bingo", result.getName());
  }

  @Test
  public void putAll_getAll() throws EntityStoreException {
    Superwidget superwidget = new Superwidget()
      .setId(UUID.randomUUID())
      .setName("parent5");
    Widget widget2 = new Widget()
      .setId(UUID.randomUUID())
      .setSuperwidgetId(superwidget.getId())
      .setName("Test2");
    Widget widget3 = new Widget()
      .setId(UUID.randomUUID())
      .setSuperwidgetId(superwidget.getId())
      .setName("Test7");
    Widget widget2_widget0 = new Widget()
      .setId(UUID.randomUUID())
      .setSuperwidgetId(widget2.getId())
      .setName("Test2_A");
    Widget widget3_widget0 = new Widget()
      .setId(UUID.randomUUID())
      .setSuperwidgetId(widget3.getId())
      .setName("Test7_B");
    Widget widget3_widget1 = new Widget()
      .setId(UUID.randomUUID())
      .setSuperwidgetId(widget3.getId())
      .setName("Test7_C");
    assertEquals(5, subject.putAll(List.of(widget2, widget3, widget2_widget0, widget3_widget0, widget3_widget1)).size());

    Collection<Widget> result = subject.getAll(Widget.class, Superwidget.class, List.of(superwidget.getId()));
    assertEquals(2, result.size());
  }

}
