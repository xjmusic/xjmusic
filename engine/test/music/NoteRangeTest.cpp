// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjnexus/music/NoteRange.h"

#include "../_helper/AssertionHelpers.h"

using namespace Music;

TEST(Music_NoteRange, Low) {
  auto subject = NoteRange::ofStrings(std::vector<std::string>{"C3", "E3", "D4", "E5", "F6"});

  ASSERT_TRUE(Note::of("C3") == (subject.low.value()));
}

TEST(Music_NoteRange, High) {
  auto subject = NoteRange::ofStrings(std::vector<std::string>{"C3", "E3", "D4", "E5", "F6"});

  ASSERT_TRUE(Note::of("F6") == (subject.high.value()));
}

TEST(Music_NoteRange, RangeFromNotes) {
  auto subject = NoteRange::from(Note::of("C3"), Note::of("C4"));

  ASSERT_TRUE(Note::of("C3") == (subject.low.value()));
  ASSERT_TRUE(Note::of("C4") == (subject.high.value()));
}

TEST(Music_NoteRange, RangeOfNoteX_isEmpty) {
  auto subject = NoteRange::ofNotes(std::vector<Note>{Note::of("X")});

  ASSERT_TRUE(subject.isEmpty());
}

TEST(Music_NoteRange, RangeFromNotes_LowOptional) {
  auto subject = NoteRange::from(Note::atonal(), Note::of("C4"));

  ASSERT_FALSE(subject.low.has_value());
  ASSERT_TRUE(Note::of("C4") == (subject.high.value()));
}

TEST(Music_NoteRange, RangeFromNotes_HighOptional) {
  auto subject = NoteRange::from(Note::of("C4"), Note::atonal());

  ASSERT_TRUE(Note::of("C4") == (subject.low.value()));
  ASSERT_FALSE(subject.high.has_value());
}

TEST(Music_NoteRange, RangeFromStrings) {
  auto subject = NoteRange::from("C3", "C4");

  ASSERT_TRUE(Note::of("C3") == (subject.low.value()));
  ASSERT_TRUE(Note::of("C4") == (subject.high.value()));
}

TEST(Music_NoteRange, RangeFromStrings_LowOptional) {
  auto subject = NoteRange::from("X", "C4");

  ASSERT_FALSE(subject.low.has_value());
  ASSERT_TRUE(Note::of("C4") == (subject.high.value()));
}

TEST(Music_NoteRange, RangeFromStrings_HighOptional) {
  auto subject = NoteRange::from("C4", "X");

  ASSERT_TRUE(Note::of("C4") == (subject.low.value()));
  ASSERT_FALSE(subject.high.has_value());
}

TEST(Music_NoteRange, CopyOf) {
  auto subject = NoteRange::ofStrings(std::vector<std::string>{"C3", "E3", "D4", "E5", "F6"});
  auto cp = NoteRange::copyOf(subject);

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

  subject.expand(NoteRange::ofStrings(std::vector<std::string>{
      "G2",
      "G6"
  }));

  ASSERT_TRUE(Note::of("G2") == (subject.low.value()));
  ASSERT_TRUE(Note::of("G6") == (subject.high.value()));
}

TEST(Music_NoteRange, Median) {
  auto fromNulls = NoteRange::median(NoteRange::ofStrings(std::vector<std::string>{}), NoteRange::ofStrings({}));
  ASSERT_FALSE(fromNulls.low.has_value());
  ASSERT_FALSE(fromNulls.high.has_value());

  auto emptyHigh = NoteRange::median(NoteRange::ofStrings(std::vector<std::string>{"C5"}), NoteRange::ofStrings({}));
  assertNote("C5", emptyHigh.low.value());
  assertNote("C5", emptyHigh.high.value());

  auto emptyLow = NoteRange::median(NoteRange::ofStrings(std::vector<std::string>{}), NoteRange::ofStrings({"G#5"}));
  assertNote("G#5", emptyLow.low.value());
  assertNote("G#5", emptyLow.high.value());

  auto normal = NoteRange::median(NoteRange::ofStrings(std::vector<std::string>{"C5", "G5"}),
                                  NoteRange::ofStrings({"G#5", "B5"}));
  assertNote("E5", normal.low.value());
  assertNote("A5", normal.high.value());
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
  assertNote("C5", NoteRange::from("C3", "G6").getNoteNearestMedian(PitchClass::C).value());
  assertNote("C5", NoteRange::from("C3", "G7").getNoteNearestMedian(PitchClass::C).value());
  assertNote("C4", NoteRange::from("C2", "G6").getNoteNearestMedian(PitchClass::C).value());
  ASSERT_FALSE(NoteRange::from("C3", "G6").getNoteNearestMedian(PitchClass::Atonal).has_value());
}

TEST(Music_NoteRange, MedianNote) {
  assertNote("D#5", NoteRange::from("C5", "G5").getMedianNote().value());
  assertNote("G5", NoteRange::from("X", "G5").getMedianNote().value());
  assertNote("C5", NoteRange::from("C5", "X").getMedianNote().value());
  ASSERT_FALSE(NoteRange::empty().getMedianNote().has_value());
}

TEST(Music_NoteRange, Shifted) {
  auto input = NoteRange::from("C5", "G5");

  auto result = input.shifted(2);

  assertNote("D5", result.low.value());
  assertNote("A5", result.high.value());
  // input not modified
  assertNote("C5", input.low.value());
  assertNote("G5", input.high.value());
}

TEST(Music_NoteRange, IsEmpty) {
  ASSERT_TRUE(NoteRange::empty().isEmpty());
  ASSERT_FALSE(NoteRange::from("C5", "G5").isEmpty());
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
  ASSERT_FALSE(NoteRange::empty().includes(Note::of("C4")));
}

TEST(Music_NoteRange, ToAvailableOctave) {
  assertNote("C4", NoteRange::from(Note::of("C4"), Note::of("C6")).toAvailableOctave(Note::of("C4")));
  assertNote("C5", NoteRange::from(Note::of("C4"), Note::of("C6")).toAvailableOctave(Note::of("C5")));
  assertNote("C6", NoteRange::from(Note::of("C4"), Note::of("C6")).toAvailableOctave(Note::of("C6")));
  assertNote("D5", NoteRange::from(Note::of("C4"), Note::of("C6")).toAvailableOctave(Note::of("D6")));
  assertNote("D4", NoteRange::from(Note::of("C4"), Note::of("C6")).toAvailableOctave(Note::of("D3")));
  assertNote("C4", NoteRange::from(Note::of("C4"), Note::atonal()).toAvailableOctave(Note::of("C4")));
  assertNote("B3", NoteRange::from(Note::of("C4"), Note::atonal()).toAvailableOctave(Note::of("B3")));
  assertNote("D4", NoteRange::from(Note::of("C4"), Note::atonal()).toAvailableOctave(Note::of("D4")));
  assertNote("C4", NoteRange::from(Note::atonal(), Note::of("C4")).toAvailableOctave(Note::of("C4")));
  assertNote("B3", NoteRange::from(Note::atonal(), Note::of("C4")).toAvailableOctave(Note::of("B3")));
  assertNote("D4", NoteRange::from(Note::atonal(), Note::of("C4")).toAvailableOctave(Note::of("D4")));
  assertNote("C4", NoteRange::empty().toAvailableOctave(Note::of("C4")));
}

TEST(Music_NoteRange, ComputeMedianOptimalRangeShiftOctaves) {
  auto rangeA = NoteRange::from(Note::of("C3"), Note::of("B3"));
  auto rangeA_overlap = NoteRange::from(Note::of("G3"), Note::of("C6"));
  auto rangeB = NoteRange::from(Note::of("C5"), Note::of("B5"));
  auto rangeB_superset = NoteRange::from(Note::of("G2"), Note::of("D6"));

  auto rangeC1 = NoteRange::from(Note::of("F3"), Note::of("E4"));
  auto rangeC2 = NoteRange::from(Note::of("D3"), Note::of("C6"));

  ASSERT_EQ(2, NoteRange::computeMedianOptimalRangeShiftOctaves(rangeA, rangeB));
  ASSERT_EQ(-2, NoteRange::computeMedianOptimalRangeShiftOctaves(rangeB, rangeA));
  ASSERT_EQ(0, NoteRange::computeMedianOptimalRangeShiftOctaves(rangeB, rangeB_superset));
  ASSERT_EQ(0, NoteRange::computeMedianOptimalRangeShiftOctaves(rangeB_superset, rangeB));
  ASSERT_EQ(1, NoteRange::computeMedianOptimalRangeShiftOctaves(rangeA, rangeA_overlap));
  ASSERT_EQ(0, NoteRange::computeMedianOptimalRangeShiftOctaves(rangeC1, rangeC2));
  ASSERT_EQ(0, NoteRange::computeMedianOptimalRangeShiftOctaves(rangeC2, rangeC1));
}
