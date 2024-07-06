// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/craft/MacroMainCraft.h"
#include "xjmusic/fabricator/ChainUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

/**
 Choose next Macro program based on the memes of the last sequence from the previous Macro program https://github.com/xjmusic/xjmusic/issues/299
 */
class MacroFromOverlappingMemeSequencesTest : public testing::Test {
protected:
  int REPEAT_TIMES = 100;
  Program macro2a;
  std::unique_ptr<ContentEntityStore> sourceMaterial;
  std::unique_ptr<SegmentEntityStore> store;
  const Segment *segment2 = nullptr;

  void SetUp() override {
    store = std::make_unique<SegmentEntityStore>();


    // Mock request via HubClientFactory returns fake generated library of model content
    // Project "bananas"
    auto project1 = ContentFixtures::buildProject("bananas");
    auto library2 = ContentFixtures::buildLibrary(&project1, "house");
    auto template1 = ContentFixtures::buildTemplate(&project1, "Test Template 1", "test1");
    TemplateBinding templateBinding1 = ContentFixtures::buildTemplateBinding(&template1, &library2);

    // Macro Program already chosen for previous segment
    auto macro1 = ContentFixtures::buildProgram(&library2, Program::Type::Macro, Program::State::Published,
                                                "Chosen Macro", "C", 120.0f);
    auto macro1_meme = ContentFixtures::buildMeme(&macro1, "Tropical");
    auto macro1_sequenceA = ContentFixtures::buildSequence(&macro1, 0, "Start Wild", 0.6f, "C");
    auto macro1_sequenceA_binding = ContentFixtures::buildBinding(&macro1_sequenceA, 0);
    auto macro1_sequenceA_bindingMeme = ContentFixtures::buildMeme(&macro1_sequenceA_binding, "Red");
    ProgramSequence macro1_sequenceB = ContentFixtures::buildSequence(&macro1, 0, "Intermediate", 0.4f, "Bb minor");
    auto macro1_sequenceB_binding = ContentFixtures::buildBinding(&macro1_sequenceB, 1);
    auto macro1_sequenceB_bindingMeme = ContentFixtures::buildMeme(&macro1_sequenceB_binding, "Green");

    // Main Program already chosen for previous segment
    auto main5 = ContentFixtures::buildProgram(&library2, Program::Type::Main, Program::State::Published, "Chosen Main",
                                               "C", 120.0f);
    auto main5_meme = ContentFixtures::buildMeme(&main5, "Tropical");
    auto main5_sequenceA = ContentFixtures::buildSequence(&main5, 0, "Start Wild", 0.6f, "C");
    ProgramSequenceBinding main5_sequenceA_binding = ContentFixtures::buildBinding(&main5_sequenceA, 0);

    // Macro Program will be chosen because of matching meme
    macro2a = ContentFixtures::buildProgram(&library2, Program::Type::Macro, Program::State::Published, "Always Chosen",
                                            "C", 120.0f);
    auto macro2a_meme = ContentFixtures::buildMeme(&macro2a, "Tropical");
    auto macro2a_sequenceA = ContentFixtures::buildSequence(&macro2a, 0, "Start Wild", 0.6f, "C");
    auto macro2a_sequenceA_binding = ContentFixtures::buildBinding(&macro2a_sequenceA, 0);
    auto macro2a_sequenceA_bindingMeme = ContentFixtures::buildMeme(&macro2a_sequenceA_binding, "Green");

    // Macro Program will NEVER be chosen because of non-matching meme
    auto macro2b = ContentFixtures::buildProgram(&library2, Program::Type::Macro, Program::State::Published,
                                                 "Never Chosen", "C", 120.0f);
    auto macro2b_meme = ContentFixtures::buildMeme(&macro2a, "Tropical");
    auto macro2b_sequenceA = ContentFixtures::buildSequence(&macro2a, 0, "Start Wild", 0.6f, "C");
    auto macro2b_sequenceA_binding = ContentFixtures::buildBinding(&macro2b_sequenceA, 0);
    auto macro2b_sequenceA_bindingMeme = ContentFixtures::buildMeme(&macro2b_sequenceA_binding, "Purple");

    sourceMaterial = std::make_unique<ContentEntityStore>();
    sourceMaterial->put(project1);
    sourceMaterial->put(library2);
    sourceMaterial->put(macro1);
    sourceMaterial->put(macro1_meme);
    sourceMaterial->put(macro1_sequenceA);
    sourceMaterial->put(macro1_sequenceA_binding);
    sourceMaterial->put(macro1_sequenceA_bindingMeme);
    sourceMaterial->put(macro1_sequenceB);
    sourceMaterial->put(macro1_sequenceB_binding);
    sourceMaterial->put(macro1_sequenceB_bindingMeme);
    sourceMaterial->put(main5);
    sourceMaterial->put(main5_meme);
    sourceMaterial->put(main5_sequenceA);
    sourceMaterial->put(main5_sequenceA_binding);
    sourceMaterial->put(macro2a);
    sourceMaterial->put(macro2a_meme);
    sourceMaterial->put(macro2a_sequenceA);
    sourceMaterial->put(macro2a_sequenceA_binding);
    sourceMaterial->put(macro2a_sequenceA_bindingMeme);
    sourceMaterial->put(macro2b);
    sourceMaterial->put(macro2b_meme);
    sourceMaterial->put(macro2b_sequenceA);
    sourceMaterial->put(macro2b_sequenceA_binding);
    sourceMaterial->put(macro2b_sequenceA_bindingMeme);
    sourceMaterial->put(template1);
    sourceMaterial->put(templateBinding1);

    // Chain "Test Print #1" has 5 total segments
    const auto chain1 = store->put(
        SegmentFixtures::buildChain("Test Print #1", Chain::Type::Production, Chain::State::Fabricate, &template1, ""));
    auto segment1 = store->put(SegmentFixtures::buildSegment(
        chain1,
        0,
        Segment::State::Crafted,
        "D major",
        64,
        0.73f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892"));
    store->put(SegmentFixtures::buildSegmentChoice(segment1, Program::Type::Macro, &macro1_sequenceA_binding));
    store->put(SegmentFixtures::buildSegmentChoice(segment1, Program::Type::Main, &main5_sequenceA_binding));

    segment2 = store->put(SegmentFixtures::buildSegment(
        chain1,
        1,
        Segment::State::Crafting,
        "Db minor",
        64,
        0.85f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav"));
  }
};

TEST_F(MacroFromOverlappingMemeSequencesTest, ChooseNextMacroProgram_alwaysBasedOnOverlappingMemes) {
  const auto retrospective = SegmentRetrospective(store.get(), segment2->id);
  auto fabricator = Fabricator(sourceMaterial.get(), store.get(), &retrospective, segment2->id, std::nullopt);
  auto subject = MacroMainCraft(&fabricator, std::nullopt, {});

  // This test is repeated many times to ensure the correct function of macro choice
  // At 100 repetitions, false positive is 2^100:1 against
  for (int i = 0; i < REPEAT_TIMES; i++) {
    const auto result = subject.chooseMacroProgram();
    ASSERT_EQ(macro2a.id, result->id);
  }
}
