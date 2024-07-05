// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include <utility>
#include <vector>


#include "../_helper/ContentFixtures.h"
#include "../_helper/SegmentFixtures.h"
#include "../_helper/YamlTest.h"
#include "../_mock/MockFabricator.h"
#include "../_mock/MockSegmentRetrospective.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/fabricator/ChainUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class CraftTest : public YamlTest {// NOLINT(*-pro-type-member-init)
protected:
  int TEST_REPEAT_TIMES = 20;
  MockFabricator *mockFabricator = nullptr;
  SegmentEntityStore *segmentEntityStore = nullptr;
  ContentEntityStore *sourceMaterial = nullptr;
  MockSegmentRetrospective *mockSegmentRetrospective = nullptr;
  Craft *subject = nullptr;
  const Segment *segment0 = nullptr;
  Program *program1 = nullptr;
  TemplateConfig templateConfig{};

  void SetUp() override {
    sourceMaterial = new ContentEntityStore();
    segmentEntityStore = new SegmentEntityStore();
    const Project *project1 = sourceMaterial->put(ContentFixtures::buildProject("fish"));
    const Library *library1 = sourceMaterial->put(ContentFixtures::buildLibrary(project1, "sea"));
    program1 = sourceMaterial->put(
        ContentFixtures::buildProgram(library1, Program::Type::Detail, Program::State::Published, "swimming",
                                      "C", 120.0f));
    const Template *template1 = sourceMaterial->put(
        ContentFixtures::buildTemplate(project1, "Test Template 1", "test1"));
    // Chain "Test Print #1" is fabricating segments
    const Chain *chain1 = segmentEntityStore->put(
        SegmentFixtures::buildChain("Test Print #1", Chain::Type::Production, Chain::State::Fabricate,
                                    template1));

    segment0 = segmentEntityStore->put(
        SegmentFixtures::buildSegment(chain1, Segment::Type::Initial, 2, 128, Segment::State::Crafted, "D major",
                                      64, 0.73f, 120.0f, "chains-1-segments-9f7s89d8a7892", true));

    templateConfig = template1->config;
    mockSegmentRetrospective = new MockSegmentRetrospective(segmentEntityStore, 2);
    mockFabricator = new MockFabricator(
        sourceMaterial,
        segmentEntityStore,
        mockSegmentRetrospective,
        2,
        std::nullopt);
    subject = new Craft(mockFabricator);
  }

  void TearDown() override {
    delete sourceMaterial;
    delete segmentEntityStore;
    delete mockFabricator;
    delete mockSegmentRetrospective;
    delete subject;
  }

  /**
   Do the subroutine of testing the new chord part instrument audio selection
  
   @param expectThis chord name
   @param notThat    chord name
   @param match      chord name
   */
  void selectNewChordPartInstrumentAudio(std::string expectThis, std::string notThat, const std::string &match) const {
    Project project1 = ContentFixtures::buildProject("testing");
    Library library1 = ContentFixtures::buildLibrary(&project1, "leaves");
    Instrument instrument1 = ContentFixtures::buildInstrument(&library1, Instrument::Type::Percussion,
                                                              Instrument::Mode::Chord, Instrument::State::Published,
                                                              "Test chord audio");
    InstrumentAudio instrument1audio1 = ContentFixtures::buildInstrumentAudio(&instrument1, "ping", "70bpm.wav", 0.01f,
                                                                              2.123f, 120.0f, 0.62f, "PRIMARY",
                                                                              std::move(expectThis), 1.0f);
    InstrumentAudio instrument1audio2 = ContentFixtures::buildInstrumentAudio(&instrument1, "ping", "70bpm.wav", 0.01f,
                                                                              2.123f, 120.0f, 0.62f, "PRIMARY",
                                                                              std::move(notThat), 1.0f);
    sourceMaterial->put(instrument1);
    sourceMaterial->put(instrument1audio1);
    sourceMaterial->put(instrument1audio2);

    for (auto i = 0; i < TEST_REPEAT_TIMES; i++) {
      auto result = subject->selectNewChordPartInstrumentAudio(&instrument1, Chord::of(match));

      EXPECT_TRUE(result.has_value());
      EXPECT_EQ(instrument1audio1.id, result.value()->id);
    }
  }
};

TEST_F(CraftTest, PrecomputeDeltas) {
  auto choiceIndexProvider = Craft::LambdaChoiceIndexProvider(
      [](const SegmentChoice &choice) -> std::string {
        return Instrument::toString(choice.instrumentType);
      });
  auto choiceFilter = [](const SegmentChoice *choice) {
    return choice->programType == Program::Type::Detail;
  };
  std::vector<std::string> detailLayerOrder;
  detailLayerOrder.reserve(templateConfig.detailLayerOrder.size());
  for (const auto &type: templateConfig.detailLayerOrder) {
    detailLayerOrder.push_back(Instrument::toString(type));
  }

  subject->precomputeDeltas(choiceFilter, choiceIndexProvider, detailLayerOrder, {}, 1);
}

TEST_F(CraftTest, IsIntroSegment) {
  EXPECT_CALL(*mockFabricator, getSegment()).WillRepeatedly(Return(segment0));
  auto sc0 = SegmentFixtures::buildSegmentChoice(segment0, 132, 200, program1);
  auto sc1 = SegmentFixtures::buildSegmentChoice(segment0, 110, 200, program1);
  auto sc2 = SegmentFixtures::buildSegmentChoice(segment0, 200, 250, program1);

  EXPECT_TRUE(subject->isIntroSegment(&sc0));
  EXPECT_FALSE(subject->isIntroSegment(&sc1));
  EXPECT_FALSE(subject->isIntroSegment(&sc2));
}

TEST_F(CraftTest, InBounds) {
  EXPECT_FALSE(Craft::inBounds(SegmentChoice::DELTA_UNLIMITED, 17, 19));
  EXPECT_FALSE(Craft::inBounds(4, SegmentChoice::DELTA_UNLIMITED, 2));
  EXPECT_FALSE(Craft::inBounds(4, 17, 19));
  EXPECT_FALSE(Craft::inBounds(4, 17, 2));
  EXPECT_TRUE(Craft::inBounds(SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED, 799));
  EXPECT_TRUE(Craft::inBounds(SegmentChoice::DELTA_UNLIMITED, 17, 2));
  EXPECT_TRUE(Craft::inBounds(4, SegmentChoice::DELTA_UNLIMITED, 19));
  EXPECT_TRUE(Craft::inBounds(4, 17, 12));
  EXPECT_TRUE(Craft::inBounds(4, 17, 17));
  EXPECT_TRUE(Craft::inBounds(4, 17, 4));
}

TEST_F(CraftTest, IsOutroSegment) {
  EXPECT_CALL(*mockFabricator, getSegment()).WillRepeatedly(Return(segment0));
  SegmentChoice input;

  input = SegmentFixtures::buildSegmentChoice(segment0, 20, 130, program1);
  EXPECT_TRUE(subject->isOutroSegment(&input));
  input = SegmentFixtures::buildSegmentChoice(segment0, 20, 100, program1);
  EXPECT_FALSE(subject->isOutroSegment(&input));
  input = SegmentFixtures::buildSegmentChoice(segment0, 20, 250, program1);
  EXPECT_FALSE(subject->isOutroSegment(&input));
}

TEST_F(CraftTest, IsSilentEntireSegment) {
  EXPECT_CALL(*mockFabricator, getSegment()).WillRepeatedly(Return(segment0));
  SegmentChoice input;

  input = SegmentFixtures::buildSegmentChoice(segment0, 12, 25, program1);
  EXPECT_TRUE(subject->isSilentEntireSegment(&input));
  input = SegmentFixtures::buildSegmentChoice(segment0, 200, 225, program1);
  EXPECT_TRUE(subject->isSilentEntireSegment(&input));
  input = SegmentFixtures::buildSegmentChoice(segment0, 50, 136, program1);
  EXPECT_FALSE(subject->isSilentEntireSegment(&input));
  input = SegmentFixtures::buildSegmentChoice(segment0, 150, 200, program1);
  EXPECT_FALSE(subject->isSilentEntireSegment(&input));
  input = SegmentFixtures::buildSegmentChoice(segment0, 130, 150, program1);
  EXPECT_FALSE(subject->isSilentEntireSegment(&input));
}

TEST_F(CraftTest, IsActiveEntireSegment) {
  EXPECT_CALL(*mockFabricator, getSegment()).WillRepeatedly(Return(segment0));
  SegmentChoice input;

  input = SegmentFixtures::buildSegmentChoice(segment0, 12, 25, program1);
  EXPECT_FALSE(subject->isActiveEntireSegment(&input));
  input = SegmentFixtures::buildSegmentChoice(segment0, 200, 225, program1);
  EXPECT_FALSE(subject->isActiveEntireSegment(&input));
  input = SegmentFixtures::buildSegmentChoice(segment0, 50, 136, program1);
  EXPECT_FALSE(subject->isActiveEntireSegment(&input));
  input = SegmentFixtures::buildSegmentChoice(segment0, 150, 200, program1);
  EXPECT_FALSE(subject->isActiveEntireSegment(&input));
  input = SegmentFixtures::buildSegmentChoice(segment0, 130, 150, program1);
  EXPECT_FALSE(subject->isActiveEntireSegment(&input));
  input = SegmentFixtures::buildSegmentChoice(segment0, 126, 195, program1);
  EXPECT_TRUE(subject->isActiveEntireSegment(&input));
}

TEST_F(CraftTest, IsUnlimitedIn) {
  EXPECT_TRUE(Craft::isUnlimitedIn(
      SegmentFixtures::buildSegmentChoice(segment0, SegmentChoice::DELTA_UNLIMITED, 25, program1)));
  EXPECT_FALSE(Craft::isUnlimitedIn(
      SegmentFixtures::buildSegmentChoice(segment0, 25, SegmentChoice::DELTA_UNLIMITED, program1)));
}

TEST_F(CraftTest, IsUnlimitedOut) {
  EXPECT_FALSE(Craft::isUnlimitedOut(
      SegmentFixtures::buildSegmentChoice(segment0, SegmentChoice::DELTA_UNLIMITED, 25, program1)));
  EXPECT_TRUE(Craft::isUnlimitedOut(
      SegmentFixtures::buildSegmentChoice(segment0, 25, SegmentChoice::DELTA_UNLIMITED, program1)));
}

/**
 PercLoops are not adhering to "__BPM" memes
 https://github.com/xjmusic/xjmusic/issues/296
 */
TEST_F(CraftTest, ChooseFreshInstrumentAudio) {
  EXPECT_CALL(*mockFabricator, getSourceMaterial()).WillRepeatedly(Return(sourceMaterial));
  EXPECT_CALL(*mockFabricator, getRetrospective()).WillRepeatedly(Return(mockSegmentRetrospective));
  EXPECT_CALL(*mockFabricator, getSegment()).WillRepeatedly(Return(segment0));
  const Project *project1 = sourceMaterial->put(ContentFixtures::buildProject("testing"));
  const Library *library1 = sourceMaterial->put(ContentFixtures::buildLibrary(project1, "leaves"));
  const Instrument *instrument1 = sourceMaterial->put(
      ContentFixtures::buildInstrument(library1, Instrument::Type::Percussion, Instrument::Mode::Event,
                                       Instrument::State::Published, "Loop 75 beats per minute"));
  sourceMaterial->put(ContentFixtures::buildInstrumentMeme(instrument1, "70BPM"));
  InstrumentAudio *instrument1audio = sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument1, "slow loop", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.62f,
                                            "PRIMARY", "X", 1.0f));
  const Instrument *instrument2 = sourceMaterial->put(
      ContentFixtures::buildInstrument(library1, Instrument::Type::Percussion, Instrument::Mode::Event,
                                       Instrument::State::Published, "Loop 85 beats per minute"));
  sourceMaterial->put(ContentFixtures::buildInstrumentMeme(instrument2, "90BPM"));
  sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument2, "fast loop", "90bpm.wav", 0.01f, 2.123f, 120.0f, 0.62f,
                                            "SECONDARY", "X", 1.0f));

  // Mock the methods
  EXPECT_CALL(*mockFabricator, getMemeIsometryOfSegment())
      .WillOnce(Return(MemeIsometry::of(MemeTaxonomy::empty(), {"70BPM"})));

  // Call the method under test
  const auto result = subject->chooseFreshInstrumentAudio({Instrument::Type::Percussion}, {Instrument::Mode::Event},
                                                          {instrument1audio->instrumentId}, {"PRIMARY"});

  // Check the result
  EXPECT_TRUE(result.has_value());
}

/**
 XJ Should choose the correct chord audio per Main Program chord https://github.com/xjmusic/xjmusic/issues/237
 */
TEST_F(CraftTest, selectNewChordPartInstrumentAudio_stripSpaces) {
  EXPECT_CALL(*mockFabricator, getSourceMaterial()).WillRepeatedly(Return(sourceMaterial));
  EXPECT_CALL(*mockFabricator, getRetrospective()).WillRepeatedly(Return(mockSegmentRetrospective));
  EXPECT_CALL(*mockFabricator, getSegment()).WillRepeatedly(Return(segment0));
  selectNewChordPartInstrumentAudio(" G   major  ", "G-7", " G    major    ");
}

/**
 Chord-mode Instrument: Slash Chord Fluency
 https://github.com/xjmusic/xjmusic/issues/227
 When the exact match is not present for an entire slash chord name, choose a chord matching the pre-slash name
 */
TEST_F(CraftTest, selectNewChordPartInstrumentAudio_slashChordFluency) {
  EXPECT_CALL(*mockFabricator, getSourceMaterial()).WillRepeatedly(Return(sourceMaterial));
  EXPECT_CALL(*mockFabricator, getRetrospective()).WillRepeatedly(Return(mockSegmentRetrospective));
  EXPECT_CALL(*mockFabricator, getSegment()).WillRepeatedly(Return(segment0));
  selectNewChordPartInstrumentAudio("Ab/C", "Eb/G", "Ab");
  selectNewChordPartInstrumentAudio("Ab", "Eb/G", "Ab/C");
}

/**
 Enhanced Synonymous Chord recognition https://github.com/xjmusic/xjmusic/issues/236
 */
TEST_F(CraftTest, selectNewChordPartInstrumentAudio_chordSynonyms) {
  EXPECT_CALL(*mockFabricator, getSourceMaterial()).WillRepeatedly(Return(sourceMaterial));
  EXPECT_CALL(*mockFabricator, getRetrospective()).WillRepeatedly(Return(mockSegmentRetrospective));
  EXPECT_CALL(*mockFabricator, getSegment()).WillRepeatedly(Return(segment0));
  selectNewChordPartInstrumentAudio("CMadd9", "Cm6", "C add9");
}

TEST_F(CraftTest, SelectGeneralAudioIntensityLayers_ThreeLayers) {
  const Project *project1 = sourceMaterial->put(ContentFixtures::buildProject("testing"));
  const Library *library1 = sourceMaterial->put(ContentFixtures::buildLibrary(project1, "leaves"));
  const Instrument *instrument1 = sourceMaterial->put(
      ContentFixtures::buildInstrument(library1, Instrument::Type::Percussion, Instrument::Mode::Loop,
                                       Instrument::State::Published, "Test loop audio"));
  //Should pick one of these two at intensity 0.2
  const InstrumentAudio *instrument1audio1a = sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.2f, "PERC", "X",
                                            1.0f));
  const InstrumentAudio *instrument1audio1b = sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.2f, "PERC", "X",
                                            1.0f));
  //Should pick one of these two at intensity 0.5
  const InstrumentAudio *instrument1audio2a = sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.5f, "PERC", "X",
                                            1.0f));
  const InstrumentAudio *instrument1audio2b = sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.5f, "PERC", "X",
                                            1.0f));
  //Should pick one of these two at intensity 0.8
  const InstrumentAudio *instrument1audio3a = sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.8f, "PERC", "X",
                                            1.0f));
  const InstrumentAudio *instrument1audio3b = sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.8f, "PERC", "X",
                                            1.0f));

  // Mocks
  EXPECT_CALL(*mockFabricator, getSourceMaterial()).WillRepeatedly(Return(sourceMaterial));
  EXPECT_CALL(*mockFabricator, getRetrospective()).WillRepeatedly(Return(mockSegmentRetrospective));
  EXPECT_CALL(*mockFabricator, getSegment()).WillRepeatedly(Return(segment0));
  EXPECT_CALL(*mockSegmentRetrospective, getPreviousPicksForInstrument(instrument1->id)).WillOnce(
      Return(std::set<const SegmentChoiceArrangementPick *>{}));

  // Call the method under test
  auto result = subject->selectGeneralAudioIntensityLayers(instrument1);

  // Sort the result
  std::vector resultVector(result.begin(), result.end());
  std::sort(resultVector.begin(), resultVector.end(), [](const InstrumentAudio *a, const InstrumentAudio *b) {
    return a->intensity < b->intensity;
  });

  // Check the result
  EXPECT_EQ(3, resultVector.size());
  EXPECT_TRUE(resultVector[0]->id == instrument1audio1a->id || resultVector[0]->id == instrument1audio1b->id);
  EXPECT_TRUE(resultVector[1]->id == instrument1audio2a->id || resultVector[1]->id == instrument1audio2b->id);
  EXPECT_TRUE(resultVector[2]->id == instrument1audio3a->id || resultVector[2]->id == instrument1audio3b->id);
}

/**
 * SelectGeneralAudioIntensityLayers should continue the audio picks of the previous segment if the instrument is configured to do so
 */
TEST_F(CraftTest, SelectGeneralAudioIntensityLayers_ContinueSegment) {
  Project project1 = ContentFixtures::buildProject("testing");
  Library library1 = ContentFixtures::buildLibrary(&project1, "leaves");
  Instrument *instrument1 = sourceMaterial->put(
      ContentFixtures::buildInstrument(&library1, Instrument::Type::Percussion, Instrument::Mode::Loop,
                                       Instrument::State::Published, "Test loop audio"));
  instrument1->config = InstrumentConfig("isAudioSelectionPersistent=true");
  InstrumentAudio *instrument1audio1a = sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.2f, "PERC", "X",
                                            1.0f));
  sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.2f, "PERC", "X",
                                            1.0f));
  InstrumentAudio *instrument1audio2a = sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.5f, "PERC", "X",
                                            1.0f));
  sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.5f, "PERC", "X",
                                            1.0f));
  InstrumentAudio *instrument1audio3a = sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.8f, "PERC", "X",
                                            1.0f));
  sourceMaterial->put(
      ContentFixtures::buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.8f, "PERC", "X",
                                            1.0f));
  SegmentChoice choice = SegmentFixtures::buildSegmentChoice(segment0, instrument1);
  SegmentChoiceArrangement arrangement = SegmentFixtures::buildSegmentChoiceArrangement(&choice);
  SegmentChoiceArrangementPick pick1 = SegmentFixtures::buildSegmentChoiceArrangementPick(segment0, &arrangement,
                                                                                          instrument1audio1a,
                                                                                          instrument1audio1a->event);
  SegmentChoiceArrangementPick pick2 = SegmentFixtures::buildSegmentChoiceArrangementPick(segment0, &arrangement,
                                                                                          instrument1audio2a,
                                                                                          instrument1audio2a->event);
  SegmentChoiceArrangementPick pick3 = SegmentFixtures::buildSegmentChoiceArrangementPick(segment0, &arrangement,
                                                                                          instrument1audio3a,
                                                                                          instrument1audio3a->event);

  // Mock the methods
  EXPECT_CALL(*mockFabricator, getSourceMaterial()).WillRepeatedly(Return(sourceMaterial));
  EXPECT_CALL(*mockFabricator, getRetrospective()).WillRepeatedly(Return(mockSegmentRetrospective));
  EXPECT_CALL(*mockFabricator, getSegment()).WillRepeatedly(Return(segment0));
  EXPECT_CALL(*mockSegmentRetrospective, getPreviousPicksForInstrument(instrument1->id)).WillOnce(
      Return(std::set<const SegmentChoiceArrangementPick *>({&pick1, &pick2, &pick3})));

  // Call the method under test
  auto result = subject->selectGeneralAudioIntensityLayers(instrument1);

  // Sort the result
  std::vector<const InstrumentAudio *> resultVector(result.begin(), result.end());
  std::sort(resultVector.begin(), resultVector.end(), [](const InstrumentAudio *a, const InstrumentAudio *b) {
    return a->intensity < b->intensity;
  });

  // Check the result
  EXPECT_EQ(3, resultVector.size());
  EXPECT_EQ(instrument1audio1a->id, resultVector[0]->id);
  EXPECT_EQ(instrument1audio2a->id, resultVector[1]->id);
  EXPECT_EQ(instrument1audio3a->id, resultVector[2]->id);
}