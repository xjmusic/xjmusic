// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/audio/ActiveAudio.h"
#include "xjmusic/segment/SegmentChoice.h"
#include "xjmusic/segment/SegmentChoiceArrangement.h"

using namespace XJ;

class ActiveAudioTest : public testing::Test
{
protected:
	Instrument					 instrument = Instrument();
	InstrumentAudio				 audio = InstrumentAudio();
	SegmentChoice				 choice = SegmentChoice();
	SegmentChoiceArrangement	 arrangement = SegmentChoiceArrangement();
	SegmentChoiceArrangementPick pick1 = SegmentChoiceArrangementPick();
	SegmentChoiceArrangementPick pick2 = SegmentChoiceArrangementPick();
	int							 intensityLayers = 2;
	float						 intensityThreshold = 0.5f;
	ActiveAudio*				 subject{};

	void SetUp() override
	{
		instrument.volume = 0.5f;
		audio.volume = 0.5f;
		pick1.amplitude = 0.5f;
		pick1.id = EntityUtils::computeUniqueId();
		pick2.id = EntityUtils::computeUniqueId();
		subject = new ActiveAudio(
			&pick1,
			&instrument,
			&audio,
			504000000,
			528000000,
			0.7f,
			0.9f);
	}

	void TearDown() override
	{
		delete subject;
	}
};

TEST_F(ActiveAudioTest, Equality)
{
	EXPECT_TRUE(*subject == ActiveAudio(&pick1, &instrument, &audio, 504000000, 528000000, 0.7f, 0.9f));
	EXPECT_FALSE(*subject == ActiveAudio(&pick1, &instrument, &audio, 503000000, 528000000, 0.7f, 0.9f));
	EXPECT_FALSE(*subject == ActiveAudio(&pick1, &instrument, &audio, 504000000, 529000000, 0.7f, 0.9f));
	EXPECT_FALSE(*subject == ActiveAudio(&pick1, &instrument, &audio, 504000000, 528000000, 0.6f, 0.9f));
	EXPECT_FALSE(*subject == ActiveAudio(&pick1, &instrument, &audio, 504000000, 528000000, 0.7f, 0.8f));
}

TEST_F(ActiveAudioTest, InEquality)
{
	EXPECT_FALSE(*subject != ActiveAudio(&pick1, &instrument, &audio, 504000000, 528000000, 0.7f, 0.9f));
	EXPECT_TRUE(*subject != ActiveAudio(&pick1, &instrument, &audio, 503000000, 528000000, 0.7f, 0.9f));
	EXPECT_TRUE(*subject != ActiveAudio(&pick1, &instrument, &audio, 504000000, 529000000, 0.7f, 0.9f));
	EXPECT_TRUE(*subject != ActiveAudio(&pick1, &instrument, &audio, 504000000, 528000000, 0.6f, 0.9f));
	EXPECT_TRUE(*subject != ActiveAudio(&pick1, &instrument, &audio, 504000000, 528000000, 0.7f, 0.8f));
}

TEST_F(ActiveAudioTest, GetFromVolume)
{
	EXPECT_FLOAT_EQ(0.7f * 0.5f * 0.5f * 0.5f, subject->getFromVolume());
}

TEST_F(ActiveAudioTest, GetToVolume)
{
	EXPECT_FLOAT_EQ(0.9f * 0.5f * 0.5f * 0.5f, subject->getToVolume());
}

TEST_F(ActiveAudioTest, GetId)
{
	EXPECT_EQ(pick1.id, subject->getId());
}

TEST_F(ActiveAudioTest, GetPick)
{
	EXPECT_EQ(&pick1, subject->getPick());
}

TEST_F(ActiveAudioTest, GetInstrument)
{
	EXPECT_EQ(&instrument, subject->getInstrument());
}

TEST_F(ActiveAudioTest, GetStartAtChainMicros)
{
	EXPECT_EQ(504000000, subject->getStartAtChainMicros());
}

TEST_F(ActiveAudioTest, GetStopAtChainMicros)
{
	EXPECT_EQ(528000000, subject->getStopAtChainMicros());
}

TEST_F(ActiveAudioTest, GetAudio)
{
	EXPECT_EQ(&audio, subject->getAudio());
}

TEST_F(ActiveAudioTest, GetReleaseMillis)
{
	EXPECT_EQ(instrument.config.releaseMillis, subject->getReleaseMillis());
}

TEST_F(ActiveAudioTest, GetAmplitude)
{
	EXPECT_FLOAT_EQ(0.7f * 0.5f * 0.5f * 0.5f, subject->getAmplitude(0.0f));
	EXPECT_FLOAT_EQ(0.8f * 0.5f * 0.5f * 0.5f, subject->getAmplitude(0.5f));
	EXPECT_FLOAT_EQ(0.9f * 0.5f * 0.5f * 0.5f, subject->getAmplitude(1.0f));
}

TEST_F(ActiveAudioTest, LessThan)
{
	EXPECT_TRUE(*subject < ActiveAudio(
					&pick2,
					&instrument,
					&audio,
					504000000,
					528000000,
					0.7f,
					0.9f));

	EXPECT_FALSE(*subject < ActiveAudio(
					 &pick1,
					 &instrument,
					 &audio,
					 504000000,
					 528000000,
					 0.7f,
					 0.9f));

	EXPECT_TRUE(ActiveAudio(
					&pick2,
					&instrument,
					&audio,
					504000000,
					528000000,
					0.7f,
					0.9f)
		> *subject);

	EXPECT_FALSE(ActiveAudio(
					 &pick1,
					 &instrument,
					 &audio,
					 504000000,
					 528000000,
					 0.7f,
					 0.9f)
		> *subject);
}
