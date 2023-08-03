// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.entity;

import java.util.Set;
import io.xj.lib.Superwidget;
import io.xj.lib.Widget;
import io.xj.lib.json.JsonProviderImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Tests for text utilities
 * <p>
 * Created by Charney Kaye on 2020/03/09
 */
public class EntitiesTest extends TestTemplate {
  Widget widget;
  EntityFactory subject;

  @Test
  public void toResourceBelongsTo() {
    assertEquals("entity", Entities.toBelongsTo("Entity"));
    assertEquals("superwidgetWidget", Entities.toBelongsTo("SuperwidgetWidget"));
    assertEquals("widget", Entities.toBelongsTo("widget"));
    assertEquals("widget", Entities.toBelongsTo("widgets"));
    assertEquals("widget", Entities.toBelongsTo(createWidget("Ding")));
    assertEquals("widget", Entities.toBelongsTo(Widget.class));
    assertEquals("superwidget", Entities.toBelongsTo("Superwidget"));
  }

  @Test
  public void toResourceHasMany() {
    assertEquals("entities", Entities.toHasMany("Entity"));
    assertEquals("superwidgetWidgets", Entities.toHasMany("SuperwidgetWidget"));
    assertEquals("widgets", Entities.toHasMany("widget"));
    assertEquals("widgets", Entities.toHasMany(createWidget("Ding")));
    assertEquals("widgets", Entities.toHasMany(Widget.class));
    assertEquals("superwidgets", Entities.toHasMany("Superwidget"));
  }

  @Test
  public void toResourceHasManyFromType() {
    assertEquals("entities", Entities.toHasManyFromType("entities"));
    assertEquals("superwidgetWidgets", Entities.toHasManyFromType("superwidget-widgets"));
    assertEquals("superwidgetWidgets", Entities.toHasManyFromType("superwidget-widget"));
    assertEquals("superwidgets", Entities.toHasManyFromType("superwidget"));
    assertEquals("superwidgets", Entities.toHasManyFromType("Superwidgets"));
  }

  @Test
  public void toResourceType() {
    assertEquals("entities", Entities.toType("Entity"));
    assertEquals("superwidget-widgets", Entities.toType("SuperwidgetWidget"));
    assertEquals("superwidget-widgets", Entities.toType("superwidgetWidget"));
    assertEquals("superwidget-widgets", Entities.toType("superwidgetWidgets"));
    assertEquals("widgets", Entities.toType(createWidget("Ding")));
    assertEquals("widgets", Entities.toType(Widget.class));
    assertEquals("superwidgets", Entities.toType("Superwidget"));
  }

  @Test
  public void toIdAttribute() {
    assertEquals("bilgeWaterId", Entities.toIdAttribute("BilgeWater"));
    assertEquals("widgetId", Entities.toIdAttribute(createWidget("Ding")));
    assertEquals("widgetId", Entities.toIdAttribute(Widget.class));
  }

  @Test
  public void toAttributeName() {
    assertEquals("dancingAbility", Entities.toAttributeName("DancingAbility"));
  }

  @Before
  public void setUp() {
    var jsonProvider = new JsonProviderImpl();
    subject = new EntityFactoryImpl(jsonProvider);
    subject.register(Widget.class)
      .withAttribute("name");
    widget = new Widget()
      .setId(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"))
      .setName("Marv");
  }

  @Test
  public void set() throws EntityException {
    Widget widget5 = new Widget()
      .setId(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"))
      .setName("Marv");

    Entities.set(widget5, "name", "Dave");

    Assert.assertEquals("Dave", widget5.getName());
  }

  @Test
  public void set_localDateTime() throws EntityException {
    Widget widget5 = new Widget()
      .setId(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"))
      .setName("Marv");
    LocalDateTime input = LocalDateTime.parse("2020-03-09T12:34:56.789");

    Entities.set(widget5, "createdAt", input);

    Assert.assertEquals(input, widget5.getCreatedAt());
  }

  @Test
  public void set_localDateTime_fromString() throws EntityException {
    Widget widget5 = new Widget()
      .setId(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"))
      .setName("Marv");
    LocalDateTime input = LocalDateTime.parse("2020-03-09T12:34:56.789");

    Entities.set(widget5, "createdAt", input.toString());

    Assert.assertEquals(input, widget5.getCreatedAt());
  }

  @Test
  public void set_nonexistentAttribute() {
    var e = assertThrows(EntityException.class, () -> Entities.set(widget, "turnip", 4.2));
    assertEquals("Widget has no attribute 'turnip'", e.getMessage());
  }

  @Test
  public void setAllAttributes() throws EntityException {
    subject.setAllAttributes(widget, createWidget("Marv"));

    Assert.assertEquals("Marv", widget.getName());
  }

  @Test
  public void getResourceId() throws EntityException {
    assertEquals(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"), Entities.getId(widget));
  }

  @Test
  public void getResourceId_nullIfEntityHasNoId() throws EntityException {
    assertNull(Entities.getId(new Object()));
  }

  @Test
  public void set_willFailIfSetterAcceptsNoParameters() {
    Widget input = new Widget();

    var e = assertThrows(EntityException.class, () -> Entities.set(input, "willFailBecauseAcceptsNoParameters", true));
    assertEquals("Widget has no attribute 'willFailBecauseAcceptsNoParameters'", e.getMessage());
  }

  @Test
  public void set_willFailIfSetterHasProtectedAccess() {
    Widget input = new Widget();

    var e = assertThrows(EntityException.class, () -> Entities.set(input, "willFailBecauseNonexistent", "testing"));
    assertEquals("Widget has no attribute 'willFailBecauseNonexistent'", e.getMessage());
  }

  @Test
  public void testGetBelongsToId() throws EntityException {
    Widget parent = createWidget("Parent");
    Widget child = createWidget(parent.getId(), "Child");

    assertEquals(parent.getId(), Entities.getBelongsToId(child, "superwidget").orElseThrow());
  }

  @Test
  public void isChild() {
    Superwidget parent = new Superwidget()
      .setId(UUID.randomUUID());

    assertTrue(Entities.isChild(
      new Widget()
        .setSuperwidgetId(parent.getId()),
      parent));
    assertFalse(Entities.isChild(new Widget()
      .setSuperwidgetId(UUID.randomUUID()), parent));
    assertFalse(Entities.isChild(new Widget(), parent));
  }


  @Test
  public void csvIdsOf() {
    assertEquals("4872f737-3526-4532-bb9f-358e3503db7e,333d6284-d7b9-4654-b79c-cafaf9330b6a",
      Entities.csvIdsOf(List.of(
        new Widget().setId(UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e")),
        new Widget().setId(UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a"))
      )));
  }

  @Test
  public void idsOf() {
    assertEquals(Set.of(
        UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e"),
        UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a")
      ),
      Entities.idsOf(List.of(
        new Widget().setId(UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e")),
        new Widget().setId(UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a"))
      )));
  }

  @Test
  public void csvOf() {
    assertEquals("4872f737-3526-4532-bb9f-358e3503db7e, 333d6284-d7b9-4654-b79c-cafaf9330b6a",
      Entities.csvOf(List.of(
        UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e"),
        UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a")
      )));
  }

  @Test
  public void idsFromCSV() {
    assertEquals(
      List.of(
        UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e"),
        UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a")
      ),
      Entities.idsFromCSV("4872f737-3526-4532-bb9f-358e3503db7e,333d6284-d7b9-4654-b79c-cafaf9330b6a"));
  }

  @Test
  public void isParent() {
    Superwidget parent = new Superwidget()
      .setId(UUID.randomUUID());

    assertTrue(Entities.isParent(parent, new Widget().setSuperwidgetId(parent.getId())));
    assertFalse(Entities.isParent(parent, new Widget().setSuperwidgetId(UUID.randomUUID())));
    assertFalse(Entities.isParent(parent, new Widget()));
  }

  @Test
  public void isSame() {
    Widget x = new Widget()
      .setId(UUID.randomUUID());

    assertTrue(Entities.isSame(x, new Widget().setId(x.getId())));
    assertFalse(Entities.isSame(x, new Widget()));
  }

  @Test
  public void flatMapIds() {
    List<UUID> result =
      List.of(
          new Widget().setId(UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e")),
          new Widget().setId(UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a")),
          new Widget().setId(UUID.fromString("e23fb542-b0fc-4773-9848-772f64cbc5a4"))
        ).stream()
        .flatMap(Entities::flatMapIds)
        .collect(Collectors.toList());

    assertEquals(
      List.of(
        UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e"),
        UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a"),
        UUID.fromString("e23fb542-b0fc-4773-9848-772f64cbc5a4")
      ), result);
  }

  @Test
  public void namesOf() {
    Collection<String> result =
      Entities.namesOf(List.of(
        new Widget().setName("Apples"),
        new Widget().setName("Bananas"),
        new Superwidget().setName("Chips")
      ));

    assertEquals(
      List.of(
        "Apples",
        "Bananas",
        "Chips"
      ), result);
  }

}
