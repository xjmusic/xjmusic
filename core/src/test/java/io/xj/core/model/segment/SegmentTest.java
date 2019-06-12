// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.segment.sub.Arrangement;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.segment.sub.SegmentChord;
import io.xj.core.model.segment.sub.SegmentMeme;
import io.xj.core.model.segment.sub.SegmentMessage;
import io.xj.core.model.segment.sub.Pick;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.UUID;

import static io.xj.core.testing.Assert.assertContains;
import static io.xj.core.testing.Assert.assertExactChords;
import static io.xj.core.testing.Assert.assertExactMemes;
import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentTest extends CoreTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  Segment segment;

  @Before
  public void setUp() throws Exception {
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
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_setAsTimestamps() throws Exception {
    segment
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_withNoEndAtTime_isOkay_becausePlannedSegments() throws Exception {
    segment
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setOffset(473L)
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
      .setOffset(473L)
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
      .setOffset(473L)
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
      .setOffset(473L)
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
      .setOffset(473L)
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
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0);

    segment.setStateEnum(SegmentState.Crafting);
    segment.validate();
  }

  @Test
  public void isInitial_trueAtOffsetZero() {
    assertTrue(segment
      .setOffset(0L)
      .isInitial());
  }

  @Test
  public void isInitial_falseAtOffsetOne() {
    assertFalse(segment
      .setOffset(1L)
      .isInitial());
  }

  @Test
  public void isInitial_falseAtOffsetHigh() {
    assertFalse(segment
      .setOffset(901L)
      .isInitial());
  }

  @Test
  public void getPreviousOffset() throws Exception {
    assertEquals(Long.valueOf(234L),
      segment.setOffset(235L).getPreviousOffset());
  }

  @Test
  public void getPreviousOffset_throwsExceptionForInitialSegment() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Cannot get previous id of initial Segment");

    segment.setOffset(0L).getPreviousOffset();
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
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted");
    // memes
    segment.add(newSegmentMeme("Hindsight"));
    segment.add(newSegmentMeme("Chunky"));
    segment.add(newSegmentMeme("Regret"));
    segment.add(newSegmentMeme("Tangy"));
    // choices
    UUID id1 = UUID.randomUUID();
    segment.add(new Choice()
      .setProgramId(BigInteger.valueOf(2))
      .setSequenceBindingId(id1)
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(4));
    UUID id2 = UUID.randomUUID();
    segment.add(new Choice()
      .setProgramId(BigInteger.valueOf(2))
      .setSequenceBindingId(id2)
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-2));
    segment.add(new Choice()
      .setProgramId(BigInteger.valueOf(35))
      .setTypeEnum(ProgramType.Rhythm)
      .setTranspose(-4));
    // chords
    segment.add(newSegmentChord(0.0, "F minor"));
    segment.add(newSegmentChord(8.0, "Gb minor"));

    assertEquals(id1, segment.getChoiceOfType(ProgramType.Macro).getSequenceBindingId());
    assertEquals(id2, segment.getChoiceOfType(ProgramType.Main).getSequenceBindingId());
    assertEquals(BigInteger.valueOf(35), segment.getChoiceOfType(ProgramType.Rhythm).getProgramId());
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
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted");
    // memes
    segment.add(newSegmentMeme("Hindsight"));
    segment.add(newSegmentMeme("Chunky"));
    segment.add(newSegmentMeme("Regret"));
    segment.add(newSegmentMeme("Tangy"));
    // chords
    segment.add(newSegmentChord(0.0, "F minor"));
    segment.add(newSegmentChord(8.0, "Gb minor"));
    // choices
    Choice choice1 = segment.add(newChoice(UUID.randomUUID(), ProgramType.Macro, 5, UUID.randomUUID(), 4));
    Choice choice2 = segment.add(newChoice(UUID.randomUUID(), ProgramType.Main, 5, UUID.randomUUID(), -2));
    Choice choice3 = segment.add(newChoice(UUID.randomUUID(), ProgramType.Rhythm, 5, UUID.randomUUID(), -4));
    // arrangements
    Arrangement arrangement1 = segment.add(newArrangement(choice1));
    Arrangement arrangement2 = segment.add(newArrangement(choice2));
    Arrangement arrangement3 = segment.add(newArrangement(choice3));
    // picks (currently NOT included in getAllSubEntities)
    segment.add(newPick(arrangement1));
    segment.add(newPick(arrangement2));
    segment.add(newPick(arrangement3));

    assertEquals(12, segment.getAllSubEntities().size());
    segment.validate();
  }

  @Test
  public void arrangement_addAssignsUniqueIds() throws CoreException {
    Choice choice = segment.add(newChoice(UUID.randomUUID()));
    segment.add(newArrangement(choice));
    segment.add(newArrangement(choice));
    segment.add(newArrangement(choice));

    assertEquals(3, segment.getArrangements().size());
    segment.validateContent();
  }

  @Test
  public void arrangement_addExceptionOnDuplicateId() throws CoreException {
    Choice choice = segment.add(newChoice(UUID.randomUUID()));
    Arrangement arrangement = new Arrangement()
      .setId(UUID.randomUUID())
      .setChoiceId(choice.getId())
      .setVoiceId(UUID.randomUUID())
      .setInstrumentId(BigInteger.valueOf(432L));
    segment.add(arrangement);

    segment.add(arrangement);

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("already exists", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void arrangement_addExceptionOnBadRelationId() throws CoreException {
    segment.add(newArrangement(new Choice().setId(UUID.randomUUID())));

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("nonexistent choice", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void arrangement_afterLoadingNewEntityHasUniqueId() throws CoreException {
    Choice choice = segment.add(newChoice(UUID.randomUUID()));
    segment.setArrangements(ImmutableList.of(
      newArrangement(choice),
      newArrangement(choice)
    ));
    segment.add(newArrangement(choice));

    segment.validateContent();
  }

  @Test
  public void arrangement_getAllForChoice() throws CoreException {
    Choice choiceB = segment.add(newChoice(UUID.randomUUID()));
    Choice choiceA = segment.add(newChoice(UUID.randomUUID()));
    segment.add(newArrangement(choiceA));
    segment.add(newArrangement(choiceB));
    segment.add(newArrangement(choiceB));

    assertEquals(2, segment.getArrangementsForChoice(choiceB).size());
    segment.validateContent();
  }

  @Test
  public void arrangement_setAssignsUniqueIds() throws CoreException {
    Choice choice = segment.add(newChoice(UUID.randomUUID()));
    segment.setArrangements(ImmutableList.of(
      newArrangement(choice),
      newArrangement(choice),
      newArrangement(choice)));

    assertEquals(3, segment.getArrangements().size());
    segment.validateContent();
  }

  @Test
  public void arrangement_setWithImmutableList_thenAddAnother() throws CoreException {
    Choice choice = segment.add(newChoice(UUID.randomUUID()));
    segment.setArrangements(ImmutableList.of(
      newArrangement(choice),
      newArrangement(choice),
      newArrangement(choice)));
    segment.add(newArrangement(choice));

    assertEquals(4, segment.getArrangements().size());
    segment.validateContent();
  }

  @Test
  public void choice_addAssignsUniqueIds() throws CoreException {
    segment.add(new Choice()
      .setProgramId(BigInteger.valueOf(2))
      .setTypeEnum(ProgramType.Macro)
      .setSequenceBindingId(UUID.randomUUID()));
    segment.add(new Choice()
      .setProgramId(BigInteger.valueOf(2))
      .setTypeEnum(ProgramType.Main)
      .setSequenceBindingId(UUID.randomUUID()));
    segment.add(new Choice()
      .setTypeEnum(ProgramType.Rhythm)
      .setProgramId(BigInteger.valueOf(789)));

    segment.validateContent();
  }

  @Test
  public void choice_addExceptionOnDuplicateId() throws CoreException {
    Choice choice = new Choice()
      .setId(UUID.randomUUID())
      .setProgramId(BigInteger.valueOf(2))
      .setTypeEnum(ProgramType.Macro)
      .setSequenceBindingId(UUID.randomUUID());
    segment.add(choice);

    segment.add(choice);

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("already exists", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void choice_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.setChoices(ImmutableList.of(
      newChoice(UUID.randomUUID(), ProgramType.Macro, 5, UUID.randomUUID(), 0),
      newChoice(UUID.randomUUID(), ProgramType.Main, 5, UUID.randomUUID(), 0)));
    segment.add(new Choice()
      .setTypeEnum(ProgramType.Rhythm)
      .setProgramId(BigInteger.valueOf(789)));

    segment.validateContent();
  }

  @Test
  public void choice_setAssignsUniqueIds() throws CoreException {
    segment.setChoices(ImmutableList.of(new Choice()
        .setProgramId(BigInteger.valueOf(2))
        .setTypeEnum(ProgramType.Macro)
        .setSequenceBindingId(UUID.randomUUID()),
      new Choice()
        .setProgramId(BigInteger.valueOf(2))
        .setTypeEnum(ProgramType.Main)
        .setSequenceBindingId(UUID.randomUUID()),
      new Choice()
        .setProgramId(BigInteger.valueOf(2))
        .setTypeEnum(ProgramType.Rhythm)
        .setProgramId(BigInteger.valueOf(789))));

    assertEquals(3, segment.getChoices().size());
    segment.validateContent();
  }

  @Test
  public void choice_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.setChoices(ImmutableList.of(new Choice()
      .setProgramId(BigInteger.valueOf(2))
      .setTypeEnum(ProgramType.Macro)
      .setSequenceBindingId(UUID.randomUUID())));
    segment.add(new Choice()
      .setProgramId(BigInteger.valueOf(2))
      .setTypeEnum(ProgramType.Main)
      .setSequenceBindingId(UUID.randomUUID()));

    assertEquals(2, segment.getChoices().size());
    segment.validateContent();
  }

  @Test
  public void chord_addAssignsUniqueIds() throws CoreException {
    segment.add(newSegmentChord(0, "C# Major"));
    segment.add(newSegmentChord(1, "D7"));
    segment.add(newSegmentChord(2, "G minor"));

    assertExactChords(ImmutableList.of("C# Major", "D7", "G minor"), segment.getChords());
    segment.validateContent();
  }

  @Test
  public void chord_addExceptionOnDuplicateId() throws CoreException {
    SegmentChord chord1 = newSegmentChord(0.0, "C");
    chord1.setId(UUID.randomUUID());
    segment.add(chord1);

    segment.add(chord1);

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("already exists", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void chord_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.setChords(ImmutableList.of(
      newSegmentChord(0.0, "C# Major"),
      newSegmentChord(0.0, "D7")));
    segment.add(new SegmentChord()
      .setPosition(0.0)
      .setName("G minor"));

    segment.validateContent();
  }

  @Test
  public void chord_setAssignsUniqueIds() throws CoreException {
    segment.setChords(ImmutableList.of(
      newSegmentChord(0, "C# Major"),
      newSegmentChord(1, "D7"),
      newSegmentChord(2, "G minor")
    ));

    assertExactChords(ImmutableList.of("C# Major", "D7", "G minor"), segment.getChords());
    segment.validateContent();
  }

  @Test
  public void chord_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.setChords(ImmutableList.of(newSegmentChord(0, "C# Major")));
    segment.add(newSegmentChord(0, "D7"));

    assertEquals(2, segment.getChords().size());
    segment.validateContent();
  }

  @Test
  public void meme_addAssignsUniqueIds() throws CoreException {
    segment.add(newSegmentMeme("Red"));
    segment.add(newSegmentMeme("Yellow"));
    segment.add(newSegmentMeme("Blue"));

    assertExactMemes(ImmutableList.of("Red", "Yellow", "Blue"), segment.getMemes());
    segment.validateContent();
  }

  @Test
  public void meme_addExceptionOnDuplicateId() throws CoreException {
    SegmentMeme meme1 = newSegmentMeme("Test");
    meme1.setId(UUID.randomUUID());
    segment.add(meme1);

    segment.add(meme1);

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("already exists", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void meme_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.setMemes(ImmutableList.of(
      newSegmentMeme("Red"),
      newSegmentMeme("Yellow")));
    segment.add(newSegmentMeme("Blue"));

    segment.validateContent();
  }

  @Test
  public void meme_setAssignsUniqueIds() throws CoreException {
    segment.setMemes(ImmutableList.of(
      newSegmentMeme("Red"),
      newSegmentMeme("Yellow"),
      newSegmentMeme("Blue"))
    );

    assertExactMemes(ImmutableList.of("Red", "Yellow", "Blue"), segment.getMemes());
    segment.validateContent();
  }

  @Test
  public void meme_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.setMemes(ImmutableList.of(
      newSegmentMeme("Red")));
    segment.add(newSegmentMeme("Yellow"));

    assertEquals(2, segment.getMemes().size());
    segment.validateContent();
  }

  @Test
  public void message_addAssignsUniqueIds() throws CoreException {
    segment.add(newSegmentMessage(MessageType.Info, "All is Well!"));
    segment.add(newSegmentMessage(MessageType.Warning, "This is your Final Warning."));
    segment.add(newSegmentMessage(MessageType.Error, "Danger, Will Robinson!"));

    assertEquals(3, segment.getMessages().size());
    segment.validateContent();
  }

  @Test
  public void message_addExceptionOnDuplicateId() throws CoreException {
    SegmentMessage message1 = newSegmentMessage(MessageType.Info, "Test");
    message1.setId(UUID.randomUUID());
    segment.add(message1);

    segment.add(message1);

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("already exists", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void message_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.setMessages(ImmutableList.of(
      newSegmentMessage(MessageType.Info, "All is Well!"),
      newSegmentMessage(MessageType.Warning, "This is your Final Warning.")));
    segment.add(newSegmentMessage(MessageType.Error, "Danger, Will Robinson!"));

    segment.validateContent();
  }

  @Test
  public void message_setAssignsUniqueIds() throws CoreException {
    segment.setMessages(ImmutableList.of(
      newSegmentMessage(MessageType.Info, "All is Well!"),
      newSegmentMessage(MessageType.Warning, "This is your Final Warning."),
      newSegmentMessage(MessageType.Error, "Danger, Will Robinson!")
    ));

    assertEquals(3, segment.getMessages().size());
    segment.validateContent();
  }

  @Test
  public void message_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.setMessages(ImmutableList.of(
      newSegmentMessage(MessageType.Info, "All is Well!")));
    segment.add(newSegmentMessage(MessageType.Warning, "This is your Final Warning."));

    assertEquals(2, segment.getMessages().size());
    segment.validateContent();
  }

  @Test
  public void pick_addAssignsUniqueIds() throws CoreException {
    Choice choice = segment.add(newChoice(UUID.randomUUID()));
    Arrangement arrangement = segment.add(newArrangement(choice));
    segment.add(newPick(arrangement));
    segment.add(newPick(arrangement));
    segment.add(newPick(arrangement));

    assertEquals(3, segment.getPicks().size());
    segment.validateContent();
  }

  @Test
  public void pick_addExceptionOnDuplicateId() throws CoreException {
    Choice choice = segment.add(newChoice(UUID.randomUUID()));
    Arrangement arrangement = segment.add(newArrangement(choice));
    Pick pk = newPick(arrangement);
    pk.setId(UUID.randomUUID());
    segment.add(pk);

    segment.add(pk);

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("already exists", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void pick_addExceptionOnBadRelationId() throws CoreException {
    segment.add(newPick(new Arrangement().setId(UUID.randomUUID())));

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("nonexistent arrangement", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void pick_afterLoadingNewEntityHasUniqueId() throws CoreException {
    Choice choice = segment.add(newChoice(UUID.randomUUID()));
    Arrangement arrangement = segment.add(newArrangement(choice));
    Pick pick1 = newPick(arrangement);
    pick1.setId(UUID.randomUUID());
    Pick pick2 = newPick(arrangement);
    pick2.setId(UUID.randomUUID());
    segment.setPicks(ImmutableList.of(pick1, pick2));
    segment.add(newPick(arrangement));

    segment.validateContent();
  }

  @Test
  public void pick_setAssignsUniqueIds() throws CoreException {
    Choice choice = segment.add(newChoice(UUID.randomUUID()));
    Arrangement arrangement = segment.add(newArrangement(choice));
    segment.setPicks(ImmutableList.of(
      newPick(arrangement),
      newPick(arrangement),
      newPick(arrangement)));

    assertEquals(3, segment.getPicks().size());
    segment.validateContent();
  }

  @Test
  public void pick_setWithImmutableList_thenAddAnother() throws CoreException {
    Choice choice = segment.add(newChoice(UUID.randomUUID()));
    Arrangement arrangement = segment.add(newArrangement(choice));
    segment.setPicks(ImmutableList.of(
      newPick(arrangement),
      newPick(arrangement),
      newPick(arrangement)));
    segment.add(newPick(arrangement));

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
  public void add_failsOnInvalidChoice() {
    segment.add(new Choice());

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("Program ID is required", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void add_failsOnInvalidArrangement() {
    Choice choice = segment.add(newChoice(UUID.randomUUID()));

    segment.add(new Arrangement().setChoiceId(choice.getId()));

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("Voice ID is required", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void add_failsOnInvalidArrangementRelation() {

    segment.add(new Arrangement());

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("has null choiceId", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void add_failsOnInvalidPick() {
    Choice choice = segment.add(newChoice(UUID.randomUUID()));
    segment.add(newArrangement(choice));

    segment.add(new Pick().setArrangementId(UUID.randomUUID()));

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("has nonexistent arrangementId", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void add_failsOnInvalidPickRelation() {
    segment.add(new Pick());

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("has null arrangementId", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void add_failsOnInvalidMeme() {
    segment.add(new SegmentMeme());

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("Name is required", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void add_failsOnInvalidChord() {
    segment.add(new SegmentChord());

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("Name is required", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("createdAt", "updatedAt", "state", "beginAt", "endAt", "key", "total", "offset", "density", "tempo", "waveformKey", "type"), segment.getResourceAttributeNames());
  }

}
