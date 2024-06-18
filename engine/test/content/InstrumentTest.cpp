// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjmusic/content/Instrument.h"

using namespace XJ;

TEST(InstrumentTest, FieldValues) {
  Instrument subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.libraryId = "0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9";
  subject.type = Instrument::Type::Drum;
  subject.state = Instrument::State::Draft;
  subject.name = "Test Instrument";
  subject.config = "isAudioSelectionPersistent = true";
  subject.isDeleted = false;
  subject.volume = 0.5f;
  subject.mode = Instrument::Mode::Event;
  subject.updatedAt = 1711089919558;

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9", subject.libraryId);
  ASSERT_EQ(Instrument::Type::Drum, subject.type);
  ASSERT_EQ(Instrument::State::Draft, subject.state);
  ASSERT_EQ("Test Instrument", subject.name);
  ASSERT_EQ("isAudioSelectionPersistent = true", subject.config);
  ASSERT_EQ(false, subject.isDeleted);
  ASSERT_EQ(0.5f, subject.volume);
  ASSERT_EQ(Instrument::Mode::Event, subject.mode);
  ASSERT_EQ(1711089919558, subject.updatedAt);
}

TEST(InstrumentTest, ParseInstrumentType) {
  ASSERT_EQ(Instrument::Type::Drum, Instrument::parseType("Drum"));
  ASSERT_EQ(Instrument::Type::Bass, Instrument::parseType("Bass"));
  ASSERT_EQ(Instrument::Type::Pad, Instrument::parseType("Pad"));
  ASSERT_EQ(Instrument::Type::Sticky, Instrument::parseType("Sticky"));
  ASSERT_EQ(Instrument::Type::Stripe, Instrument::parseType("Stripe"));
  ASSERT_EQ(Instrument::Type::Stab, Instrument::parseType("Stab"));
  ASSERT_EQ(Instrument::Type::Hook, Instrument::parseType("Hook"));
  ASSERT_EQ(Instrument::Type::Percussion, Instrument::parseType("Percussion"));
  ASSERT_EQ(Instrument::Type::Transition, Instrument::parseType("Transition"));
  ASSERT_EQ(Instrument::Type::Background, Instrument::parseType("Background"));
}

TEST(InstrumentTest, ParseInstrumentMode) {
  ASSERT_EQ(Instrument::Mode::Event, Instrument::parseMode("Event"));
  ASSERT_EQ(Instrument::Mode::Chord, Instrument::parseMode("Chord"));
  ASSERT_EQ(Instrument::Mode::Loop, Instrument::parseMode("Loop"));
}

TEST(InstrumentTest, ParseInstrumentState) {
  ASSERT_EQ(Instrument::State::Draft, Instrument::parseState("Draft"));
  ASSERT_EQ(Instrument::State::Published, Instrument::parseState("Published"));
}

TEST(InstrumentTest, ToStringInstrumentType) {
  ASSERT_EQ("Drum", Instrument::toString(Instrument::Type::Drum));
  ASSERT_EQ("Bass", Instrument::toString(Instrument::Type::Bass));
  ASSERT_EQ("Pad", Instrument::toString(Instrument::Type::Pad));
  ASSERT_EQ("Sticky", Instrument::toString(Instrument::Type::Sticky));
  ASSERT_EQ("Stripe", Instrument::toString(Instrument::Type::Stripe));
  ASSERT_EQ("Stab", Instrument::toString(Instrument::Type::Stab));
  ASSERT_EQ("Hook", Instrument::toString(Instrument::Type::Hook));
  ASSERT_EQ("Percussion", Instrument::toString(Instrument::Type::Percussion));
  ASSERT_EQ("Transition", Instrument::toString(Instrument::Type::Transition));
  ASSERT_EQ("Background", Instrument::toString(Instrument::Type::Background));
}

TEST(InstrumentTest, ToStringInstrumentMode) {
  ASSERT_EQ("Event", Instrument::toString(Instrument::Mode::Event));
  ASSERT_EQ("Chord", Instrument::toString(Instrument::Mode::Chord));
  ASSERT_EQ("Loop", Instrument::toString(Instrument::Mode::Loop));
}

TEST(InstrumentTest, ToStringInstrumentState) {
  ASSERT_EQ("Draft", Instrument::toString(Instrument::State::Draft));
  ASSERT_EQ("Published", Instrument::toString(Instrument::State::Published));
}