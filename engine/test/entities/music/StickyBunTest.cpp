// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/entities/music/StickyBun.h"

#include "../../_helper/TestHelpers.h"

using namespace XJ;


/**
 * Sticky buns v2 persisted for each randomly selected note in the series for any given event https://www.pivotaltracker.com/story/show/179153822
 */
static UUID eventId = "0f650ae7-42b7-4023-816d-168759f37d2e";

TEST(Music_StickyBun, getValues) {
  auto subject = StickyBun(eventId, 3);

  ASSERT_EQ(3, subject.values.size());
}

/**
 * super-key on program-sequence-event id, measuring delta from the first event seen in that event
 */
TEST(Music_StickyBun, getParentId) {
  auto subject = StickyBun(eventId, 3);

  ASSERT_EQ(eventId, subject.eventId);
}

/**
 * Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787
 */
TEST(Music_StickyBun, computeMetaKey) {
  ASSERT_EQ("StickyBun_0f650ae7-42b7-4023-816d-168759f37d2e", StickyBun(eventId, 1).computeMetaKey());
  ASSERT_EQ("StickyBun_0f650ae7-42b7-4023-816d-168759f37d2e", StickyBun::computeMetaKey(eventId));
}

/**
 * Replace any number of members of the set, when atonal, by computing the sticky bun
 */
TEST(Music_StickyBun, replaceAtonal) {
  auto source = std::vector{Note::of("Bb7"), Note::of("X"), Note::of("X"), Note::of("X")};
  auto voicingNotes = std::vector{Note::of("C4"), Note::of("E5"), Note::of("G6"), Note::of("Bb7")};
  auto bun = StickyBun(eventId, std::vector{42, 67, 100, 0});

  auto result = bun.replaceAtonal(source, voicingNotes);

  TestHelpers::assertNote("Bb7", result.at(0));
  TestHelpers::assertNote("G6", result.at(1));
  TestHelpers::assertNote("Bb7", result.at(2));
  TestHelpers::assertNote("C4", result.at(3));
}

/**
 * Pick one
 */
TEST(Music_StickyBun, compute) {
  auto voicingNotes = std::vector{Note::of("C4"), Note::of("E5"), Note::of("G6"), Note::of("Bb7")};
  auto bun = StickyBun(eventId, std::vector{42, 67, 100, 0});

  TestHelpers::assertNote("E5", bun.compute(voicingNotes, 0));
  TestHelpers::assertNote("G6", bun.compute(voicingNotes, 1));
  TestHelpers::assertNote("Bb7", bun.compute(voicingNotes, 2));
  TestHelpers::assertNote("C4", bun.compute(voicingNotes, 3));
}

/**
 * Serialize to JSON and deserialize
 */
TEST(Music_StickyBun, Serialize_Deserialize) {
  auto bun = StickyBun(eventId, std::vector{42, 67, 100, 0});

  auto json = bun.to_json();

  auto result = StickyBun::from_json(json);

  ASSERT_EQ(bun.eventId, result.eventId);
  ASSERT_EQ(bun.values, result.values);
}