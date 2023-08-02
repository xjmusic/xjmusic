// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity;

import java.util.Set;
import io.xj.lib.Widget;
import io.xj.lib.WidgetState;
import io.xj.lib.json.JsonProviderImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class EntityFactoryImplTest {
  EntityFactory subject;

  @Before
  public void setUp() {
    var jsonProvider = new JsonProviderImpl();
    subject = new EntityFactoryImpl(jsonProvider);
  }

  @Test
  public void register_returnsSameSchema_forExistingType() {
    subject.register("Widget").createdBy(Widget::new).belongsTo("OtherThing");

    assertEquals(Set.of("otherThing"), subject.register("widgets").getBelongsTo());
  }

  @Test
  public void register_returnsSameSchema_forExistingTypeClass() {
    subject.register(Widget.class).createdBy(Widget::new).belongsTo("OtherThing");

    assertEquals(Set.of("otherThing"), subject.register(Widget.class).getBelongsTo());
  }

  @Test
  public void register_returnsSameSchema_forExisting_TypeThenClass() {
    subject.register("Widget").createdBy(Widget::new).belongsTo("OtherThing");

    assertEquals(Set.of("otherThing"), subject.register(Widget.class).getBelongsTo());
  }

  @Test
  public void register_returnsSameSchema_forExisting_ClassThenType() {
    subject.register(Widget.class).createdBy(Widget::new).belongsTo("OtherThing");

    assertEquals(Set.of("otherThing"), subject.register("Widget").getBelongsTo());
  }

  @Test
  public void register_basicTypeCreator() throws EntityException {
    subject.register("Widget").createdBy(Widget::new);

    assertEquals(Widget.class, subject.getInstance("widget").getClass());
  }

  @Test
  public void register_withBelongsTo() throws EntityException {
    subject.register("Widget").belongsTo("FictionalEntity");

    assertEquals(Set.of("fictionalEntity"), subject.getBelongsTo("widgets"));
  }

  @Test
  public void register_withAttributesAndBelongsTo() throws EntityException {
    subject.register("Widget");
    subject.register("FictionalEntity").withAttribute("name").belongsTo("superwidget").createdBy(Widget::new);

    assertEquals(Set.of("superwidget"),
      subject.getBelongsTo("fictional-entity"));

    assertEquals(Set.of("name"),
      subject.getAttributes("fictional-entity"));
  }

  @Test
  public void register_withAttributes() throws EntityException {
    subject.register("FictionalEntity").withAttribute("name").createdBy(Widget::new);

    assertEquals(Set.of("name"), subject.getAttributes("fictional-entity"));
  }

  @Test
  public void getBelongsToType() throws EntityException {
    subject.register("OtherEntity");
    subject.register("FakeEntity").belongsTo("otherEntity").createdBy(Widget::new);

    assertEquals(Set.of("otherEntity"), subject.getBelongsTo("fake-entity"));
  }

  @Test
  public void getBelongsToType_emptyBelongsTo() throws EntityException {
    subject.register("OtherEntity");

    assertTrue(subject.getBelongsTo("other-entity").isEmpty());
  }

  @Test
  public void getBelongsToType_exceptionIfDoesNotExist() {
    var e = assertThrows(EntityException.class, () ->
      subject.getBelongsTo("other-entity"));

    assertEquals("Cannot get belongs-to type unknown type: other-entities", e.getMessage());
  }

  @Test
  public void getAttributes() throws EntityException {
    subject.register("FalseEntity").withAttribute("yarn").createdBy(Widget::new);

    assertEquals(Set.of("yarn"), subject.getAttributes("false-entity"));
  }


  @Test
  public void testClone() throws EntityException {
    subject.register("Widget")
      .withAttributes("name")
      .belongsTo("superwidget")
      .createdBy(Widget::new);
    Widget from = new Widget().setName("Flight")
      .setSuperwidgetId(UUID.randomUUID());

    Widget result = subject.clone(from);

    Assert.assertEquals("Flight", result.getName());
    Assert.assertEquals(from.getSuperwidgetId(), result.getSuperwidgetId());
    Assert.assertEquals(from.getId(), result.getId());
    assertNotSame(result, from);
  }

  @Test
  public void testClone_withState() throws EntityException {
    subject.register("Widget")
      .withAttributes("name", "state")
      .belongsTo("superwidget")
      .createdBy(Widget::new);
    Widget from = new Widget()
      .setName("Flight")
      .setState(WidgetState.Published);

    Widget result = subject.clone(from);

    Assert.assertEquals("Flight", result.getName());
    Assert.assertEquals(from.getState(), result.getState());
    Assert.assertEquals(from.getId(), result.getId());
    assertNotSame(result, from);
  }


  /**
   * This should ostensibly be a test inside the Entity superwidget-- and it is, except for this bug that
   * at the time of this writing, we couldn't isolate to that superwidget, and are therefore reproducing it here.
   *
   * @throws EntityException on failure
   */
  @Test
  public void internal_entityFactoryClonesWidgetTypeOK() throws EntityException {
    // Some topology
    subject.register("Widget");
    subject.register("Widget")
      .withAttributes("name", "state")
      .belongsTo("superwidget")
      .createdBy(Widget::new);
    Widget widget = new Widget()
      .setId(UUID.fromString("ac5eba0a-f725-4831-9ff2-a8d92a73a09d"))
      .setState(WidgetState.Published);

    Widget result = subject.clone(widget);

    Assert.assertEquals(WidgetState.Published, result.getState());
  }

  @Test
  public void testClone_withNullBelongsToId() throws EntityException {
    subject.register("Widget").withAttribute("name").belongsTo("superwidget").createdBy(Widget::new);
    Widget from = new Widget()
      .setName("Flight");

    Widget result = subject.clone(from);

    Assert.assertEquals("Flight", result.getName());
    Assert.assertEquals(from.getId(), result.getId());
    assertNotSame(result, from);
  }

  @Test
  public void testCloneAll() throws EntityException {
    subject.register("Widget")
      .withAttribute("name")
      .belongsTo("superwidget")
      .createdBy(Widget::new);
    Widget fromA = new Widget()
      .setName("Air")
      .setSuperwidgetId(UUID.randomUUID());
    Widget fromB = new Widget()
      .setName("Ground")
      .setSuperwidgetId(UUID.randomUUID());

    Collection<Widget> result = subject.cloneAll(List.of(fromA, fromB));

    assertEquals(2, result.size());
    Iterator<Widget> resultIt = result.iterator();
    //
    Widget resultA = resultIt.next();
    Assert.assertEquals("Air", resultA.getName());
    Assert.assertEquals(fromA.getSuperwidgetId(), resultA.getSuperwidgetId());
    Assert.assertEquals(fromA.getId(), resultA.getId());
    assertNotSame(resultA, fromA);
    //
    Widget resultB = resultIt.next();
    Assert.assertEquals("Ground", resultB.getName());
    Assert.assertEquals(fromB.getSuperwidgetId(), resultB.getSuperwidgetId());
    Assert.assertEquals(fromB.getId(), resultB.getId());
    assertNotSame(resultB, fromB);
  }

}
