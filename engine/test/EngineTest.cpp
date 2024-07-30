// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "./_helper/ContentFixtures.h"

#include "xjmusic/Engine.h"
#include "xjmusic/craft/Craft.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class XJEngineTest : public testing::Test
{

protected:
	std::string			  ENGINE_TEST_PROJECT_PATH = "_data/test_project/TestProject.xj";
	int					  craftAheadSeconds = 15;
	int					  dubAheadSeconds = 30;
	int					  deadlineSeconds = 1;
	int					  persistenceWindowSeconds = 60;
	int					  MARATHON_NUMBER_OF_SEGMENTS = 50;
	long				  MICROS_PER_CYCLE = 1000000;
	long long			  MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
	long long			  MILLIS_PER_SECOND = 1000;
	long long			  startTime = EntityUtils::currentTimeMillis();
	std::set<std::string> seenAudioSchedulingEventKeys;

	/**
	 * Compute a key for the audio scheduling event
	 * @param atChainMicros  the chain micros at which the event is scheduled
	 * @param event  the audio scheduling event
	 * @return  the key
	 */
	static std::string computeKey(const unsigned long long atChainMicros, const AudioScheduleEvent& event)
	{
		return StringUtils::zeroPadded(atChainMicros, 12) + "_" + AudioScheduleEvent::toString(event.type)
			+ "_" + StringUtils::toProperSlug(event.audio.getAudio()->name)
			+ "_at[" + StringUtils::zeroPadded(event.audio.getStartAtChainMicros(), 12)
			+ "," + StringUtils::zeroPadded(event.audio.getStopAtChainMicros(), 12) + "]"
			+ "_vol[" + StringUtils::zeroPadded(static_cast<int>(event.audio.getFromVolume() * 100), 3)
			+ "," + StringUtils::zeroPadded(static_cast<int>(event.audio.getToVolume() * 100), 3) + "]";
	}

	void SetUp() override
	{
		seenAudioSchedulingEventKeys.clear();
	}
};

TEST_F(XJEngineTest, ReadsAndRunsProjectFromDisk)
{
	const std::unique_ptr<Engine> subject = std::make_unique<Engine>(
		ENGINE_TEST_PROJECT_PATH,
		Fabricator::ControlMode::Taxonomy,
		craftAheadSeconds,
		dubAheadSeconds,
		deadlineSeconds,
		persistenceWindowSeconds);

	auto memeTaxonomy = subject->getMemeTaxonomy();
	ASSERT_TRUE(memeTaxonomy.has_value());
	const auto categories = memeTaxonomy.value().getCategories();
	ASSERT_FALSE(categories.empty());
	ASSERT_EQ(categories.size(), 2);

	const auto tmpl = subject->getProjectContent()->getFirstTemplate();
	ASSERT_TRUE(tmpl.has_value());

	subject->start(tmpl.value()->id);
	unsigned long long atChainMicros = 0;
	while (atChainMicros < 60000000)
	{
		auto audios = subject->RunCycle(atChainMicros);
		for (const auto& audio : audios)
		{
			seenAudioSchedulingEventKeys.insert(computeKey(atChainMicros, audio));
			// check that the audio file exists
			ASSERT_TRUE(std::filesystem::exists(subject->getPathToBuildDirectory() / audio.audio.getAudio()->waveformKey));
		}
		std::cout << "Ran cycle at " << std::to_string(atChainMicros) << std::endl;
		atChainMicros += MICROS_PER_CYCLE;
	}
	subject->finish(false);

	// check that all the audio scheduling events were seen
	ASSERT_TRUE(seenAudioSchedulingEventKeys.size() > 10);
}

TEST_F(XJEngineTest, CorrectlyDubsAudio)
{
	const std::unique_ptr<Engine> subject = std::make_unique<Engine>(
		std::nullopt,
		Fabricator::ControlMode::Auto,
		craftAheadSeconds,
		dubAheadSeconds,
		deadlineSeconds,
		persistenceWindowSeconds);
	// Project, library, template
	const auto pProject = subject->getProjectContent()->put(ContentFixtures::buildProject());
	const auto pLibrary = subject->getProjectContent()->put(ContentFixtures::buildLibrary(pProject, "Test Library"));
	const auto pTemplate = subject->getProjectContent()->put(ContentFixtures::buildTemplate(pProject, "Test Template", "test", "memeTaxonomy=[]\nintensityAutoCrescendoEnabled=false\n"));
	subject->getProjectContent()->put(ContentFixtures::buildTemplateBinding(pTemplate, pLibrary));
	// Macro program
	const auto pProgramMacro = subject->getProjectContent()->put(
		ContentFixtures::buildProgram(pLibrary, Program::Type::Macro, Program::State::Published, "Test Macro", "C", 60));
	const auto pProgramMacroSequence = subject->getProjectContent()->put(
		ContentFixtures::buildProgramSequence(pProgramMacro, 8, "Test Macro Sequence", 1, "C"));
	subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceBinding(pProgramMacroSequence, 0));
	subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceBinding(pProgramMacroSequence, 1));
	// Main program
	const auto pProgramMain = subject->getProjectContent()->put(
		ContentFixtures::buildProgram(pLibrary, Program::Type::Main, Program::State::Published, "Test Main", "C", 60));
	subject->getProjectContent()->put(ContentFixtures::buildProgramMeme(pProgramMain, "Apples"));
	const auto pProgramMainSequence = subject->getProjectContent()->put(
		ContentFixtures::buildProgramSequence(pProgramMain, 8, "Test Main Sequence", 1, "C"));
	subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceChord(pProgramMainSequence, 0, "C"));
	subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceChord(pProgramMainSequence, 2, "G"));
	subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceChord(pProgramMainSequence, 4, "C"));
	subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceChord(pProgramMainSequence, 6, "G"));
	subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceBinding(pProgramMainSequence, 0));
	subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceBinding(pProgramMainSequence, 1));
	subject->getProjectContent()->put(ContentFixtures::buildProgramSequenceBinding(pProgramMainSequence, 2));
	// Beat program
	const auto pProgramBeat = subject->getProjectContent()->put(
		ContentFixtures::buildProgram(pLibrary, Program::Type::Beat, Program::State::Published, "Test Beat", "C", 60));
	const auto pProgramBeatVoice = subject->getProjectContent()->put(
		ContentFixtures::buildProgramVoice(pProgramBeat, Instrument::Type::Drum, "Kick & Snare"));
	const auto pProgramBeatTrackKick = subject->getProjectContent()->put(
		ContentFixtures::buildProgramVoiceTrack(pProgramBeatVoice, "KICK"));
	const auto pProgramBeatTrackSnare = subject->getProjectContent()->put(
		ContentFixtures::buildProgramVoiceTrack(pProgramBeatVoice, "SNARE"));
	const auto pProgramBeatSequence = subject->getProjectContent()->put(
		ContentFixtures::buildProgramSequence(pProgramBeat, 8, "Test Beat Sequence", 1, "C"));
	subject->getProjectContent()->put(
		ContentFixtures::buildProgramSequenceBinding(pProgramBeatSequence, 0));
	const auto pProgramBeatPattern = subject->getProjectContent()->put(
		ContentFixtures::buildProgramSequencePattern(pProgramBeatSequence, pProgramBeatVoice, 2, "Groove"));
	subject->getProjectContent()->put(
		ContentFixtures::buildProgramSequencePatternEvent(pProgramBeatPattern, pProgramBeatTrackKick, 0, 1, "X", 1));
	subject->getProjectContent()->put(
		ContentFixtures::buildProgramSequencePatternEvent(pProgramBeatPattern, pProgramBeatTrackSnare, 1, 1, "X", 1));
	// Percussion Loop instrument
	const auto pInstrumentPercussionLoop = subject->getProjectContent()->put(
		ContentFixtures::buildInstrument(
			pLibrary, Instrument::Type::Percussion, Instrument::Mode::Loop, Instrument::State::Published, "Perc Loop 1"));
	subject->getProjectContent()->put(
		ContentFixtures::buildInstrumentAudio(
			pInstrumentPercussionLoop, "Perc Loop 1 Audio", "perc-loop-1.wav", 0, 2, 60, 1, "X", "X", 1));
	// Pad Chord instrument
	const auto pInstrumentPadChord = subject->getProjectContent()->put(
		ContentFixtures::buildInstrument(
			pLibrary, Instrument::Type::Pad, Instrument::Mode::Chord, Instrument::State::Published, "Pad Chord 1"));
	subject->getProjectContent()->put(
		ContentFixtures::buildInstrumentAudio(
			pInstrumentPadChord, "Pad Chord 1 C", "pad-chord-1.wav", 0, 4, 60, 1, "C", "C", 1));
	subject->getProjectContent()->put(
		ContentFixtures::buildInstrumentAudio(
			pInstrumentPadChord, "Pad Chord 1 F", "pad-chord-1.wav", 0, 4, 60, 1, "F", "F", 1));
	// Hook Loop instrument
	const auto pInstrumentHookLoop = subject->getProjectContent()->put(
		ContentFixtures::buildInstrument(
			pLibrary, Instrument::Type::Hook, Instrument::Mode::Loop, Instrument::State::Published, "Hook Loop 1"));
	subject->getProjectContent()->put(
		ContentFixtures::buildInstrumentAudio(
			pInstrumentHookLoop, "Hook Loop 1 Audio", "hook-loop-1.wav", 0, 4, 60, 1, "X", "X", 1));
	// Drum Event Instrument
	const auto pInstrumentDrumEvent = subject->getProjectContent()->put(
		ContentFixtures::buildInstrument(
			pLibrary, Instrument::Type::Drum, Instrument::Mode::Event, Instrument::State::Published, "Drum Event 1"));
	subject->getProjectContent()->put(
		ContentFixtures::buildInstrumentAudio(
			pInstrumentDrumEvent, "Kick", "drum-event-kick.wav", 0, 2, 60, 1, "KICK", "X", 1));
	subject->getProjectContent()->put(
		ContentFixtures::buildInstrumentAudio(
			pInstrumentDrumEvent, "Snare", "drum-event-snare.wav", 0, 2, 60, 1, "SNARE", "X", 1));

	subject->start("Test Template");
	unsigned long long atChainMicros = 0;
	while (atChainMicros < 60000000)
	{
		auto audios = subject->RunCycle(atChainMicros);
		for (const auto& audio : audios)
		{
			seenAudioSchedulingEventKeys.emplace(computeKey(atChainMicros, audio));
		}
		std::cout << "Ran cycle at " << std::to_string(atChainMicros) << std::endl;
		atChainMicros += MICROS_PER_CYCLE;
	}
	subject->finish(false);

	// Check that Perc Loop 1 was dubbed correctly
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_PercLoop1Audio_at[000000000000,000002000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_PercLoop1Audio_at[000002000000,000004000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_PercLoop1Audio_at[000004000000,000006000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_PercLoop1Audio_at[000006000000,000008000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000001000000_Create_PercLoop1Audio_at[000008000000,000010000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000001000000_Create_PercLoop1Audio_at[000010000000,000012000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000001000000_Create_PercLoop1Audio_at[000012000000,000014000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000001000000_Create_PercLoop1Audio_at[000014000000,000016000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000003000000_Delete_PercLoop1Audio_at[000000000000,000002000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000005000000_Delete_PercLoop1Audio_at[000002000000,000004000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000007000000_Delete_PercLoop1Audio_at[000004000000,000006000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000009000000_Delete_PercLoop1Audio_at[000006000000,000008000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000009000000_Delete_PercLoop1Audio_at[000006000000,000008000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000011000000_Delete_PercLoop1Audio_at[000008000000,000010000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000013000000_Delete_PercLoop1Audio_at[000010000000,000012000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000015000000_Delete_PercLoop1Audio_at[000012000000,000014000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000017000000_Delete_PercLoop1Audio_at[000014000000,000016000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	// Check that Pad Chord 1 was dubbed correctly
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_PadChord1C_at[000000000000,000002000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_PadChord1C_at[000004000000,000006000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000001000000_Create_PadChord1C_at[000008000000,000010000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000001000000_Create_PadChord1C_at[000012000000,000014000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000002000000_Create_PadChord1C_at[000016000000,000018000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000002000000_Create_PadChord1C_at[000020000000,000022000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000003000000_Delete_PadChord1C_at[000000000000,000002000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000007000000_Delete_PadChord1C_at[000004000000,000006000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000009000000_Create_PadChord1C_at[000024000000,000026000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000009000000_Create_PadChord1C_at[000028000000,000030000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000011000000_Delete_PadChord1C_at[000008000000,000010000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000015000000_Delete_PadChord1C_at[000012000000,000014000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000019000000_Delete_PadChord1C_at[000016000000,000018000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000023000000_Delete_PadChord1C_at[000020000000,000022000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000027000000_Delete_PadChord1C_at[000024000000,000026000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000031000000_Delete_PadChord1C_at[000028000000,000030000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	// Check that Hook Loop 1 was dubbed correctly
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_HookLoop1Audio_at[000000000000,000004000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_HookLoop1Audio_at[000004000000,000008000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000001000000_Create_HookLoop1Audio_at[000008000000,000012000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000001000000_Create_HookLoop1Audio_at[000012000000,000016000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000002000000_Create_HookLoop1Audio_at[000016000000,000020000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000002000000_Create_HookLoop1Audio_at[000020000000,000024000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000005000000_Delete_HookLoop1Audio_at[000000000000,000004000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000009000000_Create_HookLoop1Audio_at[000024000000,000028000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000009000000_Create_HookLoop1Audio_at[000028000000,000032000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000009000000_Delete_HookLoop1Audio_at[000004000000,000008000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000013000000_Delete_HookLoop1Audio_at[000008000000,000012000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000017000000_Delete_HookLoop1Audio_at[000012000000,000016000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000021000000_Delete_HookLoop1Audio_at[000016000000,000020000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000025000000_Delete_HookLoop1Audio_at[000020000000,000024000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000029000000_Delete_HookLoop1Audio_at[000024000000,000028000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000033000000_Delete_HookLoop1Audio_at[000028000000,000032000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	// Check that the beat program was dubbed correctly with the drum instrument
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_Kick_at[000000000000,000001000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_Kick_at[000002000000,000003000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_Kick_at[000004000000,000005000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_Kick_at[000006000000,000007000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_Snare_at[000001000000,000002000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_Snare_at[000003000000,000004000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_Snare_at[000005000000,000006000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000000000000_Create_Snare_at[000007000000,000008000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000002000000_Delete_Kick_at[000000000000,000001000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000003000000_Delete_Snare_at[000001000000,000002000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000004000000_Delete_Kick_at[000002000000,000003000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000005000000_Delete_Snare_at[000003000000,000004000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000006000000_Delete_Kick_at[000004000000,000005000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000007000000_Delete_Snare_at[000005000000,000006000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000008000000_Delete_Kick_at[000006000000,000007000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
	EXPECT_TRUE(seenAudioSchedulingEventKeys.find("000009000000_Delete_Snare_at[000007000000,000008000000]_vol[100,100]") != seenAudioSchedulingEventKeys.end());
}