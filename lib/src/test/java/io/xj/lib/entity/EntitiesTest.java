// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.entity;

import io.xj.lib.Superwidget;
import io.xj.lib.Widget;
import io.xj.lib.json.JsonProviderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 Tests for text utilities
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class EntitiesTest extends TestTemplate {
  Widget widget;
  EntityFactory subject;

  @Test
  public void toResourceBelongsTo() {
    assertEquals("entity", EntityUtils.toBelongsTo("Entity"));
    assertEquals("superwidgetWidget", EntityUtils.toBelongsTo("SuperwidgetWidget"));
    assertEquals("widget", EntityUtils.toBelongsTo("widget"));
    assertEquals("widget", EntityUtils.toBelongsTo("widgets"));
    assertEquals("widget", EntityUtils.toBelongsTo(createWidget("Ding")));
    assertEquals("widget", EntityUtils.toBelongsTo(Widget.class));
    assertEquals("superwidget", EntityUtils.toBelongsTo("Superwidget"));
  }

  @Test
  public void toResourceHasMany() {
    assertEquals("entities", EntityUtils.toHasMany("Entity"));
    assertEquals("superwidgetWidgets", EntityUtils.toHasMany("SuperwidgetWidget"));
    assertEquals("widgets", EntityUtils.toHasMany("widget"));
    assertEquals("widgets", EntityUtils.toHasMany(createWidget("Ding")));
    assertEquals("widgets", EntityUtils.toHasMany(Widget.class));
    assertEquals("superwidgets", EntityUtils.toHasMany("Superwidget"));
  }

  @Test
  public void toResourceHasManyFromType() {
    assertEquals("entities", EntityUtils.toHasManyFromType("entities"));
    assertEquals("superwidgetWidgets", EntityUtils.toHasManyFromType("superwidget-widgets"));
    assertEquals("superwidgetWidgets", EntityUtils.toHasManyFromType("superwidget-widget"));
    assertEquals("superwidgets", EntityUtils.toHasManyFromType("superwidget"));
    assertEquals("superwidgets", EntityUtils.toHasManyFromType("Superwidgets"));
  }

  @Test
  public void toResourceType() {
    assertEquals("entities", EntityUtils.toType("Entity"));
    assertEquals("superwidget-widgets", EntityUtils.toType("SuperwidgetWidget"));
    assertEquals("superwidget-widgets", EntityUtils.toType("superwidgetWidget"));
    assertEquals("superwidget-widgets", EntityUtils.toType("superwidgetWidgets"));
    assertEquals("widgets", EntityUtils.toType(createWidget("Ding")));
    assertEquals("widgets", EntityUtils.toType(Widget.class));
    assertEquals("superwidgets", EntityUtils.toType("Superwidget"));
  }

  @Test
  public void toIdAttribute() {
    assertEquals("bilgeWaterId", EntityUtils.toIdAttribute("BilgeWater"));
    assertEquals("widgetId", EntityUtils.toIdAttribute(createWidget("Ding")));
    assertEquals("widgetId", EntityUtils.toIdAttribute(Widget.class));
  }

  @Test
  public void toAttributeName() {
    assertEquals("dancingAbility", EntityUtils.toAttributeName("DancingAbility"));
  }

  @BeforeEach
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

    EntityUtils.set(widget5, "name", "Dave");

    assertEquals("Dave", widget5.getName());
  }

  @Test
  public void set_localDateTime() throws EntityException {
    Widget widget5 = new Widget()
      .setId(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"))
      .setName("Marv");
    LocalDateTime input = LocalDateTime.parse("2020-03-09T12:34:56.789");

    EntityUtils.set(widget5, "createdAt", input);

    assertEquals(input, widget5.getCreatedAt());
  }

  @Test
  public void set_localDateTime_fromString() throws EntityException {
    Widget widget5 = new Widget()
      .setId(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"))
      .setName("Marv");
    LocalDateTime input = LocalDateTime.parse("2020-03-09T12:34:56.789");

    EntityUtils.set(widget5, "createdAt", input.toString());

    assertEquals(input, widget5.getCreatedAt());
  }

  @Test
  public void set_nonexistentAttribute() {
    var e = assertThrows(EntityException.class, () -> EntityUtils.set(widget, "turnip", 4.2));
    assertEquals("Widget has no attribute 'turnip'", e.getMessage());
  }

  @Test
  public void setAllAttributes() throws EntityException {
    subject.setAllAttributes(widget, createWidget("Marv"));

    assertEquals("Marv", widget.getName());
  }

  @Test
  public void getResourceId() throws EntityException {
    assertEquals(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"), EntityUtils.getId(widget));
  }

  @Test
  public void getResourceId_nullIfEntityHasNoId() throws EntityException {
    assertNull(EntityUtils.getId(new Object()));
  }

  @Test
  public void set_willFailIfSetterAcceptsNoParameters() {
    Widget input = new Widget();

    var e = assertThrows(EntityException.class, () -> EntityUtils.set(input, "willFailBecauseAcceptsNoParameters", true));
    assertEquals("Widget has no attribute 'willFailBecauseAcceptsNoParameters'", e.getMessage());
  }

  @Test
  public void set_willFailIfSetterHasProtectedAccess() {
    Widget input = new Widget();

    var e = assertThrows(EntityException.class, () -> EntityUtils.set(input, "willFailBecauseNonexistent", "testing"));
    assertEquals("Widget has no attribute 'willFailBecauseNonexistent'", e.getMessage());
  }

  @Test
  public void testGetBelongsToId() throws EntityException {
    Widget parent = createWidget("Parent");
    Widget child = createWidget(parent.getId(), "Child");

    assertEquals(parent.getId(), EntityUtils.getBelongsToId(child, "superwidget").orElseThrow());
  }

  @Test
  public void isChild() {
    Superwidget parent = new Superwidget()
      .setId(UUID.randomUUID());

    assertTrue(EntityUtils.isChild(
      new Widget()
        .setSuperwidgetId(parent.getId()),
      parent));
    assertFalse(EntityUtils.isChild(new Widget()
      .setSuperwidgetId(UUID.randomUUID()), parent));
    assertFalse(EntityUtils.isChild(new Widget(), parent));
  }


  @Test
  public void csvIdsOf() {
    assertEquals("4872f737-3526-4532-bb9f-358e3503db7e,333d6284-d7b9-4654-b79c-cafaf9330b6a",
      EntityUtils.csvIdsOf(List.of(
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
      EntityUtils.idsOf(List.of(
        new Widget().setId(UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e")),
        new Widget().setId(UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a"))
      )));
  }

  @Test
  public void csvOf() {
    assertEquals("4872f737-3526-4532-bb9f-358e3503db7e, 333d6284-d7b9-4654-b79c-cafaf9330b6a",
      EntityUtils.csvOf(List.of(
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
      EntityUtils.idsFromCSV("4872f737-3526-4532-bb9f-358e3503db7e,333d6284-d7b9-4654-b79c-cafaf9330b6a"));
  }

  @Test
  public void isParent() {
    Superwidget parent = new Superwidget()
      .setId(UUID.randomUUID());

    assertTrue(EntityUtils.isParent(parent, new Widget().setSuperwidgetId(parent.getId())));
    assertFalse(EntityUtils.isParent(parent, new Widget().setSuperwidgetId(UUID.randomUUID())));
    assertFalse(EntityUtils.isParent(parent, new Widget()));
  }

  @Test
  public void isSame() {
    Widget x = new Widget()
      .setId(UUID.randomUUID());

    assertTrue(EntityUtils.isSame(x, new Widget().setId(x.getId())));
    assertFalse(EntityUtils.isSame(x, new Widget()));
  }

  @Test
  public void flatMapIds() {
    List<UUID> result =
      List.of(
          new Widget().setId(UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e")),
          new Widget().setId(UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a")),
          new Widget().setId(UUID.fromString("e23fb542-b0fc-4773-9848-772f64cbc5a4"))
        ).stream()
        .flatMap(EntityUtils::flatMapIds)
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
      EntityUtils.namesOf(List.of(
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
