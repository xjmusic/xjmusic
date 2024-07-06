// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include <vector>

#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/fabricator/Fabricator.h"
#include "xjmusic/util/ValueUtils.h"

#include "../_helper/ContentFixtures.h"
#include "../_helper/SegmentFixtures.h"
#include "../_mock/MockSegmentRetrospective.h"


namespace XJ {
  class Fabricator;
  class ContentFixtures;
}
// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class FabricatorTest : public testing::Test {// NOLINT(*-pro-type-member-init)
protected:
  int SEQUENCE_TOTAL_BEATS = 64;
  std::unique_ptr<ContentEntityStore> sourceMaterial;
  std::unique_ptr<SegmentEntityStore> store;
  std::unique_ptr<MockSegmentRetrospective> mockRetrospective;
  Fabricator * subject = nullptr;
  std::unique_ptr<ContentFixtures> fake;
  const Segment *segment = nullptr;

protected:
  void SetUp() override {
    sourceMaterial = std::make_unique<ContentEntityStore>();
    store = std::make_unique<SegmentEntityStore>();
    fake = std::make_unique<ContentFixtures>();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake->setupFixtureB1(sourceMaterial.get());
    fake->setupFixtureB2(sourceMaterial.get());
    fake->setupFixtureB3(sourceMaterial.get());

    // Here's a basic setup that can be replaced for complex tests
    const auto chain = store->put(SegmentFixtures::buildChain(
        &fake->template1,
        "test",
        Chain::Type::Production,
        Chain::State::Fabricate));
    segment = store->put(SegmentFixtures::buildSegment(
        chain,
        2,
        Segment::State::Crafting,
        "G major",
        8,
        0.6f,
        240.0f,
        "seg123"));
    mockRetrospective = std::make_unique<MockSegmentRetrospective>(store.get(), 2);
    subject = new Fabricator(sourceMaterial.get(), store.get(), mockRetrospective.get(), 2, std::nullopt);
  }

  void TearDown() override {
    delete subject;
  }

  static bool setContains(const std::set<std::string> &items, const char *string) {
    return std::any_of(items.begin(), items.end(), [string](const std::string &item) {
      return item == string;
    });
  }
};


TEST_F(FabricatorTest, PickReturnedByPicks) {
  sourceMaterial->put(ContentFixtures::buildTemplateBinding(&fake->template1, &fake->library2));
  auto chain = store->put(
      SegmentFixtures::buildChain(&fake->template1, "test", Chain::Type::Production, Chain::State::Fabricate));
  store->put(SegmentFixtures::buildSegment(chain, 1, Segment::State::Crafted, "F major", 8, 0.6f, 120.0f, "seg123"));
  segment = store->put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));
  store->put(SegmentFixtures::buildSegmentChord(segment, 0.0f, "A"));
  store->put(
      SegmentFixtures::buildSegmentChoice(segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED,
                                          &fake->program5));
  auto beatChoice = store->put(
      SegmentFixtures::buildSegmentChoice(segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED,
                                          &fake->program35, &fake->program35_voice0, &fake->instrument8));
  auto beatArrangement = store->put(SegmentFixtures::buildSegmentChoiceArrangement(beatChoice));
  SegmentChoiceArrangementPick pick;
  pick.id = EntityUtils::computeUniqueId();
  pick.segmentId = beatArrangement->segmentId;
  pick.segmentChoiceArrangementId = beatArrangement->id;
  pick.programSequencePatternEventId = fake->program35_sequence0_pattern0_event0.id;
  pick.instrumentAudioId = fake->instrument8_audio8kick.id;
  pick.event = "CLANG";
  pick.startAtSegmentMicros = static_cast<long>(0.273 * static_cast<double>(ValueUtils::MICROS_PER_SECOND));
  pick.lengthMicros = static_cast<long>(1.571 * static_cast<double>(ValueUtils::MICROS_PER_SECOND));
  pick.amplitude = 0.8f;
  pick.tones = "A4";
  store->put(pick);

  std::set<const SegmentChoiceArrangementPick *> result = subject->getPicks();

  const SegmentChoiceArrangementPick *resultPick = *result.begin();
  ASSERT_EQ(beatArrangement->id, resultPick->segmentChoiceArrangementId);
  ASSERT_EQ(fake->instrument8_audio8kick.id, resultPick->instrumentAudioId);
  ASSERT_NEAR(0.273 * ValueUtils::MICROS_PER_SECOND, resultPick->startAtSegmentMicros, 0.001);
  ASSERT_NEAR(1.571 * ValueUtils::MICROS_PER_SECOND, resultPick->lengthMicros, 0.001);
  ASSERT_NEAR(0.8f, resultPick->amplitude, 0.1);
  ASSERT_EQ("A4", resultPick->tones);
}


TEST_F(FabricatorTest, GetDistinctChordVoicingTypes) {
  sourceMaterial->put(ContentFixtures::buildVoicing(
      &fake->program5_sequence0_chord0, &fake->program5_voiceSticky, "G4, B4, D4"));
  sourceMaterial->put(ContentFixtures::buildVoicing(
      &fake->program5_sequence0_chord0, &fake->program5_voiceStripe, "F5"));
  sourceMaterial->put(ContentFixtures::buildVoicing(
      &fake->program5_sequence0_chord0, &fake->program5_voicePad, "(None)"));// No voicing notes- doesn't count!

  // Create a chain
  store->put(SegmentFixtures::buildChain(
      &fake->template1, "test", Chain::Type::Production, Chain::State::Fabricate));

  // Create a segment choice
  store->put(SegmentFixtures::buildSegmentChoice(
      segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED, &fake->program5));

  // Get the result
  const std::set<Instrument::Type> result = subject->getDistinctChordVoicingTypes();

  // Check the result
  const std::set expected = {Instrument::Type::Bass, Instrument::Type::Sticky, Instrument::Type::Stripe};
  ASSERT_EQ(expected, result);
}

/**
 Choose next Macro program based on the memes of the last sequence from the previous Macro program https://github.com/xjmusic/xjmusic/issues/299
 */
TEST_F(FabricatorTest, GetType) {
  // Create a chain
  const auto chain = store->put(
      SegmentFixtures::buildChain(&fake->template1, "test", Chain::Type::Production, Chain::State::Fabricate));

  // Create previous segments with different choices
  const Segment *previousSegment = store->put(
      SegmentFixtures::buildSegment(chain, 1, Segment::State::Crafted, "F major", 8, 0.6f, 120.0f, "seg123"));
  store->put(SegmentFixtures::buildSegmentChoice(previousSegment, SegmentChoice::DELTA_UNLIMITED,
                                                 SegmentChoice::DELTA_UNLIMITED, &fake->program4,
                                                 &fake->program4_sequence1_binding0));
  store->put(SegmentFixtures::buildSegmentChoice(previousSegment, SegmentChoice::DELTA_UNLIMITED,
                                                 SegmentChoice::DELTA_UNLIMITED, &fake->program5,
                                                 &fake->program5_sequence1_binding0));

  // Create the current segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));

  // Set up the mock Retrospective to return the previous choices
  EXPECT_CALL(*mockRetrospective, getPreviousChoiceOfType(Program::Type::Main)).WillOnce(Return(std::nullopt));
  EXPECT_CALL(*mockRetrospective, getPreviousChoiceOfType(Program::Type::Macro)).WillOnce(Return(std::nullopt));

  // Get the result
  const auto result = subject->getType();

  // Check the result
  ASSERT_EQ(Segment::Type::NextMacro, result);
}


// FUTURE: test getChoicesOfPreviousSegments


TEST_F(FabricatorTest, GetMemeIsometryOfNextSequenceInPreviousMacro) {
  // Create a chain
  const auto chain = store->put(
      SegmentFixtures::buildChain(&fake->template1, "test", Chain::Type::Production, Chain::State::Fabricate));

  // Create previous segments with different choices
  const Segment *previousSegment = store->put(
      SegmentFixtures::buildSegment(chain, 1, Segment::State::Crafted, "F major", 8, 0.6f, 120.0f, "seg123"));
  auto previousMacroChoice = store->put(
      SegmentFixtures::buildSegmentChoice(previousSegment, SegmentChoice::DELTA_UNLIMITED,
                                          SegmentChoice::DELTA_UNLIMITED, &fake->program4,
                                          &fake->program4_sequence1_binding0));
  store->put(SegmentFixtures::buildSegmentChoice(previousSegment, SegmentChoice::DELTA_UNLIMITED,
                                                 SegmentChoice::DELTA_UNLIMITED, &fake->program5,
                                                 &fake->program5_sequence1_binding0));

  // Create the current segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));

  // Set up the mock Retrospective to return the previous choices
  EXPECT_CALL(*mockRetrospective, getPreviousChoiceOfType(Program::Type::Macro)).WillOnce(Return(std::optional(previousMacroChoice)));

  // Get the result
  auto result = subject->getMemeIsometryOfNextSequenceInPreviousMacro();
  ASSERT_EQ(2, result.getSources().size());
  ASSERT_TRUE(setContains(result.getSources(), "COZY"));
  ASSERT_TRUE(setContains(result.getSources(), "TROPICAL"));
}


TEST_F(FabricatorTest, GetChordAt) {
  // Create a chain
  const auto chain = store->put(
      SegmentFixtures::buildChain(&fake->template1, "test", Chain::Type::Production, Chain::State::Fabricate));

  // Create a segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));

  // Add chords to the subject
  subject->put(SegmentFixtures::buildSegmentChord(segment, 0.0f, "C"));
  subject->put(SegmentFixtures::buildSegmentChord(segment, 2.0f, "F"));
  subject->put(SegmentFixtures::buildSegmentChord(segment, 5.5f, "Gm"));

  // Check the chords at different times
  ASSERT_EQ("C", subject->getChordAt(0.0).value()->name);
  ASSERT_EQ("C", subject->getChordAt(1.0).value()->name);
  ASSERT_EQ("F", subject->getChordAt(2.0).value()->name);
  ASSERT_EQ("F", subject->getChordAt(3.0).value()->name);
  ASSERT_EQ("F", subject->getChordAt(5.0).value()->name);
  ASSERT_EQ("Gm", subject->getChordAt(5.5).value()->name);
  ASSERT_EQ("Gm", subject->getChordAt(6.0).value()->name);
  ASSERT_EQ("Gm", subject->getChordAt(7.5).value()->name);
}


TEST_F(FabricatorTest, ComputeProgramRange) {
  // Create a chain
  auto chain = store->put(
      SegmentFixtures::buildChain(&fake->template1, "test", Chain::Type::Production, Chain::State::Fabricate));

  // Create a segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));

  // Create a program
  auto program = ContentFixtures::buildProgram(Program::Type::Detail, "C", 120.0f);

  // Create a voice
  auto voice = ContentFixtures::buildVoice(&program, Instrument::Type::Bass);

  // Create a track
  auto track = ContentFixtures::buildTrack(&voice);

  // Create a sequence
  auto sequence = ContentFixtures::buildSequence(&program, 4);

  // Create a pattern
  auto pattern = ContentFixtures::buildPattern(&sequence, &voice, 4);

  // Add entities to sourceMaterial
  sourceMaterial->put(program);
  sourceMaterial->put(voice);
  sourceMaterial->put(track);
  sourceMaterial->put(sequence);
  sourceMaterial->put(pattern);
  sourceMaterial->put(fake->template1);
  sourceMaterial->put(fake->templateBinding1);
  sourceMaterial->put(ContentFixtures::buildEvent(&pattern, &track, 0.0f, 1.0f, "C1"));
  sourceMaterial->put(ContentFixtures::buildEvent(&pattern, &track, 1.0f, 1.0f, "D2"));

  // Get the result
  auto result = subject->getProgramRange(program.id, Instrument::Type::Bass);

  // Check the result
  ASSERT_EQ(Note::of("C1"), result.low.value());
  ASSERT_EQ(Note::of("D2"), result.high.value());
}


TEST_F(FabricatorTest, ComputeProgramRange_IgnoresAtonalNotes) {
  // Create a chain
  auto chain = store->put(
      SegmentFixtures::buildChain(&fake->template1, "test", Chain::Type::Production, Chain::State::Fabricate));

  // Create a segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, 2, Segment::State::Crafting, "G major", 8, 0.6f, 240.0f, "seg123"));

  // Create a program
  auto program = ContentFixtures::buildProgram(Program::Type::Detail, "C", 120.0f);

  // Create a voice
  auto voice = ContentFixtures::buildVoice(&program, Instrument::Type::Bass);

  // Create a track
  auto track = ContentFixtures::buildTrack(&voice);

  // Create a sequence
  auto sequence = ContentFixtures::buildSequence(&program, 4);

  // Create a pattern
  auto pattern = ContentFixtures::buildPattern(&sequence, &voice, 4);

  // Add entities to sourceMaterial
  sourceMaterial->put(program);
  sourceMaterial->put(voice);
  sourceMaterial->put(track);
  sourceMaterial->put(sequence);
  sourceMaterial->put(pattern);
  sourceMaterial->put(fake->template1);
  sourceMaterial->put(fake->templateBinding1);
  sourceMaterial->put(ContentFixtures::buildEvent(&pattern, &track, 0.0f, 1.0f, "C1"));
  sourceMaterial->put(ContentFixtures::buildEvent(&pattern, &track, 1.0f, 1.0f, "X"));
  sourceMaterial->put(ContentFixtures::buildEvent(&pattern, &track, 2.0f, 1.0f, "D2"));

  // Get the result
  auto result = subject->getProgramRange(program.id, Instrument::Type::Bass);

  // Check the result
  ASSERT_EQ(Note::of("C1"), result.low.value());
  ASSERT_EQ(Note::of("D2"), result.high.value());
}


TEST_F(FabricatorTest, GetProgramSequence_FromSequence) {
  // Create a project
  const auto project1 = ContentFixtures::buildProject("fish");

  // Create a template
  const Template template1 = ContentFixtures::buildTemplate(&project1, "Test Template 1", "test1");

  // Create a chain
  auto chain = store->put(SegmentFixtures::buildChain(&template1));

  // Create a segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, Segment::Type::Continue, 17, 4, Segment::State::Crafted, "D major",
                                    SEQUENCE_TOTAL_BEATS, 0.73f, 120.0f,
                                    "chains-" + ChainUtils::getIdentifier(chain) + "-segments-" + std::to_string(17),
                                    true));

  // Create a segment choice
  const auto choice = store->put(
      SegmentFixtures::buildSegmentChoice(segment, Program::Type::Main, &fake->program5_sequence0));

  // Get the result
  const auto result = subject->getProgramSequence(choice);

  // Check the result
  ASSERT_EQ(fake->program5_sequence0.id, result.value()->id);
}


TEST_F(FabricatorTest, GetProgramSequence_FromSequenceBinding) {
  // Create a project
  const auto project1 = ContentFixtures::buildProject("fish");

  // Create a template
  const Template template1 = ContentFixtures::buildTemplate(&project1, "Test Template 1", "test1");

  // Create a chain
  auto chain = store->put(SegmentFixtures::buildChain(&template1));

  // Create a segment
  segment = store->put(
      SegmentFixtures::buildSegment(chain, Segment::Type::Continue, 17, 4, Segment::State::Crafted, "D major",
                                    SEQUENCE_TOTAL_BEATS, 0.73f, 120.0f,
                                    "chains-" + ChainUtils::getIdentifier(chain) + "-segments-" + std::to_string(17),
                                    true));

  // Create a segment choice
  const auto choice = store->put(
      SegmentFixtures::buildSegmentChoice(segment, Program::Type::Main, &fake->program5_sequence0_binding0));

  // Get the result
  const auto result = subject->getProgramSequence(choice);

  // Check the result
  ASSERT_EQ(fake->program5_sequence0.id, result.value()->id);
}

/**
 Sticky buns v2 use slash root when available https://github.com/xjmusic/xjmusic/issues/231
 */
TEST_F(FabricatorTest, GetRootNote) {
  // Call the method and get the result
  const auto chord = Chord::of("Cm");
  const std::optional<Note> result = subject->getRootNoteMidRange("C3,E3,G3,A#3,C4,E4,G4", &chord);

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
      SegmentFixtures::buildSegmentChoice(segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED,
                                          &fake->program9,
                                          &fake->program9_voice0, &fake->instrument8),
      false);
  subject->put(
      SegmentFixtures::buildSegmentChoice(segment, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED,
                                          &fake->program4,
                                          &fake->program4_sequence1_binding0),
      false);

  // Get the result
  const auto resultMemes = store->readAllSegmentMemes(segment->id);

  std::vector<SegmentMeme> sortedResultMemes;
  sortedResultMemes.reserve(resultMemes.size());
  for (const SegmentMeme *pointer: resultMemes) {
    sortedResultMemes.push_back(*pointer);
  }
  std::sort(sortedResultMemes.begin(), sortedResultMemes.end(), [](const SegmentMeme &a, const SegmentMeme &b) {
    return a.name < b.name;
  });

  // Check the result
  ASSERT_EQ("BASIC", sortedResultMemes[0].name);
  ASSERT_EQ("COZY", sortedResultMemes[1].name);
  ASSERT_EQ("HEAVY", sortedResultMemes[2].name);
  ASSERT_EQ("TROPICAL", sortedResultMemes[3].name);
  ASSERT_EQ("WILD", sortedResultMemes[4].name);

  const auto resultChoices = store->readAllSegmentChoices(segment->id);
  std::vector<const SegmentChoice *> sortedResultChoices;
  sortedResultChoices.reserve(resultChoices.size());
  for (const SegmentChoice *pointer: resultChoices) {
    sortedResultChoices.emplace_back(pointer);
  }
  std::sort(sortedResultChoices.begin(), sortedResultChoices.end(), [](const SegmentChoice *a, const SegmentChoice *b) {
    return a->programType < b->programType;
  });

  ASSERT_EQ(fake->program4.id, sortedResultChoices[0]->programId);
  ASSERT_EQ(fake->program4_sequence1_binding0.id, sortedResultChoices[0]->programSequenceBindingId);
  ASSERT_EQ(fake->instrument8.id, sortedResultChoices[1]->instrumentId);
}


/*
 Unit test behavior of choosing an event for a note in a detail program
 <p>
 Sticky bun note choices should persist into following segments https://github.com/xjmusic/xjmusic/issues/281
*/
TEST_F(FabricatorTest, GetStickyBun_ReadMetaFromCurrentSegment) {
  auto bun = StickyBun(fake->program9_sequence0_pattern0_event0.id, 3);
  const auto bunJson = bun.serialize();
  const auto bunKey = StickyBun::computeMetaKey(fake->program9_sequence0_pattern0_event0.id);
  store->put(SegmentFixtures::buildSegmentMeta(segment, bunKey, bunJson));

  auto const result = subject->getStickyBun(fake->program9_sequence0_pattern0_event0.id);

  // Check the result
  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(fake->program9_sequence0_pattern0_event0.id, result.value().eventId);
  ASSERT_EQ(bun.values, result.value().values);
}

/*
 Unit test behavior of choosing an event for a note in a detail program
 <p>
 Sticky bun note choices should persist into following segments https://github.com/xjmusic/xjmusic/issues/281
*/
TEST_F(FabricatorTest, GetStickyBun_ReadMetaFromPreviousSegment) {
  // Create a StickyBun
  auto bun = StickyBun(fake->program9_sequence0_pattern0_event0.id, 3);

  // Convert the StickyBun to a JSON string
  const auto bunJson = bun.serialize();

  // Compute the meta key for the StickyBun
  const auto bunKey = StickyBun::computeMetaKey(fake->program9_sequence0_pattern0_event0.id);

  // Compute a StickyBun but don't put it in the SegmentEntityStore-- we'll inject it through the mock retrospective
  auto bunMeta = SegmentFixtures::buildSegmentMeta(segment, bunKey, bunJson);

  // Set up the mock Retrospective to return the previous meta
  SegmentMeta *bunMetaPtr = &bunMeta;
  EXPECT_CALL(*mockRetrospective, getPreviousMeta(bunKey)).WillOnce(Return(std::optional(bunMetaPtr)));

  // Call the method and get the result
  const auto result = subject->getStickyBun(fake->program9_sequence0_pattern0_event0.id);

  // Check the result
  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(fake->program9_sequence0_pattern0_event0.id, result.value().eventId);
  ASSERT_EQ(bun.values, result.value().values);
}

/*
 Unit test behavior of choosing a different events for a series of X notes in a detail program
 <p>
 Sticky bun note choices should persist into following segments https://github.com/xjmusic/xjmusic/issues/281
*/
TEST_F(FabricatorTest, getStickyBun_createForEvent) {
  EXPECT_CALL(*mockRetrospective, getPreviousMeta(_)).WillOnce(Return(std::nullopt));

  const auto result = subject->getStickyBun(fake->program9_sequence0_pattern0_event0.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(fake->program9_sequence0_pattern0_event0.id, result.value().eventId);
}

/*
 Unit test behavior of choosing an event for a note in a detail program
 <p>
 Sticky bun note choices should persist into following segments https://github.com/xjmusic/xjmusic/issues/281
*/
TEST_F(FabricatorTest, GetStickyBun_MultipleEventsPickedSeparately) {
  // Create StickyBuns
  auto bun0 = StickyBun(fake->program9_sequence0_pattern0_event0.id, 3);
  auto bun1 = StickyBun(fake->program9_sequence0_pattern0_event1.id, 3);

  // Convert the StickyBuns to JSON strings
  auto bunJson0 = bun0.serialize();
  auto bunJson1 = bun1.serialize();

  // Compute the meta keys for the StickyBuns
  auto bunKey0 = StickyBun::computeMetaKey(fake->program9_sequence0_pattern0_event0.id);
  auto bunKey1 = StickyBun::computeMetaKey(fake->program9_sequence0_pattern0_event1.id);

  // Store the StickyBuns in the SegmentEntityStore
  store->put(SegmentFixtures::buildSegmentMeta(segment, bunKey0, bunJson0));
  store->put(SegmentFixtures::buildSegmentMeta(segment, bunKey1, bunJson1));

  // Call the method and get the results
  auto result0 = subject->getStickyBun(fake->program9_sequence0_pattern0_event0.id);
  auto result1 = subject->getStickyBun(fake->program9_sequence0_pattern0_event1.id);

  // Check the results
  ASSERT_TRUE(result0.has_value());
  ASSERT_EQ(fake->program9_sequence0_pattern0_event0.id, result0.value().eventId);
  ASSERT_EQ(bun0.values, result0.value().values);

  ASSERT_TRUE(result1.has_value());
  ASSERT_EQ(fake->program9_sequence0_pattern0_event1.id, result1.value().eventId);
  ASSERT_EQ(bun1.values, result1.value().values);
}


TEST_F(FabricatorTest, getMemeTaxonomy) {
  auto result = subject->getMemeTaxonomy();
  std::vector<MemeCategory> sortedCategories;
  for (const auto &category: result.getCategories()) {
    sortedCategories.emplace_back(category);
  }
  std::sort(sortedCategories.begin(), sortedCategories.end(), [](const MemeCategory &a, const MemeCategory &b) {
    return a.getName() < b.getName();
  });

  ASSERT_EQ(2, sortedCategories.size());
  ASSERT_EQ("COLOR", sortedCategories[0].getName());
  ASSERT_EQ("SEASON", sortedCategories[1].getName());
}


// Test for getSegmentId
TEST_F(FabricatorTest, GetSegmentId) {
  SegmentChoice segmentChoice;
  segmentChoice.segmentId = segment->id;
  SegmentChoiceArrangement segmentChoiceArrangement;
  segmentChoiceArrangement.segmentId = segment->id;
  SegmentChoiceArrangementPick segmentChoiceArrangementPick;
  segmentChoiceArrangementPick.segmentId = segment->id;
  SegmentChord segmentChord;
  segmentChord.segmentId = segment->id;
  SegmentChordVoicing segmentChordVoicing;
  segmentChordVoicing.segmentId = segment->id;
  SegmentMeme segmentMeme;
  segmentMeme.segmentId = segment->id;
  SegmentMessage segmentMessage;
  segmentMessage.segmentId = segment->id;
  SegmentMeta segmentMeta;
  segmentMeta.segmentId = segment->id;

  ASSERT_EQ(segment->id, Fabricator::getSegmentId(&segmentChoice));
  ASSERT_EQ(segment->id, Fabricator::getSegmentId(&segmentChoiceArrangement));
  ASSERT_EQ(segment->id, Fabricator::getSegmentId(&segmentChoiceArrangementPick));
  ASSERT_EQ(segment->id, Fabricator::getSegmentId(&segmentChord));
  ASSERT_EQ(segment->id, Fabricator::getSegmentId(&segmentChordVoicing));
  ASSERT_EQ(segment->id, Fabricator::getSegmentId(&segmentMeme));
  ASSERT_EQ(segment->id, Fabricator::getSegmentId(&segmentMessage));
  ASSERT_EQ(segment->id, Fabricator::getSegmentId(&segmentMeta));
}