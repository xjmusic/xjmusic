// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <gmock/gmock.h>

#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/fabricator/Fabricator.h"
#include "../_helper/SegmentFixtures.h"
#include "../_mock/MockFabricatorFactory.h"
#include "../_mock/MockSegmentRetrospective.h"

// NOLINTNEXTLINE
using ::testing::_;
using ::testing::Return;
using ::testing::ReturnRef;

using namespace XJ;

class FabricatorTest : public ::testing::Test {
protected:
  int SEQUENCE_TOTAL_BEATS = 64;
  ContentEntityStore sourceMaterial;
  SegmentEntityStore store;
  MockFabricatorFactory *mockFabricatorFactory = new MockFabricatorFactory(store);
  MockSegmentRetrospective *mockRetrospective = new MockSegmentRetrospective(store, 2);
  Fabricator subject = Fabricator(*mockFabricatorFactory, store, sourceMaterial, 2, 48000.0f, 2, std::nullopt);
  ContentFixtures fake;
  Segment segment;

  void SetUp() override {
    // Manipulate the underlying entity store; reset before each test
    store.clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake.setupFixtureB1(sourceMaterial);
    fake.setupFixtureB2(sourceMaterial);
    fake.setupFixtureB3(sourceMaterial);

    // Here's a basic setup that can be replaced for complex tests
    auto chain = store.put(SegmentFixtures::buildChain(
        fake.project1,
        fake.template1,
        "test",
        Chain::Type::Production,
        Chain::State::Fabricate
    ));
    segment = store.put(SegmentFixtures::buildSegment(
        chain,
        2,
        Segment::State::Crafting,
        "G major",
        8,
        0.6f,
        240.0f,
        "seg123"
    ));
    EXPECT_CALL(*mockFabricatorFactory, loadRetrospective(_)).WillOnce(Return(mockRetrospective));
  }
};


TEST_F(FabricatorTest, pick_returned_by_picks) {
  sourceMaterial.put(ContentFixtures::buildTemplateBinding(fake.template1, fake.library2));
  auto chain = store.put(SegmentFixtures::buildChain(fake.project1, fake.template1, "test", Chain::Type::Production,
                                                     Chain::State::Fabricate));
  store.put(SegmentFixtures::buildSegment(chain, 1, Segment::State::Crafted, "F major", 8, 0.6f, 120.0f, "seg123"));
  segment = store.put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));
  store.put(SegmentFixtures::buildSegmentChord(segment, 0.0f, "A"));
  store.put(SegmentFixtures::buildSegmentChoice(segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED,
                                                fake.program5));
  SegmentChoice beatChoice = store.put(
      SegmentFixtures::buildSegmentChoice(segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED,
                                          fake.program35, fake.program35_voice0, fake.instrument8));
  SegmentChoiceArrangement beatArrangement = store.put(SegmentFixtures::buildSegmentChoiceArrangement(beatChoice));
  SegmentChoiceArrangementPick pick;
  pick.id = ContentEntity::randomUUID();
  pick.segmentId = beatArrangement.segmentId;
  pick.segmentChoiceArrangementId = beatArrangement.id;
  pick.programSequencePatternEventId = fake.program35_sequence0_pattern0_event0.id;
  pick.instrumentAudioId = fake.instrument8_audio8kick.id;
  pick.event = "CLANG";
  pick.startAtSegmentMicros = (long) (0.273 * ValueUtils::MICROS_PER_SECOND);
  pick.lengthMicros = (long) (1.571 * ValueUtils::MICROS_PER_SECOND);
  pick.amplitude = 0.8f;
  pick.tones = "A4";
  store.put(pick);
  EXPECT_CALL(*mockFabricatorFactory, loadRetrospective(_)).WillOnce(Return(mockRetrospective));

  std::set<SegmentChoiceArrangementPick> result = subject.getPicks();

  SegmentChoiceArrangementPick resultPick = *result.begin();
  ASSERT_EQ(beatArrangement.id, resultPick.segmentChoiceArrangementId);
  ASSERT_EQ(fake.instrument8_audio8kick.id, resultPick.instrumentAudioId);
  ASSERT_NEAR(0.273 * ValueUtils::MICROS_PER_SECOND, resultPick.startAtSegmentMicros, 0.001);
  ASSERT_NEAR(1.571 * ValueUtils::MICROS_PER_SECOND, resultPick.lengthMicros, 0.001);
  ASSERT_NEAR(0.8f, resultPick.amplitude, 0.1);
  ASSERT_EQ("A4", resultPick.tones);
}


/*
 * TODO - finish converting these Java/mockito tests to C++/Google Mock
 *
 *
TEST_F(FabricatorTest, getDistinctChordVoicingTypes) {
  sourceMaterial = ContentEntityStore(Stream.concat(
      Stream.concat(Stream.concat(fake.setupFixtureB1().stream(), fake.setupFixtureB2().stream()),
                    fake.setupFixtureB3().stream()),
      Stream.of(ContentFixtures::buildVoicing(fake.program5_sequence0_chord0, fake.program5_voiceSticky, "G4, B4, D4"),
                ContentFixtures::buildVoicing(fake.program5_sequence0_chord0, fake.program5_voiceStripe, "F5"),
                ContentFixtures::buildVoicing(fake.program5_sequence0_chord0, fake.program5_voicePad,
                                              "(None)") // No voicing notes- doesn't count!
      )).collect(Collectors.toList()));
  auto chain = store.put(SegmentFixtures::buildChain(fake.project1, fake.template1, "test", Chain::Type::Production,
                                                     Chain::State::Fabricate));
  segment = store.put(
      SegmentFixtures::buildSegment(chain, 0, Segment::State::Crafting, "F major", 8, 0.6f, 120.0f, "seg123"));
  store.put(SegmentFixtures::buildSegmentChoice(segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED,
                                                fake.program5));
  when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
  subject = Fabricator(mockFabricatorFactory, store, sourceMaterial, segment.id, mockJsonapiPayloadFactory,
                       jsonProvider, 48000.0f, 2, null);

  Set <Instrument::Type> result = subject.getDistinctChordVoicingTypes();

  ASSERT_EQ(Set.of(Instrument::Type::Bass, Instrument::Type::Sticky, Instrument::Type::Stripe), result);
}


*/
/**
 Choose next Macro program based on the memes of the last sequence from the previous Macro program https://github.com/xjmusic/xjmusic/issues/299
 *//*

TEST_F(FabricatorTest, Type) {
  auto chain = store.put(SegmentFixtures::buildChain(fake.project1, fake.template1, "test", Chain::Type::Production,
                                                     Chain::State::Fabricate));
  Segment previousSegment = store.put(
      SegmentFixtures::buildSegment(chain, 1, Segment::State::Crafted, "F major", 8, 0.6f, 120.0f, "seg123"));
  auto previousMacroChoice = // second-to-last sequence of macro program
      store.put(SegmentFixtures::buildSegmentChoice(previousSegment, SegmentChoice::DELTA_UNLIMITED,
                                                    SegmentChoice::DELTA_UNLIMITED, fake.program4,
                                                    fake.program4_sequence1_binding0));
  auto previousMainChoice = // last sequence of main program
      store.put(SegmentFixtures::buildSegmentChoice(previousSegment, SegmentChoice::DELTA_UNLIMITED,
                                                    SegmentChoice::DELTA_UNLIMITED, fake.program5,
                                                    fake.program5_sequence1_binding0));
  segment = store.put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));
  when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
  when(mockRetrospective.getPreviousChoiceOfType(Program::Type::Main)).thenReturn(Optional.of(previousMainChoice));
  when(mockRetrospective.getPreviousChoiceOfType(Program::Type::Macro)).thenReturn(Optional.of(previousMacroChoice));
  subject = Fabricator(mockFabricatorFactory, store, sourceMaterial, segment.id, mockJsonapiPayloadFactory,
                       jsonProvider, 48000.0f, 2, null);

  auto result = subject.type;

  ASSERT_EQ(Segment::Type::NextMacro, result);
}

// FUTURE: test getChoicesOfPreviousSegments

TEST_F(FabricatorTest, getMemeIsometryOfNextSequenceInPreviousMacro) {
  auto chain = store.put(SegmentFixtures::buildChain(fake.project1, fake.template1, "test", Chain::Type::Production,
                                                     Chain::State::Fabricate));
  Segment previousSegment = store.put(
      SegmentFixtures::buildSegment(chain, 1, Segment::State::Crafted, "F major", 8, 0.6f, 120.0f, "seg123"));
  auto previousMacroChoice = // second-to-last sequence of macro program
      store.put(SegmentFixtures::buildSegmentChoice(previousSegment, SegmentChoice::DELTA_UNLIMITED,
                                                    SegmentChoice::DELTA_UNLIMITED, fake.program4,
                                                    fake.program4_sequence1_binding0));
  store.put(SegmentFixtures::buildSegmentChoice(previousSegment, SegmentChoice::DELTA_UNLIMITED,
                                                SegmentChoice::DELTA_UNLIMITED, fake.program5,
                                                fake.program5_sequence1_binding0));
  segment = store.put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));
  when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
  when(mockRetrospective.getPreviousChoiceOfType(Program::Type::Macro)).thenReturn(Optional.of(previousMacroChoice));
  subject = Fabricator(mockFabricatorFactory, store, sourceMaterial, segment.id, mockJsonapiPayloadFactory,
                       jsonProvider, 48000.0f, 2, null);

  auto result = subject.getMemeIsometryOfNextSequenceInPreviousMacro();

  assertArrayEquals(String[]
  { "COZY", "TROPICAL" }, result.getSources().stream().sorted().toArray());
}


TEST_F(FabricatorTest, getChordAt) {
  auto chain = store.put(SegmentFixtures::buildChain(fake.project1, fake.template1, "test", Chain::Type::Production,
                                                     Chain::State::Fabricate));
  segment = store.put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));
  when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
  subject = Fabricator(mockFabricatorFactory, store, sourceMaterial, segment.id, mockJsonapiPayloadFactory,
                       jsonProvider, 48000.0f, 2, null);
  subject.put(SegmentFixtures::buildSegmentChord(segment, 0.0f, "C"), false);
  subject.put(SegmentFixtures::buildSegmentChord(segment, 2.0f, "F"), false);
  subject.put(SegmentFixtures::buildSegmentChord(segment, 5.5f, "Gm"), false);

  ASSERT_EQ("C", subject.getChordAt(0.0).orElseThrow().name);
  ASSERT_EQ("C", subject.getChordAt(1.0).orElseThrow().name);
  ASSERT_EQ("F", subject.getChordAt(2.0).orElseThrow().name);
  ASSERT_EQ("F", subject.getChordAt(3.0).orElseThrow().name);
  ASSERT_EQ("F", subject.getChordAt(5.0).orElseThrow().name);
  ASSERT_EQ("Gm", subject.getChordAt(5.5).orElseThrow().name);
  ASSERT_EQ("Gm", subject.getChordAt(6.0).orElseThrow().name);
  ASSERT_EQ("Gm", subject.getChordAt(7.5).orElseThrow().name);
}


TEST_F(FabricatorTest, computeProgramRange) {
  auto chain = store.put(SegmentFixtures::buildChain(fake.project1, fake.template1, "test", Chain::Type::Production,
                                                     Chain::State::Fabricate));
  segment = store.put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));
  when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
  auto program = ContentFixtures::buildProgram(Program::Type::Detail, "C", 120.0f);
  auto voice = ContentFixtures::buildVoice(program, Instrument::Type::Bass);
  auto track = ContentFixtures::buildTrack(voice);
  auto sequence = ContentFixtures::buildSequence(program, 4);
  auto pattern = ContentFixtures::buildPattern(sequence, voice, 4);
  sourceMaterial = ContentEntityStore(
      List.of(program, voice, track, sequence, pattern, fake.template1, fake.templateBinding1,
              ContentFixtures::buildEvent(pattern, track, 0.0f, 1.0f, "C1"),
              ContentFixtures::buildEvent(pattern, track, 1.0f, 1.0f, "D2")));
  subject = Fabricator(mockFabricatorFactory, store, sourceMaterial, segment.id, mockJsonapiPayloadFactory,
                       jsonProvider, 48000.0f, 2, null);

  auto result = subject.getProgramRange(program.id, Instrument::Type::Bass);

  assertTrue(Note.of("C1").sameAs(result.getLow().orElseThrow()));
  assertTrue(Note.of("D2").sameAs(result.getHigh().orElseThrow()));
}


TEST_F(FabricatorTest, computeProgramRange_ignoresAtonalNotes) {
  auto chain = store.put(
      buildChain(fake.project1, fake.template1, "test", Chain::Type::Production, Chain::State::Fabricate));
  segment = store.put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));
  when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
  auto program = ContentFixtures::buildProgram(Program::Type::Detail, "C", 120.0f);
  auto voice = ContentFixtures::buildVoice(program, Instrument::Type::Bass);
  auto track = ContentFixtures::buildTrack(voice);
  auto sequence = ContentFixtures::buildSequence(program, 4);
  auto pattern = ContentFixtures::buildPattern(sequence, voice, 4);
  sourceMaterial = ContentEntityStore(
      List.of(program, voice, track, sequence, pattern, ContentFixtures::buildEvent(pattern, track, 0.0f, 1.0f, "C1"),
              ContentFixtures::buildEvent(pattern, track, 1.0f, 1.0f, "X"),
              ContentFixtures::buildEvent(pattern, track, 2.0f, 1.0f, "D2"), fake.template1, fake.templateBinding1));
  subject = Fabricator(mockFabricatorFactory, store, sourceMaterial, segment.id, mockJsonapiPayloadFactory,
                       jsonProvider, 48000.0f, 2, null);

  auto result = subject.getProgramRange(program.id, Instrument::Type::Bass);

  assertTrue(Note.of("C1").sameAs(result.getLow().orElseThrow()));
  assertTrue(Note.of("D2").sameAs(result.getHigh().orElseThrow()));
}


TEST_F(FabricatorTest, getProgramSequence_fromSequence) {
  auto project1 = ContentFixtures::buildProject("fish");
  Template template1 = ContentFixtures::buildTemplate(project1, "Test Template 1", "test1");
  auto chain = store.put(SegmentFixtures::buildChain(template1));
  segment = store.put(
      SegmentFixtures::buildSegment(chain, Segment::Type::Continue, 17, 4, Segment::State::Crafted, "D major",
                                    SEQUENCE_TOTAL_BEATS, 0.73f, 120.0f,
                                    String.format("chains-%s-segments-%s", ChainUtils.getIdentifier(chain), 17), true));
  SegmentChoice choice = store.put(buildSegmentChoice(segment, Program::Type::Main, fake.program5_sequence0));
  when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
  sourceMaterial = ContentEntityStore(List.of(fake.program5_sequence0, fake.template1, fake.templateBinding1));
  subject = Fabricator(mockFabricatorFactory, store, sourceMaterial, segment.id, mockJsonapiPayloadFactory,
                       jsonProvider, 48000.0f, 2, null);

  auto result = subject.getProgramSequence(choice);

  ASSERT_EQ(fake.program5_sequence0.id, result.orElseThrow().id);
}


TEST_F(FabricatorTest, getProgramSequence_fromSequenceBinding) {
  auto project1 = ContentFixtures::buildProject("fish");
  Template template1 = ContentFixtures::buildTemplate(project1, "Test Template 1", "test1");
  auto chain = store.put(SegmentFixtures::buildChain(template1));
  segment = store.put(
      SegmentFixtures::buildSegment(chain, Segment::Type::Continue, 17, 4, Segment::State::Crafted, "D major",
                                    SEQUENCE_TOTAL_BEATS, 0.73f, 120.0f,
                                    String.format("chains-%s-segments-%s", ChainUtils.getIdentifier(chain), 17), true));
  SegmentChoice choice = store.put(buildSegmentChoice(segment, Program::Type::Main, fake.program5_sequence0_binding0));
  when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
  sourceMaterial = ContentEntityStore(
      List.of(fake.program5_sequence0, fake.program5_sequence0_binding0, fake.template1, fake.templateBinding1));
  subject = Fabricator(mockFabricatorFactory, store, sourceMaterial, segment.id, mockJsonapiPayloadFactory,
                       jsonProvider, 48000.0f, 2, null);

  auto result = subject.getProgramSequence(choice);

  ASSERT_EQ(fake.program5_sequence0.id, result.orElseThrow().id);
}

*/
/**
 Sticky buns v2 use slash root when available https://github.com/xjmusic/xjmusic/issues/231
 *//*

TEST_F(FabricatorTest, getRootNote) {
  auto result = subject.getRootNoteMidRange("C3,E3,G3,A#3,C4,E4,G4", Chord.of("Cm")).orElseThrow();
  ASSERT_EQ(PitchClass.C, result.getPitchClass());
  ASSERT_EQ(4, result.getOctave().intValue());
}

*/
/**
 Should add meme from ALL program and instrument types! https://github.com/xjmusic/xjmusic/issues/210
 *//*

TEST_F(FabricatorTest, put_addsMemesForChoice) {
  subject.put(
      buildSegmentChoice(segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED, fake.program9,
                         fake.program9_voice0, fake.instrument8), false);
  subject.put(buildSegmentChoice(segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED, fake.program4,
                                 fake.program4_sequence1_binding0), false);

  auto resultMemes = store.readAll(segment.id, SegmentMeme.
  class).stream().sorted(Comparator.comparing(SegmentMeme::getName)).toList();
  ASSERT_EQ("BASIC", (resultMemes.get(0)).name);
  ASSERT_EQ("COZY", (resultMemes.get(1)).name);
  ASSERT_EQ("HEAVY", (resultMemes.get(2)).name);
  ASSERT_EQ("TROPICAL", (resultMemes.get(3)).name);
  ASSERT_EQ("WILD", (resultMemes.get(4)).name);
  auto resultChoices = store.readAll(segment.id, SegmentChoice.
  class).stream().sorted(Comparator.comparing(SegmentChoice::getProgramType)).toList();
  ASSERT_EQ(fake.program4.id, (resultChoices.get(0)).programId);
  ASSERT_EQ(fake.program4_sequence1_binding0.id, (resultChoices.get(0)).programSequenceBindingId);
  ASSERT_EQ(fake.instrument8.id, (resultChoices.get(1)).instrumentId);
}

*/
/**
 Unit test behavior of choosing an event for a note in a detail program
 <p>
 Sticky bun note choices should persist into following segments https://github.com/xjmusic/xjmusic/issues/281
 *//*

TEST_F(FabricatorTest, getStickyBun_readMetaFromCurrentSegment) {
  auto bun = StickyBun(fake.program9_sequence0_pattern0_event0.id, 3);
  auto bunJson = jsonProvider.getMapper().writeValueAsString(bun);
  auto bunKey = StickyBun.computeMetaKey(fake.program9_sequence0_pattern0_event0.id);
  store.put(SegmentFixtures::buildSegmentMeta(segment, bunKey, bunJson));

  auto result = subject.getStickyBun(fake.program9_sequence0_pattern0_event0.id).orElseThrow();

  ASSERT_EQ(fake.program9_sequence0_pattern0_event0.id, result.getEventId());
  assertArrayEquals(bun.getValues().toArray(), result.getValues().toArray());
}

*/
/**
 Unit test behavior of choosing an event for a note in a detail program
 <p>
 Sticky bun note choices should persist into following segments https://github.com/xjmusic/xjmusic/issues/281
 *//*

TEST_F(FabricatorTest, getStickyBun_readMetaFromPreviousSegment) {
  auto bun = StickyBun(fake.program9_sequence0_pattern0_event0.id, 3);
  auto bunJson = jsonProvider.getMapper().writeValueAsString(bun);
  auto bunKey = StickyBun.computeMetaKey(fake.program9_sequence0_pattern0_event0.id);
  auto bunMeta = SegmentFixtures::buildSegmentMeta(segment, bunKey, bunJson);
  when(mockRetrospective.getPreviousMeta(eq(bunKey))).thenReturn(Optional.of(bunMeta));

  auto result = subject.getStickyBun(fake.program9_sequence0_pattern0_event0.id).orElseThrow();

  ASSERT_EQ(fake.program9_sequence0_pattern0_event0.id, result.getEventId());
  assertArrayEquals(bun.getValues().toArray(), result.getValues().toArray());
}

*/
/**
 Unit test behavior of choosing a different events for a series of X notes in a detail program
 <p>
 Sticky bun note choices should persist into following segments https://github.com/xjmusic/xjmusic/issues/281
 *//*

TEST_F(FabricatorTest, getStickyBun_createForEvent) {
  auto result = subject.getStickyBun(fake.program9_sequence0_pattern0_event0.id).orElseThrow();

  ASSERT_EQ(fake.program9_sequence0_pattern0_event0.id, result.getEventId());
}

*/
/**
 Unit test behavior of choosing an event for a note in a detail program
 <p>
 Sticky bun note choices should persist into following segments https://github.com/xjmusic/xjmusic/issues/281
 *//*

TEST_F(FabricatorTest, getStickyBun_multipleEventsPickedSeparately) {
  auto bun0 = StickyBun(fake.program9_sequence0_pattern0_event0.id, 3);
  auto bunJson0 = jsonProvider.getMapper().writeValueAsString(bun0);
  auto bunKey0 = StickyBun.computeMetaKey(fake.program9_sequence0_pattern0_event0.id);
  store.put(SegmentFixtures::buildSegmentMeta(segment, bunKey0, bunJson0));
  auto bun1 = StickyBun(fake.program9_sequence0_pattern0_event1.id, 3);
  auto bunJson1 = jsonProvider.getMapper().writeValueAsString(bun1);
  auto bunKey1 = StickyBun.computeMetaKey(fake.program9_sequence0_pattern0_event1.id);
  store.put(SegmentFixtures::buildSegmentMeta(segment, bunKey1, bunJson1));

  auto result0 = subject.getStickyBun(fake.program9_sequence0_pattern0_event0.id).orElseThrow();
  auto result1 = subject.getStickyBun(fake.program9_sequence0_pattern0_event1.id).orElseThrow();

  ASSERT_EQ(fake.program9_sequence0_pattern0_event0.id, result0.getEventId());
  assertArrayEquals(bun0.getValues().toArray(), result0.getValues().toArray());
  ASSERT_EQ(fake.program9_sequence0_pattern0_event1.id, result1.getEventId());
  assertArrayEquals(bun1.getValues().toArray(), result1.getValues().toArray());
}


TEST_F(FabricatorTest, getMemeTaxonomy) {
  auto result = subject.getMemeTaxonomy();

  ASSERT_EQ(2, result.getCategories().size());
  ASSERT_EQ("COLOR", result.getCategories().get(0).name);
  ASSERT_EQ("SEASON", result.getCategories().get(1).name);
}

*/

// Test for getSegmentId
TEST_F(FabricatorTest, GetSegmentId) {
  SegmentChoice segmentChoice;
  segmentChoice.segmentId = segment.id;
  SegmentChoiceArrangement segmentChoiceArrangement;
  segmentChoiceArrangement.segmentId = segment.id;
  SegmentChoiceArrangementPick segmentChoiceArrangementPick;
  segmentChoiceArrangementPick.segmentId = segment.id;
  SegmentChord segmentChord;
  segmentChord.segmentId = segment.id;
  SegmentChordVoicing segmentChordVoicing;
  segmentChordVoicing.segmentId = segment.id;
  SegmentMeme segmentMeme;
  segmentMeme.segmentId = segment.id;
  SegmentMessage segmentMessage;
  segmentMessage.segmentId = segment.id;
  SegmentMeta segmentMeta;
  segmentMeta.segmentId = segment.id;

  ASSERT_EQ(segment.id, Fabricator::getSegmentId(segmentChoice));
  ASSERT_EQ(segment.id, Fabricator::getSegmentId(segmentChoiceArrangement));
  ASSERT_EQ(segment.id, Fabricator::getSegmentId(segmentChoiceArrangementPick));
  ASSERT_EQ(segment.id, Fabricator::getSegmentId(segmentChord));
  ASSERT_EQ(segment.id, Fabricator::getSegmentId(segmentChordVoicing));
  ASSERT_EQ(segment.id, Fabricator::getSegmentId(segmentMeme));
  ASSERT_EQ(segment.id, Fabricator::getSegmentId(segmentMessage));
  ASSERT_EQ(segment.id, Fabricator::getSegmentId(segmentMeta));
}