package io.xj.model.entity;

import io.xj.model.util.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityUtilsTest {

  private Widget child;
  private Superwidget parent;

  @BeforeEach
  public void setUp() {
    child = new Widget()
      .setId(UUID.fromString("654ab381-cab2-4a1e-b64f-7a87a5631d58"))
      .setSuperwidgetId(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"))
      .setUpdatedAt(1707621963556L)
      .setName("Marv");
    parent = new Superwidget()
      .setId(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"))
      .setName("Marv");
  }

  @Test
  void get() throws EntityException {
    assertEquals("Marv", EntityUtils.get(child, "name").orElseThrow());
  }

  @Test
  void set() throws EntityException {
    EntityUtils.set(child, "name", "Marv2");
    assertEquals("Marv2", child.getName());
  }

  @Test
  void getId() throws EntityException {
    assertEquals(UUID.fromString("654ab381-cab2-4a1e-b64f-7a87a5631d58"), EntityUtils.getId(child));
  }

  @Test
  void getIds() {
    var ids = EntityUtils.idsOf(Set.of(child, parent));

    assertEquals(2, ids.size());
    assertTrue(ids.contains(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6")));
    assertTrue(ids.contains(UUID.fromString("654ab381-cab2-4a1e-b64f-7a87a5631d58")));
  }

  @Test
  void getUpdatedAt() throws EntityException {
    assertEquals(1707621963556L, EntityUtils.getUpdatedAt(child));
  }

  @Test
  void setId() throws EntityException {
    EntityUtils.setId(child, UUID.fromString("212b798f-df07-4a4a-8c10-77d5793af239"));
    assertEquals(UUID.fromString("212b798f-df07-4a4a-8c10-77d5793af239"), child.getId());
  }

  @Test
  void getType() {
    assertEquals("widgets", EntityUtils.getType(child));
  }

  @Test
  void enumValue() {
    enum TestEnum {
      Apples, Bananas, Carrots
    }

    assertEquals(TestEnum.Apples, EntityUtils.enumValue(TestEnum.class, "Apples"));
  }

  @Test
  void getSimpleName() {
    assertEquals("Widget", EntityUtils.getSimpleName(child));
  }

  @Test
  void toResourceType() {
    assertEquals("widgets", EntityUtils.toResourceType(Widget.class));
  }

  @Test
  void toIdAttribute() {
    assertEquals("widgetId", EntityUtils.toIdAttribute(Widget.class));
  }

  @Test
  void toBelongsTo() {
    assertEquals("widget", EntityUtils.toBelongsTo(Widget.class));
  }

  @Test
  void toBelongsToFromType() {
    assertEquals("superwidget", EntityUtils.toBelongsToFromType("Superwidget"));
  }

  @Test
  void toHasMany() {
    assertEquals("widgets", EntityUtils.toHasMany(Widget.class));
  }

  @Test
  void toHasManyFromType() {
    assertEquals("widgets", EntityUtils.toHasManyFromType("Widget"));
  }

  @Test
  void toType() {
    assertEquals("widgets", EntityUtils.toType("widgets"));
  }

  @Test
  void toAttributeName() {
    assertEquals("widget", EntityUtils.toAttributeName("Widget"));
  }

  @Test
  void toSetterName() {
    assertEquals("setWidget", EntityUtils.toSetterName("Widget"));
  }

  @Test
  void getBelongsToId() throws EntityException {
    assertEquals(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"), EntityUtils.getBelongsToId(child, "Superwidget").orElseThrow());
  }

  @Test
  void toGetterName() {
    assertEquals("getWidget", EntityUtils.toGetterName("Widget"));
  }

  @Test
  void add() {
    var test = new Widget().setId(UUID.fromString("c8b5529d-af44-4863-bf67-ff61c2c3253f"));
    var set = new HashSet<>();

    EntityUtils.add(set, test);

    assertEquals(1, set.size());
  }

  @Test
  void idsOf() {
    var ids = EntityUtils.idsOf(Set.of(child, parent));
    assertEquals(2, ids.size());
    assertTrue(ids.contains(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6")));
    assertTrue(ids.contains(UUID.fromString("654ab381-cab2-4a1e-b64f-7a87a5631d58")));
  }

  @Test
  void flatMapIds() {
    var ids = Set.of(child, parent).stream().flatMap(EntityUtils::flatMapIds).toList();
    assertEquals(2, ids.size());
    assertTrue(ids.contains(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6")));
    assertTrue(ids.contains(UUID.fromString("654ab381-cab2-4a1e-b64f-7a87a5631d58")));
  }

  @Test
  void idsFromCSV() {
    var ids = EntityUtils.idsFromCSV("879802e8-5856-4b1f-8c7f-09fd7f4bcde6,654ab381-cab2-4a1e-b64f-7a87a5631d58");
    assertEquals(2, ids.size());
    assertTrue(ids.contains(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6")));
    assertTrue(ids.contains(UUID.fromString("654ab381-cab2-4a1e-b64f-7a87a5631d58")));
  }

  @Test
  void isChild() {
    assertTrue(EntityUtils.isChild(child, parent));
  }

  @Test
  void isParent() {
    assertTrue(EntityUtils.isParent(parent, child));
  }

  @Test
  void isSame() {
    assertTrue(EntityUtils.isSame(child, child));
    assertFalse(EntityUtils.isSame(child, parent));
  }

  @Test
  void namesOf() {
    var names = EntityUtils.namesOf(Set.of(child, parent));
    assertEquals(2, names.size());
    assertTrue(names.contains("Marv"));
    assertTrue(names.contains("Marv"));
  }

  @Test
  void isType() {
    assertTrue(EntityUtils.isType(child, Widget.class));
    assertTrue(EntityUtils.isType(parent, Superwidget.class));
  }

  @Test
  void csvOf() {
    var csv = EntityUtils.csvOf(Set.of(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"), UUID.fromString("654ab381-cab2-4a1e-b64f-7a87a5631d58")));

    assertTrue(csv.contains("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"));
    assertTrue(csv.contains("654ab381-cab2-4a1e-b64f-7a87a5631d58"));
  }
}
