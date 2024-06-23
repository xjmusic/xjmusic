// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/music/NoteRange.h"

#include "../_helper/TestHelpers.h"

using namespace XJ;
using namespace XJ;


TEST(Music_NoteRange, Low) {
  const auto subject = NoteRange::ofStrings(std::vector<std::string>{"C3", "E3", "D4", "E5", "F6"});

  ASSERT_TRUE(Note::of("C3") == (subject.low.value()));
}


TEST(Music_NoteRange, High) {
  const auto subject = NoteRange::ofStrings(std::vector<std::string>{"C3", "E3", "D4", "E5", "F6"});

  ASSERT_TRUE(Note::of("F6") == (subject.high.value()));
}


TEST(Music_NoteRange, RangeFromNotes) {
  const auto subject = NoteRange::from(Note::of("C3"), Note::of("C4"));

  ASSERT_TRUE(Note::of("C3") == (subject.low.value()));
  ASSERT_TRUE(Note::of("C4") == (subject.high.value()));
}


TEST(Music_NoteRange, RangeOfNotes_isEmpty) {
  const auto subject = NoteRange::ofNotes(std::set<Note>{Note::of("X")});

  ASSERT_TRUE(subject.empty());
}


TEST(Music_NoteRange, RangeOfNotes_FromSet) {
  const auto subject = NoteRange::ofNotes(std::set<Note>{
      Note::of("C3"),
      Note::of("E3"),
      Note::of("D4"),
      Note::of("E5"),
      Note::of("F6")
  });

  ASSERT_TRUE(Note::of("C3") == (subject.low.value()));
  ASSERT_TRUE(Note::of("F6") == (subject.high.value()));
}


TEST(Music_NoteRange, RangeOfNotes_FromVector) {
  const auto subject = NoteRange::ofNotes(std::vector<Note>{
      Note::of("C3"),
      Note::of("E3"),
      Note::of("D4"),
      Note::of("E5"),
      Note::of("F6")
  });

  ASSERT_TRUE(Note::of("C3") == (subject.low.value()));
  ASSERT_TRUE(Note::of("F6") == (subject.high.value()));
}


TEST(Music_NoteRange, RangeFromNotes_LowOptional) {
  const auto subject = NoteRange::from(Note::atonal(), Note::of("C4"));

  ASSERT_FALSE(subject.low.has_value());
  ASSERT_TRUE(Note::of("C4") == (subject.high.value()));
}


TEST(Music_NoteRange, RangeFromNotes_HighOptional) {
  const auto subject = NoteRange::from(Note::of("C4"), Note::atonal());

  ASSERT_TRUE(Note::of("C4") == (subject.low.value()));
  ASSERT_FALSE(subject.high.has_value());
}


TEST(Music_NoteRange, RangeFromStrings) {
  const auto subject = NoteRange::from("C3", "C4");

  ASSERT_TRUE(Note::of("C3") == (subject.low.value()));
  ASSERT_TRUE(Note::of("C4") == (subject.high.value()));
}


TEST(Music_NoteRange, RangeFromStrings_LowOptional) {
  const auto subject = NoteRange::from("X", "C4");

  ASSERT_FALSE(subject.low.has_value());
  ASSERT_TRUE(Note::of("C4") == (subject.high.value()));
}


TEST(Music_NoteRange, RangeFromStrings_HighOptional) {
  const auto subject = NoteRange::from("C4", "X");

  ASSERT_TRUE(Note::of("C4") == (subject.low.value()));
  ASSERT_FALSE(subject.high.has_value());
}


TEST(Music_NoteRange, CopyOf) {
  const auto subject = NoteRange::ofStrings(std::vector<std::string>{"C3", "E3", "D4", "E5", "F6"});
  const auto cp = NoteRange::copyOf(subject);

  ASSERT_TRUE(Note::of("C3") == (cp.low.value()));
  ASSERT_TRUE(Note::of("F6") == (cp.high.value()));
}


TEST(Music_NoteRange, OutputToString) {
  auto subject = NoteRange::ofStrings(std::vector<std::string>{"C3", "E3", "D4", "E5", "F6"});

  ASSERT_EQ("C3-F6", subject.toString(Accidental::Natural));
}


TEST(Music_NoteRange, Expand) {
  auto subject = NoteRange::ofStrings(std::vector<std::string>{"C3", "E3", "D4", "E5", "F6"});

  subject.expand(Note::of("G2"));

  ASSERT_TRUE(Note::of("G2") == (subject.low.value()));
  ASSERT_TRUE(Note::of("F6") == (subject.high.value()));
}


TEST(Music_NoteRange, Expand_ByNotes) {
  auto subject = NoteRange::ofStrings(std::vector<std::string>{"C3", "E3", "D4", "E5", "F6"});

  subject.expand(std::vector<Note>{
      Note::of("G2"),
      Note::of("G6")
  });

  ASSERT_TRUE(Note::of("G2") == (subject.low.value()));
  ASSERT_TRUE(Note::of("G6") == (subject.high.value()));
}


TEST(Music_NoteRange, Expand_ByRange) {
  auto subject = NoteRange::ofStrings(std::vector<std::string>{"C3", "E3", "D4", "E5", "F6"});
  const auto expansion = NoteRange::ofStrings(std::vector<std::string>{ "G2", "G6"});

  subject.expand(&expansion);

  ASSERT_TRUE(Note::of("G2") == (subject.low.value()));
  ASSERT_TRUE(Note::of("G6") == (subject.high.value()));
}


TEST(Music_NoteRange, Median) {
  auto fromNulls = NoteRange::median(NoteRange::ofStrings(std::vector<std::string>{}), NoteRange::ofStrings({}));
  ASSERT_FALSE(fromNulls.low.has_value());
  ASSERT_FALSE(fromNulls.high.has_value());

  auto emptyHigh = NoteRange::median(NoteRange::ofStrings(std::vector<std::string>{"C5"}), NoteRange::ofStrings({}));
  TestHelpers::assertNote("C5", emptyHigh.low.value());
  TestHelpers::assertNote("C5", emptyHigh.high.value());

  auto emptyLow = NoteRange::median(NoteRange::ofStrings(std::vector<std::string>{}), NoteRange::ofStrings({"G#5"}));
  TestHelpers::assertNote("G#5", emptyLow.low.value());
  TestHelpers::assertNote("G#5", emptyLow.high.value());

  auto normal = NoteRange::median(NoteRange::ofStrings(std::vector<std::string>{"C5", "G5"}),
                                  NoteRange::ofStrings({"G#5", "B5"}));
  TestHelpers::assertNote("E5", normal.low.value());
  TestHelpers::assertNote("A5", normal.high.value());
}


TEST(Music_NoteRange, DeltaSemitones) {
  ASSERT_EQ(0, NoteRange::from("C5", "G5").getDeltaSemitones(NoteRange::from("C5", "G5")));
  ASSERT_EQ(0, NoteRange::from("C5", "G5").getDeltaSemitones(NoteRange::from("D5", "F5")));
  ASSERT_EQ(1, NoteRange::from("C5", "G5").getDeltaSemitones(NoteRange::from("E5", "F5")));
  ASSERT_EQ(-1, NoteRange::from("C5", "G5").getDeltaSemitones(NoteRange::from("D5", "Eb5")));
  ASSERT_EQ(2, NoteRange::from("C5", "G5").getDeltaSemitones(NoteRange::from("F5", "F#5")));
  ASSERT_EQ(-2, NoteRange::from("C5", "G5").getDeltaSemitones(NoteRange::from("Db5", "D5")));
}


TEST(Music_NoteRange, NoteNearestMedian) {
  TestHelpers::assertNote("C5", NoteRange::from("C3", "G6").getNoteNearestMedian(PitchClass::C).value());
  TestHelpers::assertNote("C5", NoteRange::from("C3", "G7").getNoteNearestMedian(PitchClass::C).value());
  TestHelpers::assertNote("C4", NoteRange::from("C2", "G6").getNoteNearestMedian(PitchClass::C).value());
  ASSERT_FALSE(NoteRange::from("C3", "G6").getNoteNearestMedian(PitchClass::Atonal).has_value());
}


TEST(Music_NoteRange, MedianNote) {
  TestHelpers::assertNote("D#5", NoteRange::from("C5", "G5").getMedianNote().value());
  TestHelpers::assertNote("G5", NoteRange::from("X", "G5").getMedianNote().value());
  TestHelpers::assertNote("C5", NoteRange::from("C5", "X").getMedianNote().value());
  ASSERT_FALSE(NoteRange().getMedianNote().has_value());
}


TEST(Music_NoteRange, Shifted) {
  auto input = NoteRange::from("C5", "G5");

  auto result = input.shifted(2);

  TestHelpers::assertNote("D5", result.low.value());
  TestHelpers::assertNote("A5", result.high.value());
  // input not modified
  TestHelpers::assertNote("C5", input.low.value());
  TestHelpers::assertNote("G5", input.high.value());
}


TEST(Music_NoteRange, IsEmpty) {
  ASSERT_TRUE(NoteRange().empty());
  ASSERT_FALSE(NoteRange::from("C5", "G5").empty());
}


TEST(Music_NoteRange, Includes) {
  ASSERT_TRUE(NoteRange::from(Note::of("C4"), Note::of("G4")).includes(Note::of("C4")));
  ASSERT_TRUE(NoteRange::from(Note::of("C4"), Note::of("G4")).includes(Note::of("G4")));
  ASSERT_FALSE(NoteRange::from(Note::of("C4"), Note::of("G4")).includes(Note::of("B3")));
  ASSERT_FALSE(NoteRange::from(Note::of("C4"), Note::of("G4")).includes(Note::of("G#4")));
  ASSERT_TRUE(NoteRange::from(Note::of("C4"), Note::atonal()).includes(Note::of("C4")));
  ASSERT_FALSE(NoteRange::from(Note::of("C4"), Note::atonal()).includes(Note::of("B3")));
  ASSERT_FALSE(NoteRange::from(Note::of("C4"), Note::atonal()).includes(Note::of("D4")));
  ASSERT_TRUE(NoteRange::from(Note::atonal(), Note::of("C4")).includes(Note::of("C4")));
  ASSERT_FALSE(NoteRange::from(Note::atonal(), Note::of("C4")).includes(Note::of("B3")));
  ASSERT_FALSE(NoteRange::from(Note::atonal(), Note::of("C4")).includes(Note::of("D4")));
  ASSERT_FALSE(NoteRange().includes(Note::of("C4")));
}


TEST(Music_NoteRange, ToAvailableOctave) {
  TestHelpers::assertNote("C4", NoteRange::from(Note::of("C4"), Note::of("C6")).toAvailableOctave(Note::of("C4")));
  TestHelpers::assertNote("C5", NoteRange::from(Note::of("C4"), Note::of("C6")).toAvailableOctave(Note::of("C5")));
  TestHelpers::assertNote("C6", NoteRange::from(Note::of("C4"), Note::of("C6")).toAvailableOctave(Note::of("C6")));
  TestHelpers::assertNote("D5", NoteRange::from(Note::of("C4"), Note::of("C6")).toAvailableOctave(Note::of("D6")));
  TestHelpers::assertNote("D4", NoteRange::from(Note::of("C4"), Note::of("C6")).toAvailableOctave(Note::of("D3")));
  TestHelpers::assertNote("C4", NoteRange::from(Note::of("C4"), Note::atonal()).toAvailableOctave(Note::of("C4")));
  TestHelpers::assertNote("B3", NoteRange::from(Note::of("C4"), Note::atonal()).toAvailableOctave(Note::of("B3")));
  TestHelpers::assertNote("D4", NoteRange::from(Note::of("C4"), Note::atonal()).toAvailableOctave(Note::of("D4")));
  TestHelpers::assertNote("C4", NoteRange::from(Note::atonal(), Note::of("C4")).toAvailableOctave(Note::of("C4")));
  TestHelpers::assertNote("B3", NoteRange::from(Note::atonal(), Note::of("C4")).toAvailableOctave(Note::of("B3")));
  TestHelpers::assertNote("D4", NoteRange::from(Note::atonal(), Note::of("C4")).toAvailableOctave(Note::of("D4")));
  TestHelpers::assertNote("C4", NoteRange().toAvailableOctave(Note::of("C4")));
}


TEST(Music_NoteRange, ComputeMedianOptimalRangeShiftOctaves) {
  auto rangeA = NoteRange::from(Note::of("C3"), Note::of("B3"));
  auto rangeA_overlap = NoteRange::from(Note::of("G3"), Note::of("C6"));
  auto rangeB = NoteRange::from(Note::of("C5"), Note::of("B5"));
  auto rangeB_superset = NoteRange::from(Note::of("G2"), Note::of("D6"));

  auto rangeC1 = NoteRange::from(Note::of("F3"), Note::of("E4"));
  auto rangeC2 = NoteRange::from(Note::of("D3"), Note::of("C6"));

  ASSERT_EQ(2, NoteRange::computeMedianOptimalRangeShiftOctaves(&rangeA, &rangeB));
  ASSERT_EQ(-2, NoteRange::computeMedianOptimalRangeShiftOctaves(&rangeB, &rangeA));
  ASSERT_EQ(0, NoteRange::computeMedianOptimalRangeShiftOctaves(&rangeB, &rangeB_superset));
  ASSERT_EQ(0, NoteRange::computeMedianOptimalRangeShiftOctaves(&rangeB_superset, &rangeB));
  ASSERT_EQ(1, NoteRange::computeMedianOptimalRangeShiftOctaves(&rangeA, &rangeA_overlap));
  ASSERT_EQ(0, NoteRange::computeMedianOptimalRangeShiftOctaves(&rangeC1, &rangeC2));
  ASSERT_EQ(0, NoteRange::computeMedianOptimalRangeShiftOctaves(&rangeC2, &rangeC1));
}
