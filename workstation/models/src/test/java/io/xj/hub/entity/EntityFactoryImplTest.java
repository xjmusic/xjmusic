// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.entity;

import io.xj.hub.ContentTest;
import io.xj.hub.HubTopology;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.util.Widget;
import io.xj.hub.util.WidgetState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntityFactoryImplTest extends ContentTest {
  EntityFactory subject;
  private Widget widget;

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
  public void toResourceBelongsTo() {
    assertEquals("entity", EntityUtils.toBelongsTo("Entity"));
    assertEquals("superwidgetWidget", EntityUtils.toBelongsTo("SuperwidgetWidget"));
    assertEquals("widget", EntityUtils.toBelongsTo("widget"));
    assertEquals("widget", EntityUtils.toBelongsTo("widgets"));
    assertEquals("widget", EntityUtils.toBelongsTo(TestTemplate.createWidget("Ding")));
    assertEquals("widget", EntityUtils.toBelongsTo(Widget.class));
    assertEquals("superwidget", EntityUtils.toBelongsTo("Superwidget"));
  }

  @Test
  public void toResourceHasMany() {
    assertEquals("entities", EntityUtils.toHasMany("Entity"));
    assertEquals("superwidgetWidgets", EntityUtils.toHasMany("SuperwidgetWidget"));
    assertEquals("widgets", EntityUtils.toHasMany("widget"));
    assertEquals("widgets", EntityUtils.toHasMany(TestTemplate.createWidget("Ding")));
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
    assertEquals("widgets", EntityUtils.toType(TestTemplate.createWidget("Ding")));
    assertEquals("widgets", EntityUtils.toType(Widget.class));
    assertEquals("superwidgets", EntityUtils.toType("Superwidget"));
  }

  @Test
  public void toIdAttribute() {
    assertEquals("bilgeWaterId", EntityUtils.toIdAttribute("BilgeWater"));
    assertEquals("widgetId", EntityUtils.toIdAttribute(TestTemplate.createWidget("Ding")));
    assertEquals("widgetId", EntityUtils.toIdAttribute(Widget.class));
  }

  @Test
  public void toAttributeName() {
    assertEquals("dancingAbility", EntityUtils.toAttributeName("DancingAbility"));
  }

  @Test
  public void set() throws Exception {
    Widget widget5 = new Widget()
      .setId(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"))
      .setName("Marv");

    EntityUtils.set(widget5, "name", "Dave");

    assertEquals("Dave", widget5.getName());
  }

  @Test
  public void set_localDateTime() throws Exception {
    Widget widget5 = new Widget()
      .setId(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"))
      .setName("Marv");
    LocalDateTime input = LocalDateTime.parse("2020-03-09T12:34:56.789");

    EntityUtils.set(widget5, "createdAt", input);

    assertEquals(input, widget5.getCreatedAt());
  }

  @Test
  public void set_localDateTime_fromString() throws Exception {
    Widget widget5 = new Widget()
      .setId(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"))
      .setName("Marv");
    LocalDateTime input = LocalDateTime.parse("2020-03-09T12:34:56.789");

    EntityUtils.set(widget5, "createdAt", input.toString());

    assertEquals(input, widget5.getCreatedAt());
  }

  @Test
  public void set_nonexistentAttribute() {
    var e = assertThrows(Exception.class, () -> EntityUtils.set(widget, "turnip", 4.2));
    assertEquals("Widget has no attribute 'turnip'", e.getMessage());
  }

  @Test
  public void setAllAttributes() throws Exception {
    subject.setAllAttributes(widget, TestTemplate.createWidget("Marv"));

    assertEquals("Marv", widget.getName());
  }

  @Test
  public void getResourceId() throws Exception {
    assertEquals(UUID.fromString("879802e8-5856-4b1f-8c7f-09fd7f4bcde6"), EntityUtils.getId(widget));
  }

  @Test
  public void getResourceId_nullIfEntityHasNoId() throws Exception {
    assertNull(EntityUtils.getId(new Object()));
  }

  @Test
  public void set_willFailIfSetterAcceptsNoParameters() {
    Widget input = new Widget();

    var e = assertThrows(Exception.class, () -> EntityUtils.set(input, "willFailBecauseAcceptsNoParameters", true));
    assertEquals("Widget has no attribute 'willFailBecauseAcceptsNoParameters'", e.getMessage());
  }

  @Test
  public void set_willFailIfSetterHasProtectedAccess() {
    Widget input = new Widget();

    var e = assertThrows(Exception.class, () -> EntityUtils.set(input, "willFailBecauseNonexistent", "testing"));
    assertEquals("Widget has no attribute 'willFailBecauseNonexistent'", e.getMessage());
  }

  @Test
  public void testGetBelongsToId() throws Exception {
    Widget parent = TestTemplate.createWidget("Parent");
    Widget child = TestTemplate.createWidget(parent.getId(), "Child");

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
      Stream.of(
          new Widget().setId(UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e")),
          new Widget().setId(UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a")),
          new Widget().setId(UUID.fromString("e23fb542-b0fc-4773-9848-772f64cbc5a4"))
        )
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
  public void testDuplicate() throws EntityException {
    subject.register("Widget")
      .withAttributes("name")
      .belongsTo("superwidget")
      .createdBy(Widget::new);
    Widget from = new Widget().setName("Flight")
      .setId(UUID.randomUUID())
      .setSuperwidgetId(UUID.randomUUID());

    Widget result = subject.duplicate(from);

    assertEquals("Flight", result.getName());
    assertEquals(from.getSuperwidgetId(), result.getSuperwidgetId());
    assertNotEquals(from.getId(), result.getId());
    assertNotSame(result, from);
  }

  @Test
  public void testDuplicate_withState() throws EntityException {
    subject.register("Widget")
      .withAttributes("name", "state")
      .belongsTo("superwidget")
      .createdBy(Widget::new);
    Widget from = new Widget()
      .setId(UUID.randomUUID())
      .setName("Flight")
      .setState(WidgetState.Published);

    Widget result = subject.duplicate(from);

    assertEquals("Flight", result.getName());
    assertEquals(from.getState(), result.getState());
    assertNotEquals(from.getId(), result.getId());
    assertNotSame(result, from);
  }


  /**
   This should ostensibly be a test inside the Entity superwidget-- and it is, except for this bug that
   at the time of this writing, we couldn't isolate to that superwidget, and are therefore reproducing it here.

   @throws EntityException on failure
   */
  @Test
  public void internal_entityFactoryDuplicatesWidgetTypeOK() throws EntityException {
    // Some topology
    subject.register("Widget");
    subject.register("Widget")
      .withAttributes("name", "state")
      .belongsTo("superwidget")
      .createdBy(Widget::new);
    Widget widget = new Widget()
      .setId(UUID.fromString("ac5eba0a-f725-4831-9ff2-a8d92a73a09d"))
      .setState(WidgetState.Published);

    Widget result = subject.duplicate(widget);

    assertEquals(WidgetState.Published, result.getState());
  }

  @Test
  public void testDuplicate_withNullBelongsToId() throws EntityException {
    subject.register("Widget").withAttribute("name").belongsTo("superwidget").createdBy(Widget::new);
    Widget from = new Widget()
      .setId(UUID.randomUUID())
      .setName("Flight");

    Widget result = subject.duplicate(from);

    assertEquals("Flight", result.getName());
    assertNotEquals(from.getId(), result.getId());
    assertNotSame(result, from);
  }

  @Test
  public void testDuplicateAll() throws EntityException {
    subject.register("Widget")
      .withAttribute("name")
      .belongsTo("superwidget")
      .createdBy(Widget::new);
    Widget fromA = new Widget()
      .setId(UUID.randomUUID())
      .setName("Air")
      .setSuperwidgetId(UUID.randomUUID());
    Widget fromB = new Widget()
      .setId(UUID.randomUUID())
      .setName("Ground")
      .setSuperwidgetId(UUID.randomUUID());

    var result = subject.duplicateAll(Set.of(fromA, fromB));

    assertEquals(2, result.size());
    //
    assertTrue(result.containsKey(fromA.getId()));
    Widget resultA = result.get(fromA.getId());
    assertEquals("Air", resultA.getName());
    assertEquals(fromA.getSuperwidgetId(), resultA.getSuperwidgetId());
    assertNotEquals(fromA.getId(), resultA.getId());
    assertNotSame(resultA, fromA);
    //
    assertTrue(result.containsKey(fromB.getId()));
    Widget resultB = result.get(fromB.getId());
    assertEquals("Ground", resultB.getName());
    assertEquals(fromB.getSuperwidgetId(), resultB.getSuperwidgetId());
    assertNotEquals(fromB.getId(), resultB.getId());
    assertNotSame(resultB, fromB);
  }

  @Test
  public void testDuplicateAll_toNewRelationships() throws EntityException {
    subject.register("Widget")
      .withAttribute("name")
      .belongsTo("superwidget")
      .createdBy(Widget::new);
    Superwidget fromSuper = new Superwidget()
      .setId(UUID.randomUUID())
      .setName("Super");
    Superwidget toSuper = new Superwidget()
      .setId(UUID.randomUUID())
      .setName("Super");
    Widget fromA = new Widget()
      .setId(UUID.randomUUID())
      .setName("Air")
      .setSuperwidgetId(fromSuper.getId());
    Widget fromB = new Widget()
      .setId(UUID.randomUUID())
      .setName("Ground")
      .setSuperwidgetId(fromSuper.getId());

    var result = subject.duplicateAll(Set.of(fromA, fromB), Set.of(toSuper));

    assertEquals(2, result.size());
    //
    assertTrue(result.containsKey(fromA.getId()));
    Widget resultA = result.get(fromA.getId());
    assertEquals("Air", resultA.getName());
    assertEquals(toSuper.getId(), resultA.getSuperwidgetId());
    assertNotEquals(fromA.getId(), resultA.getId());
    assertEquals(toSuper.getId(), resultA.getSuperwidgetId());
    assertNotSame(resultA, fromA);
    //
    assertTrue(result.containsKey(fromB.getId()));
    Widget resultB = result.get(fromB.getId());
    assertEquals("Ground", resultB.getName());
    assertEquals(toSuper.getId(), resultB.getSuperwidgetId());
    assertNotEquals(fromB.getId(), resultB.getId());
    assertEquals(toSuper.getId(), resultB.getSuperwidgetId());
    assertNotSame(resultB, fromB);
  }

  @Test
  public void testCopy_withNullBelongsToId() throws EntityException {
    subject.register("Widget").withAttribute("name").belongsTo("superwidget").createdBy(Widget::new);
    Widget from = new Widget()
      .setId(UUID.randomUUID())
      .setName("Flight");

    Widget result = subject.copy(from);

    assertEquals("Flight", result.getName());
    assertEquals(from.getId(), result.getId());
    assertNotSame(result, from);
  }

  @Test
  public void testCopyAll() throws EntityException {
    subject.register("Widget")
      .withAttribute("name")
      .belongsTo("superwidget")
      .createdBy(Widget::new);
    Widget fromA = new Widget()
      .setId(UUID.randomUUID())
      .setName("Air")
      .setSuperwidgetId(UUID.randomUUID());
    Widget fromB = new Widget()
      .setId(UUID.randomUUID())
      .setName("Ground")
      .setSuperwidgetId(UUID.randomUUID());

    var result = subject.copyAll(Set.of(fromA, fromB));

    assertEquals(2, result.size());
    //
    Widget resultA = result.stream().filter(w -> w.getId().equals(fromA.getId())).findFirst().orElseThrow();
    assertEquals("Air", resultA.getName());
    assertEquals(fromA.getSuperwidgetId(), resultA.getSuperwidgetId());
    assertEquals(fromA.getId(), resultA.getId());
    assertNotSame(resultA, fromA);
    //
    Widget resultB = result.stream().filter(w -> w.getId().equals(fromB.getId())).findFirst().orElseThrow();
    assertEquals("Ground", resultB.getName());
    assertEquals(fromB.getSuperwidgetId(), resultB.getSuperwidgetId());
    assertEquals(fromB.getId(), resultB.getId());
    assertNotSame(resultB, fromB);
  }

  @Test
  void forTemplate_boundToLibrary() {
    HubTopology.buildHubApiTopology(subject);
    var content = buildHubContent();
    var result = subject.forTemplate(content, template1);

    assertEquals(31, result.size());
  }

  @Test
  void forTemplate_boundToProgram() {
    HubTopology.buildHubApiTopology(subject);
    var content = buildHubContent();
    content.put(buildTemplateBinding(template2, program1));
    var result = subject.forTemplate(content, template2);

    assertEquals(14, result.size());
  }

  @Test
  void forTemplate_boundToInstrument() {
    HubTopology.buildHubApiTopology(subject);
    var content = buildHubContent();
    content.put(buildTemplateBinding(template2, instrument1));
    var result = subject.forTemplate(content, template2);

    assertEquals(5, result.size());
  }

  @Test
  void forTemplate_duplicatesOriginalObjects() {
    HubTopology.buildHubApiTopology(subject);
    var content = buildHubContent();
    var copy = subject.forTemplate(content, template1);

    copy.getInstrument(instrument1.getId()).orElseThrow().setName("different");
    copy.getInstrumentMeme(instrument1_meme.getId()).orElseThrow().setName("different");
    copy.getInstrumentAudio(instrument1_audio.getId()).orElseThrow().setName("different");
    copy.getProgram(program1.getId()).orElseThrow().setName("different");
    copy.getProgramMeme(program1_meme.getId()).orElseThrow().setName("different");
    copy.getProgramVoice(program1_voice.getId()).orElseThrow().setName("different");
    copy.getProgramSequence(program1_sequence.getId()).orElseThrow().setName("different");
    copy.getProgramSequenceChord(program1_sequence_chord0.getId()).orElseThrow().setName("different");
    copy.getProgramSequenceChordVoicing(program1_sequence_chord0_voicing0.getId()).orElseThrow().setNotes("different");

    assertEquals("808 Drums", content.getInstrument(instrument1.getId()).orElseThrow().getName());
    assertEquals("Ants", content.getInstrumentMeme(instrument1_meme.getId()).orElseThrow().getName());
    assertEquals("Chords Cm to D", content.getInstrumentAudio(instrument1_audio.getId()).orElseThrow().getName());
    assertEquals("leaves", content.getProgram(program1.getId()).orElseThrow().getName());
    assertEquals("Ants", content.getProgramMeme(program1_meme.getId()).orElseThrow().getName());
    assertEquals("Birds", content.getProgramVoice(program1_voice.getId()).orElseThrow().getName());
    assertEquals("decay", content.getProgramSequence(program1_sequence.getId()).orElseThrow().getName());
    assertEquals("G minor", content.getProgramSequenceChord(program1_sequence_chord0.getId()).orElseThrow().getName());
    assertEquals("G", content.getProgramSequenceChordVoicing(program1_sequence_chord0_voicing0.getId()).orElseThrow().getNotes());
  }
}
