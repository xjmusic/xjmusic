// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.transport.GsonProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

import static io.xj.core.Assert.assertExactChords;
import static io.xj.core.Assert.assertExactMemes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentImplTest {
  private final SegmentTestHelper helper = new SegmentTestHelper();
  @Rule
  public ExpectedException failure = ExpectedException.none();
  GsonProvider gsonProvider;
  Injector injector;
  SegmentFactory segmentFactory;
  Segment segment;

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(new CoreModule());
    gsonProvider = injector.getInstance(GsonProvider.class);
    segmentFactory = injector.getInstance(SegmentFactory.class);
    segment = segmentFactory.newSegment(BigInteger.valueOf(4));
  }

  @Test
  public void validate() throws Exception {
    segment
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_setAsTimestamps() throws Exception {
    segment
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAtInstant(Instant.parse("2014-08-12T12:17:02.527142Z"))
      .setEndAtInstant(Instant.parse("2014-09-12T12:17:34.262679Z"))
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_withMinimalAttributes() throws Exception {
    segment
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setOffset(BigInteger.valueOf(473L))
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_failsWithoutChainID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Chain ID is required");

    segment
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_failsWithoutBeginAt() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Begin-at is required");

    segment
      .setChainId(BigInteger.valueOf(180923L))
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_failsWithoutOffset() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Offset is required");

    segment
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_failsWithoutState() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("State is required");

    segment
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .validate();
  }

  @Test
  public void validate_failsWithInvalidState() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("'pensive' is not a valid state");

    segment
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("pensive")
      .validate();
  }

  @Test
  public void validate_okWithSetEnumState() throws Exception {
    segment
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0);

    segment.setStateEnum(SegmentState.Crafting);
    segment.validate();
  }

  @Test
  public void isInitial_trueAtOffsetZero() {
    assertTrue(segment
      .setOffset(BigInteger.valueOf(0L))
      .isInitial());
  }

  @Test
  public void isInitial_falseAtOffsetOne() {
    assertFalse(segment
      .setOffset(BigInteger.valueOf(1L))
      .isInitial());
  }

  @Test
  public void isInitial_falseAtOffsetHigh() {
    assertFalse(segment
      .setOffset(BigInteger.valueOf(901L))
      .isInitial());
  }

  @Test
  public void getPreviousOffset() throws Exception {
    assertEquals(BigInteger.valueOf(234L),
      segment.setOffset(BigInteger.valueOf(235L)).getPreviousOffset());
  }

  @Test
  public void getPreviousOffset_throwsExceptionForInitialSegment() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Cannot get previous id of initial Segment");

    segment.setOffset(BigInteger.valueOf(0L)).getPreviousOffset();
  }

  @Test
  public void choice_getChoiceOfType() throws Exception {
    // segment
    segment
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted");
    // memes
    segment.add(helper.getMeme("Hindsight"));
    segment.add(helper.getMeme("Chunky"));
    segment.add(helper.getMeme("Regret"));
    segment.add(helper.getMeme("Tangy"));
    // choices
    segment.add(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(130))
      .setTypeEnum(SequenceType.Macro)
      .setTranspose(4));
    segment.add(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(415150))
      .setTypeEnum(SequenceType.Main)
      .setTranspose(-2));
    segment.add(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequenceId(BigInteger.valueOf(35))
      .setTypeEnum(SequenceType.Rhythm)
      .setTranspose(-4));
    // chords
    segment.add(helper.getChord(0.0, "F minor"));
    segment.add(helper.getChord(8.0, "Gb minor"));

    assertEquals(BigInteger.valueOf(130), segment.getChoiceOfType(SequenceType.Macro).getSequencePatternId());
    assertEquals(BigInteger.valueOf(415150), segment.getChoiceOfType(SequenceType.Main).getSequencePatternId());
    assertEquals(BigInteger.valueOf(35), segment.getChoiceOfType(SequenceType.Rhythm).getSequenceId());
  }

  @Test
  public void entity_getAllEntities() throws Exception {
    // segment
    segment
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted");
    // memes
    segment.add(helper.getMeme("Hindsight"));
    segment.add(helper.getMeme("Chunky"));
    segment.add(helper.getMeme("Regret"));
    segment.add(helper.getMeme("Tangy"));
    // chords
    segment.add(helper.getChord(0.0, "F minor"));
    segment.add(helper.getChord(8.0, "Gb minor"));
    // choices
    segment.add(helper.getChoice(101, SequenceType.Macro, 130, 4));
    segment.add(helper.getChoice(102, SequenceType.Main, 415150, -2));
    segment.add(helper.getChoice(103, SequenceType.Rhythm, 35, -4));
    // arrangements
    segment.add(helper.getArrangement(201, 101));
    segment.add(helper.getArrangement(202, 101));
    segment.add(helper.getArrangement(203, 101));
    // picks
    segment.add(helper.getPick(201));
    segment.add(helper.getPick(202));
    segment.add(helper.getPick(203));

    assertEquals(15, segment.getAllEntities().size());
    segment.validate();
  }

  @Test
  public void arrangement_addAssignsUniqueIds() throws CoreException {
    segment.add(helper.getChoice(23));
    segment.add(helper.getArrangement(101, 23));
    segment.add(helper.getArrangement(102, 23));
    segment.add(helper.getArrangement(103, 23));

    assertEquals(3, segment.getArrangements().size());
    segment.validateContent();
  }

  @Test
  public void arrangement_addExceptionOnDuplicateId() throws CoreException {
    segment.add(helper.getChoice(23));
    segment.add(helper.getArrangement(100001, 23));
    failure.expect(CoreException.class);
    failure.expectMessage("already exists");

    segment.add(helper.getArrangement(100001, 23));
  }

  @Test
  public void arrangement_addExceptionOnBadRelationId() throws CoreException {
    segment.add(helper.getChoice(23));
    failure.expect(CoreException.class);
    failure.expectMessage("nonexistent choice");

    segment.add(helper.getArrangement(24));
  }

  @Test
  public void arrangement_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.add(helper.getChoice(23));
    segment.setArrangements(ImmutableList.of(
      helper.getArrangement(100001, 23),
      helper.getArrangement(100002, 23)
    ));
    segment.add(helper.getArrangement(23));

    segment.validateContent();
  }

  @Test
  public void arrangement_getAllForChoice() throws CoreException {
    Choice choice23 = helper.getChoice(23);
    segment.add(choice23);
    segment.add(helper.getChoice(24));
    segment.add(helper.getArrangement(101, 23));
    segment.add(helper.getArrangement(102, 23));
    segment.add(helper.getArrangement(24));

    assertEquals(2, segment.getArrangementsForChoice(choice23).size());
    segment.validateContent();
  }

  @Test
  public void arrangement_setAssignsUniqueIds() throws CoreException {
    segment.add(helper.getChoice(23));
    segment.setArrangements(ImmutableList.of(
      helper.getArrangement(101, 23),
      helper.getArrangement(102, 23),
      helper.getArrangement(103, 23)));

    assertEquals(3, segment.getArrangements().size());
    segment.validateContent();
  }

  @Test
  public void arrangement_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.add(helper.getChoice(23));
    segment.setArrangements(ImmutableList.of(
      helper.getArrangement(101, 23),
      helper.getArrangement(102, 23),
      helper.getArrangement(103, 23)));
    segment.add(helper.getArrangement(104, 23));

    assertEquals(4, segment.getArrangements().size());
    segment.validateContent();
  }

  @Test
  public void choice_addAssignsUniqueIds() throws CoreException {
    segment.add(new Choice()
      .setSegmentId(BigInteger.valueOf(1))
      .setTypeEnum(SequenceType.Macro)
      .setSequencePatternId(BigInteger.valueOf(123)));
    segment.add(new Choice()
      .setSegmentId(BigInteger.valueOf(1))
      .setTypeEnum(SequenceType.Main)
      .setSequencePatternId(BigInteger.valueOf(456)));
    segment.add(new Choice()
      .setSegmentId(BigInteger.valueOf(1))
      .setTypeEnum(SequenceType.Rhythm)
      .setSequenceId(BigInteger.valueOf(789)));

    segment.validateContent();
  }

  @Test
  public void choice_addExceptionOnDuplicateId() throws CoreException {
    segment.add(helper.getChoice(23));
    failure.expect(CoreException.class);
    failure.expectMessage("already exists");

    segment.add(helper.getChoice(23));
  }

  @Test
  public void choice_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.setChoices(ImmutableList.of(
      helper.getChoice(100001, SequenceType.Macro, 123, 0),
      helper.getChoice(100002, SequenceType.Main, 456, 0)));
    segment.add(new Choice()
      .setSegmentId(BigInteger.valueOf(1))
      .setTypeEnum(SequenceType.Rhythm)
      .setSequenceId(BigInteger.valueOf(789)));

    segment.validateContent();
  }

  @Test
  public void choice_setAssignsUniqueIds() throws CoreException {
    segment.setChoices(ImmutableList.of(new Choice()
        .setSegmentId(BigInteger.valueOf(1))
        .setTypeEnum(SequenceType.Macro)
        .setSequencePatternId(BigInteger.valueOf(123)),
      new Choice()
        .setSegmentId(BigInteger.valueOf(1))
        .setTypeEnum(SequenceType.Main)
        .setSequencePatternId(BigInteger.valueOf(456)),
      new Choice()
        .setSegmentId(BigInteger.valueOf(1))
        .setTypeEnum(SequenceType.Rhythm)
        .setSequenceId(BigInteger.valueOf(789))));

    assertEquals(3, segment.getChoices().size());
    segment.validateContent();
  }

  @Test
  public void choice_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.setChoices(ImmutableList.of(new Choice()
      .setSegmentId(BigInteger.valueOf(1))
      .setTypeEnum(SequenceType.Macro)
      .setSequencePatternId(BigInteger.valueOf(123))));
    segment.add(new Choice()
      .setSegmentId(BigInteger.valueOf(1))
      .setTypeEnum(SequenceType.Main)
      .setSequencePatternId(BigInteger.valueOf(456)));

    assertEquals(2, segment.getChoices().size());
    segment.validateContent();
  }

  @Test
  public void chord_addAssignsUniqueIds() throws CoreException {
    segment.add(helper.getChord(0, "C# Major"));
    segment.add(helper.getChord(1, "D7"));
    segment.add(helper.getChord(2, "G minor"));

    assertExactChords(ImmutableList.of("C# Major", "D7", "G minor"), segment.getChords());
    segment.validateContent();
  }

  @Test
  public void chord_addExceptionOnDuplicateId() throws CoreException {
    SegmentChord chord1 = helper.getChord(0.0, "C");
    chord1.setUuid(helper.getUuid(250));
    segment.add(chord1);
    failure.expect(CoreException.class);
    failure.expectMessage("already exists");

    segment.add(chord1);
  }

  @Test
  public void chord_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.setChords(ImmutableList.of(
      helper.getChord(0.0, "C# Major"),
      helper.getChord(0.0, "D7")));
    segment.add(new SegmentChord()
      .setPosition(0.0)
      .setSegmentId(BigInteger.valueOf(1))
      .setName("G minor"));

    segment.validateContent();
  }

  @Test
  public void chord_setAssignsUniqueIds() throws CoreException {
    segment.setChords(ImmutableList.of(
      helper.getChord(0, "C# Major"),
      helper.getChord(1, "D7"),
      helper.getChord(2, "G minor")
    ));

    assertExactChords(ImmutableList.of("C# Major", "D7", "G minor"), segment.getChords());
    segment.validateContent();
  }

  @Test
  public void chord_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.setChords(ImmutableList.of(helper.getChord(0, "C# Major")));
    segment.add(helper.getChord(0, "D7"));

    assertEquals(2, segment.getChords().size());
    segment.validateContent();
  }

  @Test
  public void meme_addAssignsUniqueIds() throws CoreException {
    segment.add(helper.getMeme("Red"));
    segment.add(helper.getMeme("Yellow"));
    segment.add(helper.getMeme("Blue"));

    assertExactMemes(ImmutableList.of("Red", "Yellow", "Blue"), segment.getMemes());
    segment.validateContent();
  }

  @Test
  public void meme_addExceptionOnDuplicateId() throws CoreException {
    SegmentMeme meme1 = helper.getMeme("Test");
    meme1.setUuid(UUID.randomUUID());
    segment.add(meme1);
    failure.expect(CoreException.class);
    failure.expectMessage("already exists");

    segment.add(meme1);
  }

  @Test
  public void meme_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.setMemes(ImmutableList.of(
      helper.getMeme("Red"),
      helper.getMeme("Yellow")));
    segment.add(helper.getMeme("Blue"));

    segment.validateContent();
  }

  @Test
  public void meme_setAssignsUniqueIds() throws CoreException {
    segment.setMemes(ImmutableList.of(
      helper.getMeme("Red"),
      helper.getMeme("Yellow"),
      helper.getMeme("Blue"))
    );

    assertExactMemes(ImmutableList.of("Red", "Yellow", "Blue"), segment.getMemes());
    segment.validateContent();
  }

  @Test
  public void meme_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.setMemes(ImmutableList.of(
      new SegmentMeme().setSegmentId(BigInteger.valueOf(1)).setName("Red")));
    segment.add(new SegmentMeme().setSegmentId(BigInteger.valueOf(1)).setName("Yellow"));

    assertEquals(2, segment.getMemes().size());
    segment.validateContent();
  }

  @Test
  public void message_addAssignsUniqueIds() throws CoreException {
    segment.add(helper.getMessage(MessageType.Info, "All is Well!"));
    segment.add(helper.getMessage(MessageType.Warning, "This is your Final Warning."));
    segment.add(helper.getMessage(MessageType.Error, "Danger, Will Robinson!"));

    assertEquals(3, segment.getMessages().size());
    segment.validateContent();
  }

  @Test
  public void message_addExceptionOnDuplicateId() throws CoreException {
    SegmentMessage message1 = helper.getMessage(MessageType.Info, "Test");
    message1.setUuid(UUID.randomUUID());
    segment.add(message1);
    failure.expect(CoreException.class);
    failure.expectMessage("already exists");

    segment.add(message1);
  }

  @Test
  public void message_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.setMessages(ImmutableList.of(
      helper.getMessage(MessageType.Info, "All is Well!"),
      helper.getMessage(MessageType.Warning, "This is your Final Warning.")));
    segment.add(helper.getMessage(MessageType.Error, "Danger, Will Robinson!"));

    segment.validateContent();
  }

  @Test
  public void message_setAssignsUniqueIds() throws CoreException {
    segment.setMessages(ImmutableList.of(
      helper.getMessage(MessageType.Info, "All is Well!"),
      helper.getMessage(MessageType.Warning, "This is your Final Warning."),
      helper.getMessage(MessageType.Error, "Danger, Will Robinson!")
    ));

    assertEquals(3, segment.getMessages().size());
    segment.validateContent();
  }

  @Test
  public void message_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.setMessages(ImmutableList.of(
      helper.getMessage(MessageType.Info, "All is Well!")));
    segment.add(helper.getMessage(MessageType.Warning, "This is your Final Warning."));

    assertEquals(2, segment.getMessages().size());
    segment.validateContent();
  }

  @Test
  public void pick_addAssignsUniqueIds() throws CoreException {
    segment.add(helper.getChoice(23));
    segment.add(helper.getArrangement(76, 23));
    segment.add(helper.getPick(76));
    segment.add(helper.getPick(76));
    segment.add(helper.getPick(76));

    assertEquals(3, segment.getPicks().size());
    segment.validateContent();
  }

  @Test
  public void pick_addExceptionOnDuplicateId() throws CoreException {
    Pick pk = helper.getPick(76);
    pk.setUuid(UUID.randomUUID());
    segment.add(helper.getChoice(23));
    segment.add(helper.getArrangement(76, 23));
    segment.add(pk);
    failure.expect(CoreException.class);
    failure.expectMessage("already exists");

    segment.add(pk);
  }

  @Test
  public void pick_addExceptionOnBadRelationId() throws CoreException {
    segment.add(helper.getChoice(23));
    segment.add(helper.getArrangement(76, 23));
    failure.expect(CoreException.class);
    failure.expectMessage("nonexistent arrangement");

    segment.add(helper.getPick(77));
  }

  @Test
  public void pick_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.add(helper.getChoice(23));
    segment.add(helper.getArrangement(76, 23));
    Pick pick1 = helper.getPick(76);
    pick1.setUuid(helper.getUuid(100001));
    Pick pick2 = helper.getPick(76);
    pick2.setUuid(helper.getUuid(100002));
    segment.setPicks(ImmutableList.of(pick1, pick2));
    segment.add(helper.getPick(76));

    segment.validateContent();
  }

  @Test
  public void pick_setAssignsUniqueIds() throws CoreException {
    segment.add(helper.getChoice(23));
    segment.add(helper.getArrangement(76, 23));
    segment.setPicks(ImmutableList.of(
      helper.getPick(76),
      helper.getPick(76),
      helper.getPick(76)));

    assertEquals(3, segment.getPicks().size());
    segment.validateContent();
  }

  @Test
  public void pick_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.add(helper.getChoice(23));
    segment.add(helper.getArrangement(76, 23));
    segment.setPicks(ImmutableList.of(
      helper.getPick(76),
      helper.getPick(76),
      helper.getPick(76)));
    segment.add(helper.getPick(76));

    assertEquals(4, segment.getPicks().size());
    segment.validateContent();
  }

  @Test
  public void report_getSet() throws CoreException {
    segment.setReport(ImmutableMap.of(
      "OutputSampleBits", "32",
      "Beans", "Aplenty",
      "Butts", "Farting"
    ));

    assertEquals("32", segment.getReport().get("OutputSampleBits"));
    assertEquals("Aplenty", segment.getReport().get("Beans"));
    assertEquals("Farting", segment.getReport().get("Butts"));
    segment.validateContent();
  }

  @Test
  public void report_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.setReport(ImmutableMap.of(
      "OutputSampleBits", "32",
      "Beans", "Aplenty",
      "Butts", "Farting"
    ));
    segment.putReport("Pants", "Useless");

    assertEquals("Useless", segment.getReport().get("Pants"));
    segment.validateContent();
  }

  @Test
  public void type_getSet() throws CoreException {
    segment.setType("Initial");

    assertEquals(FabricatorType.Initial, segment.getType());
    segment.validateContent();
  }

  @Test
  public void typeEnum_getSet() throws CoreException {
    segment.setTypeEnum(FabricatorType.Initial);

    assertEquals(FabricatorType.Initial, segment.getType());
    segment.validateContent();
  }

  @Test
  public void customJsonSerialization() throws CoreException {
    segment
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted");
    String result = gsonProvider.gson().toJson(segment);

    assertEquals("{\"id\":4,\"chainId\":180923,\"offset\":473,\"state\":\"Crafted\",\"beginAt\":\"2014-08-12T12:17:02.527142Z\",\"endAt\":\"2014-09-12T12:17:34.262679Z\",\"total\":64,\"density\":0.754,\"key\":\"G minor\",\"tempo\":121.0}", result);
  }

  @Test
  public void add_failsOnInvalidChoice() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Required to have either");

    segment.add(new Choice());
  }

  @Test
  public void add_failsOnInvalidArrangement() throws Exception {
    segment.add(helper.getChoice(1));
    failure.expect(CoreException.class);
    failure.expectMessage("Voice ID is required");

    segment.add(new Arrangement().setChoiceUuid(helper.getUuid(1)));
  }

  @Test
  public void add_failsOnInvalidArrangementRelation() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("has null choiceId");

    segment.add(new Arrangement());
  }

  @Test
  public void add_failsOnInvalidPick() throws Exception {
    segment.add(helper.getChoice(1));
    segment.add(helper.getArrangement(2, 1));
    failure.expect(CoreException.class);
    failure.expectMessage("Pattern Event ID is required");

    segment.add(new Pick().setArrangementUuid(helper.getUuid(2)));
  }

  @Test
  public void add_failsOnInvalidPickRelation() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("has null arrangementId");

    segment.add(new Pick());
  }

  @Test
  public void add_failsOnInvalidMeme() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    segment.add(new SegmentMeme());
  }

  @Test
  public void add_failsOnInvalidChord() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    segment.add(new SegmentChord());
  }

}
