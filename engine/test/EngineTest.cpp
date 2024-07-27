// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "./_helper/ContentFixtures.h"

#include "xjmusic/Engine.h"
#include "xjmusic/craft/Craft.h"
#include "xjmusic/fabricator/SegmentUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class XJEngineTest : public testing::Test {

protected:
  std::string ENGINE_TEST_PROJECT_PATH = "_data/test_project/TestProject.xj";
  int craftAheadSeconds = 15;
  int dubAheadSeconds = 30;
  int persistenceWindowSeconds = 60;
  int MARATHON_NUMBER_OF_SEGMENTS = 50;
  long MICROS_PER_CYCLE = 1000000;
  long long MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
  long long MILLIS_PER_SECOND = 1000;
  long long startTime = EntityUtils::currentTimeMillis();
  std::set<std::string> seenAudioSchedulingEventKeys;

  /**
   * Compute a key for the audio scheduling event
   * @param event  the audio scheduling event
   * @return  the key
   */
  static std::string computeKey(const unsigned long long atChainMicros, const AudioScheduleEvent &event) {
    return
        StringUtils::zeroPadded(atChainMicros, 12) + "_" + AudioScheduleEvent::toString(event.type) + "_" + event.audio.getId()
        + "_" + StringUtils::toProperSlug(event.audio.getAudio()->name)
        + "_startAt" + std::to_string(event.audio.getStartAtChainMicros())
        + (event.audio.getStopAtChainMicros().has_value()
           ? ("_stopAt" + std::to_string(event.audio.getStopAtChainMicros().value()))
           : "_stopNever")
        + "_fromVolume" + std::to_string(static_cast<int>(event.audio.getFromVolume() * 100))
        + "_toVolume" + std::to_string(static_cast<int>(event.audio.getToVolume() * 100));
  }

  void SetUp() override {
    seenAudioSchedulingEventKeys.clear();
  }
};

TEST_F(XJEngineTest, ReadsAndRunsProjectFromDisk) {
  std::unique_ptr<Engine> subject = std::make_unique<Engine>(
      ENGINE_TEST_PROJECT_PATH,
      Fabricator::ControlMode::Taxonomy,
      craftAheadSeconds,
      dubAheadSeconds,
      persistenceWindowSeconds);

  auto memeTaxonomy = subject->getMemeTaxonomy();
  ASSERT_TRUE(memeTaxonomy.has_value());
  auto categories = memeTaxonomy.value().getCategories();
  ASSERT_FALSE(categories.empty());
  ASSERT_EQ(categories.size(), 2);

  const auto tmpl = subject->getProjectContent()->getFirstTemplate();
  ASSERT_TRUE(tmpl.has_value());

  subject->start(tmpl.value()->id);
  unsigned long long atChainMicros = 0;
  while (atChainMicros < 60000000) {
    auto audios = subject->RunCycle(atChainMicros);
    for (const auto &audio: audios) {
      seenAudioSchedulingEventKeys.insert(computeKey(atChainMicros, audio));
      // check that the audio file exists
      ASSERT_TRUE(std::filesystem::exists(subject->getPathToBuildDirectory() / audio.audio.getAudio()->waveformKey));
    }
    std::cout << "Ran cycle at " << std::to_string(atChainMicros) << std::endl;
    atChainMicros += MICROS_PER_CYCLE;
    subject->finish(false);
  }

  // check that all the audio scheduling events were seen
  ASSERT_TRUE(seenAudioSchedulingEventKeys.size() > 10);
}

TEST_F(XJEngineTest, CorrectlyDubsAudio) {
  std::unique_ptr<Engine> subject = std::make_unique<Engine>(
      std::nullopt,
      Fabricator::ControlMode::Taxonomy,
      craftAheadSeconds,
      dubAheadSeconds,
      persistenceWindowSeconds);
  // Project, library, template
  auto pProject = subject->getProjectContent()->put(ContentFixtures::buildProject());
  auto pLibrary = subject->getProjectContent()->put(ContentFixtures::buildLibrary(pProject, "Test Library"));
  auto pTemplate = subject->getProjectContent()->put(ContentFixtures::buildTemplate(pProject, "Test Template"));
  subject->getProjectContent()->put(ContentFixtures::buildTemplateBinding(pTemplate, pLibrary));
  // Macro program
  auto pProgramMacro = subject->getProjectContent()->put(
      ContentFixtures::buildProgram(pLibrary, Program::Type::Macro, Program::State::Published, "Test Macro", "C", 60));
  auto pProgramMacroSequence = subject->getProjectContent()->put(
      ContentFixtures::buildProgramSequence(pProgramMacro, 4, "Test Macro Sequence", 1, "C"));
  subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceBinding(pProgramMacroSequence, 0));
  subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceBinding(pProgramMacroSequence, 1));
  // Main program
  auto pProgramMain = subject->getProjectContent()->put(
      ContentFixtures::buildProgram(pLibrary, Program::Type::Main, Program::State::Published, "Test Main", "C", 60));
  subject->getProjectContent()->put(ContentFixtures::buildProgramMeme(pProgramMain, "Apples"));
  auto pProgramMainSequence = subject->getProjectContent()->put(
      ContentFixtures::buildProgramSequence(pProgramMain, 4, "Test Main Sequence", 1, "C"));
  subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceChord(pProgramMainSequence, 0, "C"));
  subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceChord(pProgramMainSequence, 2, "G"));
  subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceBinding(pProgramMainSequence, 0));
  subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceBinding(pProgramMainSequence, 1));
  subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceBinding(pProgramMainSequence, 2));
  // Percussion Loop instrument
  auto pInstrumentPercussionLoop = subject->getProjectContent()->put(
      ContentFixtures::buildInstrument(
          pLibrary, Instrument::Type::Percussion, Instrument::Mode::Loop, Instrument::State::Published, "Perc Loop 1"));
  subject->getProjectContent()->put(
      ContentFixtures::buildInstrumentAudio(
          pInstrumentPercussionLoop, "Perc Loop 1 Audio", "perc-loop-1.wav", 0, 4, 60, 1, "X", "X", 1));
  // Pad Chord instrument
  auto pInstrumentPadChord = subject->getProjectContent()->put(
      ContentFixtures::buildInstrument(
          pLibrary, Instrument::Type::Pad, Instrument::Mode::Chord, Instrument::State::Published, "Pad Chord 1"));
  subject->getProjectContent()->put(
      ContentFixtures::buildInstrumentAudio(
          pInstrumentPadChord, "Pad Chord 1 C", "pad-chord-1.wav", 0, 4, 60, 1, "C", "C", 1));
  subject->getProjectContent()->put(
      ContentFixtures::buildInstrumentAudio(
          pInstrumentPadChord, "Pad Chord 1 F", "pad-chord-1.wav", 0, 4, 60, 1, "F", "F", 1));
  // Hook Loop instrument
  auto pInstrumentHookLoop = subject->getProjectContent()->put(
      ContentFixtures::buildInstrument(
          pLibrary, Instrument::Type::Hook, Instrument::Mode::Loop, Instrument::State::Published, "Hook Loop 1"));
  subject->getProjectContent()->put(
      ContentFixtures::buildInstrumentAudio(
          pInstrumentHookLoop, "Hook Loop 1 Audio", "hook-loop-1.wav", 0, 4, 60, 1, "X", "X", 1));

  subject->start(std::nullopt);
  unsigned long long atChainMicros = 0;
  while (atChainMicros < 60000000) {
    auto audios = subject->RunCycle(atChainMicros);
    for (const auto &audio: audios) {
      seenAudioSchedulingEventKeys.insert(computeKey(atChainMicros, audio));
    }
    std::cout << "Ran cycle at " << std::to_string(atChainMicros) << std::endl;
    atChainMicros += MICROS_PER_CYCLE;
    subject->finish(false);
  }

  // check that all the audio scheduling events were seen
  ASSERT_TRUE(seenAudioSchedulingEventKeys.size() > 10);
}
