// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "../_helper/ContentFixtures.h"
#include "../_helper/SegmentFixtures.h"
#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/fabricator/FabricationFatalException.h"
#include "xjmusic/music/StickyBun.h"

#include <xjmusic/fabricator/SegmentRetrospective.h>
#include <xjmusic/segment/SegmentEntityStore.h>

namespace XJ {
  class SegmentEntityStore;
}
using namespace XJ;

class SegmentRetrospectiveTest : public testing::Test {
protected:
  int SEQUENCE_TOTAL_BEATS = 64;
  UUID patternId = EntityUtils::computeUniqueId();
  std::unique_ptr<ContentEntityStore> sourceMaterial = std::make_unique<ContentEntityStore>();
  std::unique_ptr<SegmentEntityStore> store = std::make_unique<SegmentEntityStore>();
  ContentFixtures fake;
  Segment segment0;
  Segment segment1;
  Segment segment3;
  Segment segment4;

  void SetUp() override {
    // Manipulate the underlying entity store; reset before each test
    store.get()->clear();
    sourceMaterial->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake.setupFixtureB1(sourceMaterial.get());
    fake.setupFixtureB2(sourceMaterial.get());

    // Chain "Test Print #1" has 5 total segments
    Chain *chain1 = store->put(
        SegmentFixtures::buildChain("Test Print #1", Chain::Type::Production, Chain::State::Fabricate, &fake.template1));
    segment0 = constructSegmentAndChoices(chain1, Segment::Type::Continue, 10, 4, &fake.program4,
                                          &fake.program4_sequence1_binding0, &fake.program15,
                                          &fake.program15_sequence1_binding0);
    segment1 = constructSegmentAndChoices(chain1, Segment::Type::NextMain, 11, 0, &fake.program4,
                                          &fake.program4_sequence1_binding0, &fake.program5,
                                          &fake.program5_sequence0_binding0);
    constructSegmentAndChoices(chain1, Segment::Type::Continue, 12, 1, &fake.program4, &fake.program4_sequence1_binding0,
                               &fake.program5, &fake.program5_sequence1_binding0);
    segment3 = constructSegmentAndChoices(chain1, Segment::Type::Continue, 13, 2, &fake.program4,
                                          &fake.program4_sequence1_binding0, &fake.program5,
                                          &fake.program5_sequence1_binding0);
    segment4 = constructSegmentAndChoices(chain1, Segment::Type::NextMacro, 14, 0, &fake.program3,
                                          &fake.program3_sequence0_binding0, &fake.program15,
                                          &fake.program15_sequence0_binding0);
  }

  Segment constructSegmentAndChoices(
      Chain *chain,
      const Segment::Type type,
      const int offset,
      const int delta,
      const Program *macro,
      const ProgramSequenceBinding *macroSB,
      const Program *main,
      const ProgramSequenceBinding *mainSB) const {
    const auto segment = store->put(SegmentFixtures::buildSegment(
        chain,
        type,
        offset,
        delta,
        Segment::State::Crafted,
        "D major",
        SEQUENCE_TOTAL_BEATS,
        0.73f,
        120.0f,
        "chains-" + ChainUtils::getIdentifier(chain) + "-segments-" + std::to_string(offset),
        true));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        macro,
        macroSB));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        main,
        mainSB));

    return *segment;
  }

  /**
   Create a retrospective to fabricate a particular segment
   <p>
   Fabricator content = contentFactory.workOn(segment);
   ... do things with this content, like craft or dub ...
   content.putReport();

   @param segmentId Segment that's currently on the workbench
   @return SegmentRetrospective
   @            on retry-able network or service failure
   @throws FabricationFatalException on failure requiring a chain restart https://github.com/xjmusic/xjmusic/issues/263
   */
  virtual SegmentRetrospective loadRetrospective(const int segmentId) {
    return SegmentRetrospective(store.get(), segmentId);
  }
};

TEST_F(SegmentRetrospectiveTest, GetPreviousChoiceOfType) {
  const auto subject = loadRetrospective(segment3.id);

  const auto result = subject.getPreviousChoiceOfType(Program::Type::Main);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(result.value()->programId, fake.program5.id);
}

TEST_F(SegmentRetrospectiveTest, getPreviousChoiceOfType_forNextMacroSegment) {
  const auto subject = loadRetrospective(segment4.id);

  const auto result = subject.getPreviousChoiceOfType(Program::Type::Main);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(result.value()->programId, fake.program5.id);
}

TEST_F(SegmentRetrospectiveTest, GetPreviousChoiceOfType_forNextMainSegment) {
  const auto subject = loadRetrospective(segment1.id);

  const auto result = subject.getPreviousChoiceOfType(Program::Type::Main);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(result.value()->programId, fake.program15.id);
}

/**
 Failure requiring a chain restart https://github.com/xjmusic/xjmusic/issues/263
 */
TEST_F(SegmentRetrospectiveTest, FailureToReadMainChoiceIsFatal) {
  for (const SegmentChoice *c: store->readAllSegmentChoices(segment0.id))
    if (c->programType == Program::Type::Main)
      store->deleteSegmentChoice(segment0.id, c->id);

  try {
    loadRetrospective(segment1.id);
    FAIL() << "Expected FabricationFatalException";
  } catch (const FabricationFatalException &e) {
    EXPECT_TRUE(std::string(e.what()).find("Retrospective sees no main choice!") != std::string::npos);
  }
}

/**
 Failure to load first chain in segment for retrospective https://github.com/xjmusic/xjmusic/issues/263
 */
TEST_F(SegmentRetrospectiveTest, FailureToReadFirstSegmentIsFatal) {
  try {
    loadRetrospective(segment0.id);
    FAIL() << "Expected FabricationFatalException";
  } catch (const FabricationFatalException &e) {
    EXPECT_TRUE(std::string(e.what()).find("Retrospective sees no previous segment!") != std::string::npos);
  }
}

/**
 Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://github.com/xjmusic/xjmusic/issues/222
 */
TEST_F(SegmentRetrospectiveTest, GetPreviousMeta) {
  auto bun = StickyBun(patternId, 1);
  const std::string json = bun.serialize();
  store->put(SegmentFixtures::buildSegmentMeta(&segment3, "StickyBun_0f650ae7-42b7-4023-816d-168759f37d2e", json));
  const auto subject = loadRetrospective(segment4.id);

  const auto result = subject.getPreviousMeta("StickyBun_0f650ae7-42b7-4023-816d-168759f37d2e");

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(json, result.value()->value);
}
