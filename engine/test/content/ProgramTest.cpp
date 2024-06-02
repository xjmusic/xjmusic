// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjmusic/content/Program.h"

using namespace Content;

TEST(ProgramTest, FieldValues) {
  Program subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.libraryId = "0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9";
  subject.state = Program::State::Draft;
  subject.type = Program::Type::Main;
  subject.key = "C minor";
  subject.tempo = 120.0f;
  subject.name = "Test Program";
  subject.config = "barBeats = 4";
  subject.isDeleted = false;
  subject.updatedAt = 1711089919558;

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9", subject.libraryId);
  ASSERT_EQ(Program::State::Draft, subject.state);
  ASSERT_EQ(Program::Type::Main, subject.type);
  ASSERT_EQ("C minor", subject.key);
  ASSERT_EQ(120.0f, subject.tempo);
  ASSERT_EQ("Test Program", subject.name);
  ASSERT_EQ("barBeats = 4", subject.config);
  ASSERT_EQ(false, subject.isDeleted);
  ASSERT_EQ(1711089919558, subject.updatedAt);
}

TEST(ProgramTest, ParseProgramType) {
  ASSERT_EQ(Program::Type::Macro, Program::parseType("Macro"));
  ASSERT_EQ(Program::Type::Main, Program::parseType("Main"));
  ASSERT_EQ(Program::Type::Beat, Program::parseType("Beat"));
  ASSERT_EQ(Program::Type::Detail, Program::parseType("Detail"));
}

TEST(ProgramTest, ParseProgramState) {
  ASSERT_EQ(Program::State::Draft, Program::parseState("Draft"));
  ASSERT_EQ(Program::State::Published, Program::parseState("Published"));
}

TEST(ProgramTest, ToStringProgramType) {
  ASSERT_EQ("Macro", Program::toString(Program::Type::Macro));
  ASSERT_EQ("Main", Program::toString(Program::Type::Main));
  ASSERT_EQ("Beat", Program::toString(Program::Type::Beat));
  ASSERT_EQ("Detail", Program::toString(Program::Type::Detail));
}

TEST(ProgramTest, ToStringProgramState) {
  ASSERT_EQ("Draft", Program::toString(Program::State::Draft));
  ASSERT_EQ("Published", Program::toString(Program::State::Published));
}