// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "TestHelpers.h"

using namespace XJ;


void TestHelpers::assertNote(const std::string &expect, const Note &actual) 
{
  check(Note::of(expect).pitchClass == actual.pitchClass);
  check(Note::of(expect).octave == actual.octave);
}
