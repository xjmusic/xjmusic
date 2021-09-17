package io.xj.lib.entity;

// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import io.xj.lib.Superwidget;
import io.xj.lib.Widget;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class EntityStoreImplTest {
  private EntityStore subject;

  @Before
  public void setUp() {
    var injector = Guice.createInjector(new EntityModule());
    var entityFactory = injector.getInstance(EntityFactory.class);
    subject = injector.getInstance(EntityStore.class);

    // Some topology
    entityFactory.register("Widget")
      .withAttribute("name")
      .belongsTo(Superwidget.class)
      .createdBy(Widget::new);

    // Instantiate the test subject and put the payload
    subject = injector.getInstance(EntityStore.class);
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
         assertEquals(5, subject.putAll(ImmutableList.of(widget2, widget3, widget2_widget0, widget3_widget0, widget3_widget1)).size());

    Collection<Widget> result = subject.getAll(Widget.class, Superwidget.class, ImmutableList.of(superwidget.getId()));
    assertEquals(2, result.size());
  }

}
