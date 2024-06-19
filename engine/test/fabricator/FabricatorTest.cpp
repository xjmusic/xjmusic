// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <vector>

#include "../_helper/SegmentFixtures.h"
#include "../_mock/MockSegmentRetrospective.h"

#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/fabricator/FabricatorFactory.h"

// NOLINTNEXTLINE
using ::testing::_;
using ::testing::Return;
using ::testing::ReturnRef;

using namespace XJ;

class FabricatorTest : public ::testing::Test { // NOLINT(*-pro-type-member-init)
protected:
  int SEQUENCE_TOTAL_BEATS = 64;
  ContentEntityStore *sourceMaterial;
  SegmentEntityStore *store;
  MockSegmentRetrospective *mockRetrospective;
  Fabricator *subject;
  ContentFixtures fake;
  Segment segment;

  void SetUp() override {
    sourceMaterial = new ContentEntityStore();
    store = new SegmentEntityStore();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake.setupFixtureB1(sourceMaterial);
    fake.setupFixtureB2(sourceMaterial);
    fake.setupFixtureB3(sourceMaterial);

    // Here's a basic setup that can be replaced for complex tests
    auto chain = store->put(SegmentFixtures::buildChain(
        fake.project1,
        fake.template1,
        "test",
        Chain::Type::Production,
        Chain::State::Fabricate
    ));
    segment = store->put(SegmentFixtures::buildSegment(
        chain,
        2,
        Segment::State::Crafting,
        "G major",
        8,
        0.6f,
        240.0f,
        "seg123"
    ));
    mockRetrospective = new MockSegmentRetrospective(store, 2);
    subject = new Fabricator(sourceMaterial, store, mockRetrospective, 2, 48000.0f, 2, std::nullopt);
  }

  void TearDown() override {
    delete store;
    delete mockRetrospective;
    delete subject;
  }
};


TEST_F(FabricatorTest, PickReturnedByPicks) {
  sourceMaterial->put(ContentFixtures::buildTemplateBinding(fake.template1, fake.library2));
  auto chain = store->put(SegmentFixtures::buildChain(fake.project1, fake.template1, "test", Chain::Type::Production,
                                                      Chain::State::Fabricate));
  store->put(SegmentFixtures::buildSegment(chain, 1, Segment::State::Crafted, "F major", 8, 0.6f, 120.0f, "seg123"));
  segment = store->put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));
  store->put(SegmentFixtures::buildSegmentChord(segment, 0.0f, "A"));
  store->put(
      SegmentFixtures::buildSegmentChoice(segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED,
                                          fake.program5));
  SegmentChoice beatChoice = store->put(
      SegmentFixtures::buildSegmentChoice(segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED,
                                          fake.program35, fake.program35_voice0, fake.instrument8));
  SegmentChoiceArrangement beatArrangement = store->put(SegmentFixtures::buildSegmentChoiceArrangement(beatChoice));
  SegmentChoiceArrangementPick pick;
  pick.id = ContentEntity::computeUniqueId();
  pick.segmentId = beatArrangement.segmentId;
  pick.segmentChoiceArrangementId = beatArrangement.id;
  pick.programSequencePatternEventId = fake.program35_sequence0_pattern0_event0.id;
  pick.instrumentAudioId = fake.instrument8_audio8kick.id;
  pick.event = "CLANG";
  pick.startAtSegmentMicros = static_cast<long>(0.273 * (double) ValueUtils::MICROS_PER_SECOND);
  pick.lengthMicros = static_cast<long>(1.571 * (double) ValueUtils::MICROS_PER_SECOND);
  pick.amplitude = 0.8f;
  pick.tones = "A4";
  store->put(pick);

  std::set<SegmentChoiceArrangementPick> result = subject->getPicks();

  SegmentChoiceArrangementPick resultPick = *result.begin();
  ASSERT_EQ(beatArrangement.id, resultPick.segmentChoiceArrangementId);
  ASSERT_EQ(fake.instrument8_audio8kick.id, resultPick.instrumentAudioId);
  ASSERT_NEAR(0.273 * ValueUtils::MICROS_PER_SECOND, resultPick.startAtSegmentMicros, 0.001);
  ASSERT_NEAR(1.571 * ValueUtils::MICROS_PER_SECOND, resultPick.lengthMicros, 0.001);
  ASSERT_NEAR(0.8f, resultPick.amplitude, 0.1);
  ASSERT_EQ("A4", resultPick.tones);
}


TEST_F(FabricatorTest, GetDistinctChordVoicingTypes) {
  sourceMaterial->put(ContentFixtures::buildVoicing(
      fake.program5_sequence0_chord0, fake.program5_voiceSticky, "G4, B4, D4"));
  sourceMaterial->put(ContentFixtures::buildVoicing(
      fake.program5_sequence0_chord0, fake.program5_voiceStripe, "F5"));
  sourceMaterial->put(ContentFixtures::buildVoicing(
      fake.program5_sequence0_chord0, fake.program5_voicePad, "(None)")); // No voicing notes- doesn't count!

  // Create a chain
  auto chain = store->put(SegmentFixtures::buildChain(
      fake.project1, fake.template1, "test", Chain::Type::Production, Chain::State::Fabricate));

  // Create a segment choice
  store->put(SegmentFixtures::buildSegmentChoice(
      segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED, fake.program5));

  // Get the result
  std::set<Instrument::Type> result = subject->getDistinctChordVoicingTypes();

  // Check the result
  std::set<Instrument::Type> expected = {Instrument::Type::Bass, Instrument::Type::Sticky, Instrument::Type::Stripe};
  ASSERT_EQ(expected, result);
}

/**
 Choose next Macro program based on the memes of the last sequence from the previous Macro program https://github.com/xjmusic/xjmusic/issues/299
 */
TEST_F(FabricatorTest, GetType) {
  // Create a chain
  auto chain = store->put(SegmentFixtures::buildChain(fake.project1, fake.template1, "test", Chain::Type::Production,
                                                      Chain::State::Fabricate));

  // Create previous segments with different choices
  Segment previousSegment = store->put(
      SegmentFixtures::buildSegment(chain, 1, Segment::State::Crafted, "F major", 8, 0.6f, 120.0f, "seg123"));
  auto previousMacroChoice = store->put(SegmentFixtures::buildSegmentChoice(previousSegment, SegmentChoice::DELTA_UNLIMITED,
                                                                            SegmentChoice::DELTA_UNLIMITED, fake.program4,
                                                                            fake.program4_sequence1_binding0));
  auto previousMainChoice = store->put(SegmentFixtures::buildSegmentChoice(previousSegment, SegmentChoice::DELTA_UNLIMITED,
                                                                           SegmentChoice::DELTA_UNLIMITED, fake.program5,
                                                                           fake.program5_sequence1_binding0));

  // Create the current segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));

  // Get the result
  auto result = subject->getType();

  // Check the result
  ASSERT_EQ(Segment::Type::NextMacro, result);
}


// FUTURE: test getChoicesOfPreviousSegments


TEST_F(FabricatorTest, GetMemeIsometryOfNextSequenceInPreviousMacro) {
  // Create a chain
  auto chain = store->put(SegmentFixtures::buildChain(fake.project1, fake.template1, "test", Chain::Type::Production,
                                                      Chain::State::Fabricate));

  // Create previous segments with different choices
  Segment previousSegment = store->put(
      SegmentFixtures::buildSegment(chain, 1, Segment::State::Crafted, "F major", 8, 0.6f, 120.0f, "seg123"));
  auto previousMacroChoice = store->put(SegmentFixtures::buildSegmentChoice(previousSegment, SegmentChoice::DELTA_UNLIMITED,
                                                                            SegmentChoice::DELTA_UNLIMITED, fake.program4,
                                                                            fake.program4_sequence1_binding0));
  store->put(SegmentFixtures::buildSegmentChoice(previousSegment, SegmentChoice::DELTA_UNLIMITED,
                                                 SegmentChoice::DELTA_UNLIMITED, fake.program5,
                                                 fake.program5_sequence1_binding0));

  // Create the current segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));

  // Set up the mock Retrospective to return the previous choices
  EXPECT_CALL(*mockRetrospective, getPreviousChoiceOfType(Program::Type::Macro)).WillOnce(Return(previousMacroChoice));

  // Get the result
  auto result = subject->getMemeIsometryOfNextSequenceInPreviousMacro();
  ASSERT_EQ(2, result.getSources().size());
  ASSERT_FALSE(result.getSources().find("COZY") == result.getSources().end());
  ASSERT_FALSE(result.getSources().find("TROPICAL") == result.getSources().end());
}


TEST_F(FabricatorTest, GetChordAt) {
  // Create a chain
  auto chain = store->put(SegmentFixtures::buildChain(fake.project1, fake.template1, "test", Chain::Type::Production,
                                                      Chain::State::Fabricate));

  // Create a segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));

  // Add chords to the subject
  subject->put(SegmentFixtures::buildSegmentChord(segment, 0.0f, "C"));
  subject->put(SegmentFixtures::buildSegmentChord(segment, 2.0f, "F"));
  subject->put(SegmentFixtures::buildSegmentChord(segment, 5.5f, "Gm"));

  // Check the chords at different times
  ASSERT_EQ("C", subject->getChordAt(0.0)->name);
  ASSERT_EQ("C", subject->getChordAt(1.0)->name);
  ASSERT_EQ("F", subject->getChordAt(2.0)->name);
  ASSERT_EQ("F", subject->getChordAt(3.0)->name);
  ASSERT_EQ("F", subject->getChordAt(5.0)->name);
  ASSERT_EQ("Gm", subject->getChordAt(5.5)->name);
  ASSERT_EQ("Gm", subject->getChordAt(6.0)->name);
  ASSERT_EQ("Gm", subject->getChordAt(7.5)->name);
}


TEST_F(FabricatorTest, ComputeProgramRange) {
  // Create a chain
  auto chain = store->put(SegmentFixtures::buildChain(fake.project1, fake.template1, "test", Chain::Type::Production,
                                                      Chain::State::Fabricate));

  // Create a segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));

  // Create a program
  auto program = ContentFixtures::buildProgram(Program::Type::Detail, "C", 120.0f);

  // Create a voice
  auto voice = ContentFixtures::buildVoice(program, Instrument::Type::Bass);

  // Create a track
  auto track = ContentFixtures::buildTrack(voice);

  // Create a sequence
  auto sequence = ContentFixtures::buildSequence(program, 4);

  // Create a pattern
  auto pattern = ContentFixtures::buildPattern(sequence, voice, 4);

  // Add entities to sourceMaterial
  sourceMaterial->put(program);
  sourceMaterial->put(voice);
  sourceMaterial->put(track);
  sourceMaterial->put(sequence);
  sourceMaterial->put(pattern);
  sourceMaterial->put(fake.template1);
  sourceMaterial->put(fake.templateBinding1);
  sourceMaterial->put(ContentFixtures::buildEvent(pattern, track, 0.0f, 1.0f, "C1"));
  sourceMaterial->put(ContentFixtures::buildEvent(pattern, track, 1.0f, 1.0f, "D2"));

  // Get the result
  auto result = subject->getProgramRange(program.id, Instrument::Type::Bass);

  // Check the result
  ASSERT_EQ(Note::of("C1"), result.low.value());
  ASSERT_EQ(Note::of("D2"), result.high.value());
}


TEST_F(FabricatorTest, ComputeProgramRange_IgnoresAtonalNotes) {
  // Create a chain
  auto chain = store->put(SegmentFixtures::buildChain(fake.project1, fake.template1, "test", Chain::Type::Production,
                                                      Chain::State::Fabricate));

  // Create a segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));

  // Create a program
  auto program = ContentFixtures::buildProgram(Program::Type::Detail, "C", 120.0f);

  // Create a voice
  auto voice = ContentFixtures::buildVoice(program, Instrument::Type::Bass);

  // Create a track
  auto track = ContentFixtures::buildTrack(voice);

  // Create a sequence
  auto sequence = ContentFixtures::buildSequence(program, 4);

  // Create a pattern
  auto pattern = ContentFixtures::buildPattern(sequence, voice, 4);

  // Add entities to sourceMaterial
  sourceMaterial->put(program);
  sourceMaterial->put(voice);
  sourceMaterial->put(track);
  sourceMaterial->put(sequence);
  sourceMaterial->put(pattern);
  sourceMaterial->put(fake.template1);
  sourceMaterial->put(fake.templateBinding1);
  sourceMaterial->put(ContentFixtures::buildEvent(pattern, track, 0.0f, 1.0f, "C1"));
  sourceMaterial->put(ContentFixtures::buildEvent(pattern, track, 1.0f, 1.0f, "X"));
  sourceMaterial->put(ContentFixtures::buildEvent(pattern, track, 2.0f, 1.0f, "D2"));

  // Get the result
  auto result = subject->getProgramRange(program.id, Instrument::Type::Bass);

  // Check the result
  ASSERT_EQ(Note::of("C1"), result.low.value());
  ASSERT_EQ(Note::of("D2"), result.high.value());
}


TEST_F(FabricatorTest, GetProgramSequence_FromSequence) {
  // Create a project
  auto project1 = ContentFixtures::buildProject("fish");

  // Create a template
  Template template1 = ContentFixtures::buildTemplate(project1, "Test Template 1", "test1");

  // Create a chain
  auto chain = store->put(SegmentFixtures::buildChain(template1));

  // Create a segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, Segment::Type::Continue, 17, 4, Segment::State::Crafted, "D major",
                                    SEQUENCE_TOTAL_BEATS, 0.73f, 120.0f,
                                    "chains-" + ChainUtils::getIdentifier(chain) + "-segments-" + std::to_string(17), true));

  // Create a segment choice
  SegmentChoice choice = store->put(SegmentFixtures::buildSegmentChoice(segment, Program::Type::Main, fake.program5_sequence0));

  // Get the result
  auto result = subject->getProgramSequence(choice);

  // Check the result
  ASSERT_EQ(fake.program5_sequence0.id, result.value()->id);
}


TEST_F(FabricatorTest, GetProgramSequence_FromSequenceBinding) {
  // Create a project
  auto project1 = ContentFixtures::buildProject("fish");

  // Create a template
  Template template1 = ContentFixtures::buildTemplate(project1, "Test Template 1", "test1");

  // Create a chain
  auto chain = store->put(SegmentFixtures::buildChain(template1));

  // Create a segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, Segment::Type::Continue, 17, 4, Segment::State::Crafted, "D major",
                                    SEQUENCE_TOTAL_BEATS, 0.73f, 120.0f,
                                    "chains-" + ChainUtils::getIdentifier(chain) + "-segments-" + std::to_string(17), true));

  // Create a segment choice
  SegmentChoice choice = store->put(SegmentFixtures::buildSegmentChoice(segment, Program::Type::Main, fake.program5_sequence0_binding0));

  // Get the result
  auto result = subject->getProgramSequence(choice);

  // Check the result
  ASSERT_EQ(fake.program5_sequence0.id, result.value()->id);
}

/**
 Sticky buns v2 use slash root when available https://github.com/xjmusic/xjmusic/issues/231
 */
TEST_F(FabricatorTest, GetRootNote) {
  // Call the method and get the result
  std::optional<Note> result = subject->getRootNoteMidRange("C3,E3,G3,A#3,C4,E4,G4", Chord::of("Cm"));

  // Check the result
  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(PitchClass::C, result.value().pitchClass);
  ASSERT_EQ(4, result.value().octave);
}

/**
 Should add meme from ALL program and instrument types! https://github.com/xjmusic/xjmusic/issues/210
 */
TEST_F(FabricatorTest, PutAddsMemesForChoice) {
  // Call the method and get the result
  subject->put(
      SegmentFixtures::buildSegmentChoice(segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED, fake.program9,
                         fake.program9_voice0, fake.instrument8), false);
  subject->put(SegmentFixtures::buildSegmentChoice(segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED, fake.program4,
                                 fake.program4_sequence1_binding0), false);

  // Get the result
  auto resultMemes = store->readAllSegmentMemes(segment.id);
  std::vector<SegmentMeme> sortedResultMemes = std::vector<SegmentMeme>(resultMemes.begin(), resultMemes.end());
  std::sort(sortedResultMemes.begin(), sortedResultMemes.end(), [](const SegmentMeme& a, const SegmentMeme& b) {
    return a.name < b.name;
  });

  // Check the result
  ASSERT_EQ("BASIC", sortedResultMemes[0].name);
  ASSERT_EQ("COZY", sortedResultMemes[1].name);
  ASSERT_EQ("HEAVY", sortedResultMemes[2].name);
  ASSERT_EQ("TROPICAL", sortedResultMemes[3].name);
  ASSERT_EQ("WILD", sortedResultMemes[4].name);

  auto resultChoices = store->readAllSegmentChoices(segment.id);
  std::vector<SegmentChoice> sortedResultChoices = std::vector<SegmentChoice>(resultChoices.begin(), resultChoices.end());
  std::sort(sortedResultChoices.begin(), sortedResultChoices.end(), [](const SegmentChoice& a, const SegmentChoice& b) {
    return a.programType < b.programType;
  });

  ASSERT_EQ(fake.program4.id, sortedResultChoices[0].programId);
  ASSERT_EQ(fake.program4_sequence1_binding0.id, sortedResultChoices[0].programSequenceBindingId);
  ASSERT_EQ(fake.instrument8.id, sortedResultChoices[1].instrumentId);
}


/*
 Unit test behavior of choosing an event for a note in a detail program
 <p>
 Sticky bun note choices should persist into following segments https://github.com/xjmusic/xjmusic/issues/281
*/
TEST_F(FabricatorTest, GetStickyBun_ReadMetaFromCurrentSegment) {
  auto bun = StickyBun(fake.program9_sequence0_pattern0_event0.id, 3);
  auto bunJson = bun.serialize();
  auto bunKey = StickyBun::computeMetaKey(fake.program9_sequence0_pattern0_event0.id);
  store->put(SegmentFixtures::buildSegmentMeta(segment, bunKey, bunJson));

  auto result = subject->getStickyBun(fake.program9_sequence0_pattern0_event0.id);

  // Check the result
  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(fake.program9_sequence0_pattern0_event0.id, result->eventId);
  ASSERT_EQ(bun.values, result->values);
}

/*
 Unit test behavior of choosing an event for a note in a detail program
 <p>
 Sticky bun note choices should persist into following segments https://github.com/xjmusic/xjmusic/issues/281
*/
TEST_F(FabricatorTest, GetStickyBun_ReadMetaFromPreviousSegment) {
  // Create a StickyBun
  auto bun = StickyBun(fake.program9_sequence0_pattern0_event0.id, 3);

  // Convert the StickyBun to a JSON string
  auto bunJson = bun.serialize();

  // Compute the meta key for the StickyBun
  auto bunKey = StickyBun::computeMetaKey(fake.program9_sequence0_pattern0_event0.id);

  // Compute a StickyBun but don't put it in the SegmentEntityStore-- we'll inject it through the mock retrospective
  auto bunMeta = SegmentFixtures::buildSegmentMeta(segment, bunKey, bunJson);

  // Set up the mock Retrospective to return the previous meta
  EXPECT_CALL(*mockRetrospective, getPreviousMeta(bunKey)).WillOnce(Return(bunMeta));

  // Call the method and get the result
  auto result = subject->getStickyBun(fake.program9_sequence0_pattern0_event0.id);

  // Check the result
  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(fake.program9_sequence0_pattern0_event0.id, result->eventId);
  ASSERT_EQ(bun.values, result->values);
}

/*
 Unit test behavior of choosing a different events for a series of X notes in a detail program
 <p>
 Sticky bun note choices should persist into following segments https://github.com/xjmusic/xjmusic/issues/281
*/
TEST_F(FabricatorTest, getStickyBun_createForEvent) {
  auto result = subject->getStickyBun(fake.program9_sequence0_pattern0_event0.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(fake.program9_sequence0_pattern0_event0.id, result.value().eventId);
}

/*
 Unit test behavior of choosing an event for a note in a detail program
 <p>
 Sticky bun note choices should persist into following segments https://github.com/xjmusic/xjmusic/issues/281
*/
TEST_F(FabricatorTest, GetStickyBun_MultipleEventsPickedSeparately) {
  // Create StickyBuns
  auto bun0 = StickyBun(fake.program9_sequence0_pattern0_event0.id, 3);
  auto bun1 = StickyBun(fake.program9_sequence0_pattern0_event1.id, 3);

  // Convert the StickyBuns to JSON strings
  auto bunJson0 = bun0.serialize();
  auto bunJson1 = bun1.serialize();

  // Compute the meta keys for the StickyBuns
  auto bunKey0 = StickyBun::computeMetaKey(fake.program9_sequence0_pattern0_event0.id);
  auto bunKey1 = StickyBun::computeMetaKey(fake.program9_sequence0_pattern0_event1.id);

  // Store the StickyBuns in the SegmentEntityStore
  store->put(SegmentFixtures::buildSegmentMeta(segment, bunKey0, bunJson0));
  store->put(SegmentFixtures::buildSegmentMeta(segment, bunKey1, bunJson1));

  // Call the method and get the results
  auto result0 = subject->getStickyBun(fake.program9_sequence0_pattern0_event0.id);
  auto result1 = subject->getStickyBun(fake.program9_sequence0_pattern0_event1.id);

  // Check the results
  ASSERT_TRUE(result0.has_value());
  ASSERT_EQ(fake.program9_sequence0_pattern0_event0.id, result0->eventId);
  ASSERT_EQ(bun0.values, result0->values);

  ASSERT_TRUE(result1.has_value());
  ASSERT_EQ(fake.program9_sequence0_pattern0_event1.id, result1->eventId);
  ASSERT_EQ(bun1.values, result1->values);
}


TEST_F(FabricatorTest, getMemeTaxonomy) {
  auto result = subject->getMemeTaxonomy();
  std::vector<MemeCategory> sortedCategories;
  for (const auto& category : result.getCategories()) {
    sortedCategories.emplace_back(category);
  }
  std::sort(sortedCategories.begin(), sortedCategories.end(), [](const MemeCategory& a, const MemeCategory& b) {
    return a.getName() < b.getName();
  });

  ASSERT_EQ(2, sortedCategories.size());
  ASSERT_EQ("COLOR", sortedCategories[0].getName());
  ASSERT_EQ("SEASON", sortedCategories[1].getName());
}


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