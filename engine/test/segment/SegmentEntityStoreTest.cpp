// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "../_helper/ContentFixtures::h"
#include "../_helper/SegmentFixtures.h"
#include "xjmusic/segment/SegmentEntityStore.h"
#include "xjmusic/fabricator/FabricationException.h"

using namespace XJ;

class SegmentEntityStoreTest : public ::testing::Test {
protected:
  SegmentEntityStore subject;
  Chain fakeChain;
  Chain chain3;
  Project project1;
  Segment segment1;
  Segment segment2;
  Segment segment4;
  Segment segment5;
  Template template1;

  void SetUp() override {
    // Instantiate the test subject and put the payload
    subject.clear();

    // add base fixtures
    Project fakeProject = ContentFixtures::buildProject("fake");
    fakeChain = SegmentFixtures::buildChain(
        fakeProject,
        "Print #2",
        Chain::Type::Production,
        Chain::State::Fabricate,
        ContentFixtures::buildTemplate(fakeProject, "Test")
    );
    subject.put(fakeChain);
    project1 = ContentFixtures::buildProject("Testing");
    template1 = ContentFixtures::buildTemplate(project1, "Test Template 1", "test1");

    chain3 = subject.put(SegmentFixtures::buildChain(
        project1,
        template1,
        "Test Print #1",
        Chain::Type::Production,
        Chain::State::Fabricate
    ));

    // Chain "Test Print #1" has 5 sequential segments
    segment1 = subject.put(SegmentFixtures::buildSegment(
        chain3,
        Segment::Type::Initial,
        0,
        0,
        Segment::State::Crafted,
        "D major",
        64,
        0.73,
        120.0,
        "chains-1-segments-9f7s89d8a7892.wav",
        true));
    segment2 = SegmentFixtures::buildSegment(
        chain3,
        Segment::Type::Continue,
        1,
        64,
        Segment::State::Crafting,
        "Db minor",
        64,
        0.85,
        120.0,
        "chains-1-segments-9f7s89d8a7892.wav",
        true);
    segment2.waveformPreroll = 1.523;
    subject.put(segment2);
    subject.put(SegmentFixtures::buildSegment(
        chain3,
        Segment::Type::Continue,
        2,
        256,
        Segment::State::Crafted,
        "F major",
        64,
        0.30,
        120.0,
        "chains-1-segments-9f7s89d8a7892.wav",
        true));
    segment4 = subject.put(SegmentFixtures::buildSegment(
        chain3,
        Segment::Type::Continue,
        3,
        192,
        Segment::State::Crafting,
        "E minor",
        64,
        0.41,
        120.0,
        "chains-1-segments-9f7s89d8a7892.wav",
        true));
    segment5 = subject.put(SegmentFixtures::buildSegment(
        chain3,
        Segment::Type::Continue,
        4,
        245,
        Segment::State::Planned,
        "E minor",
        64,
        0.41,
        120.0,
        "chains-1-segments-9f7s89d8a7892", false));
  }

};


/**
 Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation https://github.com/xjmusic/xjmusic/issues/301
 */
TEST_F(SegmentEntityStoreTest, Create) {
  Segment inputData;
  inputData.id = 5;
  inputData.chainId = chain3.id;
  inputData.state = Segment::State::Planned;
  inputData.delta = 0;
  inputData.type = Segment::Type::Continue;
  inputData.beginAtChainMicros = 5 * 32 * ValueUtils::MICROS_PER_SECOND;
  inputData.durationMicros = 32 * ValueUtils::MICROS_PER_SECOND;
  inputData.total = 64;
  inputData.intensity = 0.74;
  inputData.waveformPreroll = 2.898;
  inputData.storageKey = "chains-1-segments-9f7s89d8a7892.wav";
  inputData.key = "C# minor 7 b9";
  inputData.tempo = 120.0;

  Segment result = subject.put(inputData);

  EXPECT_EQ(chain3.id, result.chainId);
  EXPECT_EQ(5, result.id);
  EXPECT_EQ(Segment::State::Planned, result.state);
  EXPECT_EQ(5 * 32 * ValueUtils::MICROS_PER_SECOND, static_cast<long>(result.beginAtChainMicros));
  EXPECT_EQ(32 * ValueUtils::MICROS_PER_SECOND, static_cast<long>(result.durationMicros.value()));
  EXPECT_EQ(64, result.total);
  EXPECT_NEAR(0.74, result.intensity, 0.01);
  EXPECT_EQ("C# minor 7 b9", result.key);
  EXPECT_NEAR(120.0f, result.tempo, 0.01);
  EXPECT_NEAR(2.898, result.waveformPreroll, 0.01);
  ASSERT_EQ("chains-1-segments-9f7s89d8a7892.wav", result.storageKey);
}


TEST_F(SegmentEntityStoreTest, Create_Get_Segment) {
  UUID chainId = EntityUtils::computeUniqueId();
  Segment segment;
  segment.chainId = chainId;
  segment.id = 0;
  segment.type = Segment::Type::NextMacro;
  segment.state = Segment::State::Crafted;
  segment.beginAtChainMicros = 0L;
  segment.durationMicros = 32 * ValueUtils::MICROS_PER_SECOND;
  segment.key = "D Major";
  segment.total = 64;
  segment.intensity = 0.73;
  segment.tempo = 120.0;
  segment.storageKey = "chains-1-segments-9f7s89d8a7892.wav";

  subject.put(segment);
  Segment result = subject.readSegment(segment.id).value();

  ASSERT_EQ(segment.id, result.id);
  ASSERT_EQ(chainId, result.chainId);
  ASSERT_EQ(0, result.id);
  ASSERT_EQ(Segment::Type::NextMacro, result.type);
  ASSERT_EQ(Segment::State::Crafted, result.state);
  ASSERT_EQ(0, static_cast<long>(result.beginAtChainMicros));
  ASSERT_EQ(32 * ValueUtils::MICROS_PER_SECOND, static_cast<long>(result.durationMicros.value()));
  ASSERT_EQ("D Major", result.key);
  ASSERT_EQ(64, result.total);
  ASSERT_NEAR(0.73f, result.intensity, 0.01);
  ASSERT_NEAR(120.0f, result.tempo, 0.01);
  ASSERT_EQ("chains-1-segments-9f7s89d8a7892.wav", result.storageKey);
}


TEST_F(SegmentEntityStoreTest, Create_Get_Chain) {
  UUID projectId = EntityUtils::computeUniqueId();
  Chain chain;
  chain.id = EntityUtils::computeUniqueId();
  chain.type = Chain::Type::Preview;
  chain.state = Chain::State::Fabricate;
  chain.shipKey = "super";

  subject.put(chain);
  auto result = subject.readChain().value();

  ASSERT_EQ(chain.id, result.id);
  ASSERT_EQ(Chain::Type::Preview, result.type);
  ASSERT_EQ(Chain::State::Fabricate, result.state);
  ASSERT_EQ("super", result.shipKey);
}


TEST_F(SegmentEntityStoreTest, CreateAll_ReadAll) {
  subject.clear();
  auto project1 = ContentFixtures::buildProject("fish");
  auto tmpl = ContentFixtures::buildTemplate(project1, "fishy");
  auto chain3 = subject.put(SegmentFixtures::buildChain(
      project1,
      "Test Print #3",
      Chain::Type::Production,
      Chain::State::Fabricate,
      tmpl,
      "key123"));
  auto program = ContentFixtures::buildProgram(Program::Type::Macro, "C", 120.0f);
  auto programSequence = ContentFixtures::buildProgramSequence(program, 8, "Hay", 0.6f, "G");
  auto programSequenceBinding = ContentFixtures::buildProgramSequenceBinding(programSequence, 0);
  Segment chain3_segment0 = subject.put(SegmentFixtures::buildSegment(chain3,
                                                                      0,
                                                                      Segment::State::Crafted,
                                                                      "D Major",
                                                                      64,
                                                                      0.73f,
                                                                      120.0f,
                                                                      "chains-3-segments-9f7s89d8a7892.wav"
  ));
  subject.put(
      SegmentFixtures::buildSegmentChoice(chain3_segment0, SegmentChoice::DELTA_UNLIMITED,
                                          SegmentChoice::DELTA_UNLIMITED, program,
                                          programSequenceBinding));
  // not in the above chain, won't be retrieved with it
  subject.put(SegmentFixtures::buildSegment(chain3,
                                            1,
                                            Segment::State::Crafted,
                                            "D Major",
                                            48,
                                            0.73f,
                                            120.0f,
                                            "chains-3-segments-d8a78929f7s89.wav"
  ));

  auto result = subject.readAllSegments();
  ASSERT_EQ(2, result.size());
  auto resultChoices = subject.readAllSegmentChoices(chain3_segment0.id);
  ASSERT_EQ(1, resultChoices.size());
}

TEST_F(SegmentEntityStoreTest, ReadSegment) {
  Segment result = subject.readSegment(segment2.id).value();

  ASSERT_EQ(segment2.id, result.id);
  ASSERT_EQ(chain3.id, result.chainId);
  ASSERT_EQ(1, result.id);
  ASSERT_EQ(Segment::State::Crafting, result.state);
  ASSERT_EQ(32 * ValueUtils::MICROS_PER_SECOND, (long) result.beginAtChainMicros);
  ASSERT_TRUE(result.durationMicros.has_value());
  ASSERT_EQ(32 * ValueUtils::MICROS_PER_SECOND, (long) result.durationMicros.value());
  ASSERT_EQ(64, result.total);
  ASSERT_NEAR(0.85f, result.intensity, 0.01);
  ASSERT_EQ("Db minor", result.key);
  ASSERT_NEAR(120.0f, result.tempo, 0.01);
  ASSERT_NEAR(1.523, result.waveformPreroll, 0.01);
}


TEST_F(SegmentEntityStoreTest, ReadLastSegmentId) {
  subject.put(SegmentFixtures::buildSegment(fakeChain,
                                            4,
                                            Segment::State::Crafted,
                                            "D Major",
                                            64,
                                            0.73f,
                                            120.0f,
                                            "chains-3-segments-9f7s89d8a7892.wav"
  ));

  ASSERT_EQ(4, subject.readLastSegmentId());
}


TEST_F(SegmentEntityStoreTest, ReadSegmentsFromToOffset) {
  auto result = subject.readSegmentsFromToOffset(2, 3);

  ASSERT_EQ(2, result.size());
  auto it = result.begin();
  Segment result1 = *it;
  ASSERT_EQ(Segment::State::Crafted, result1.state);
  ++it;
  Segment result2 = *it;
  ASSERT_EQ(Segment::State::Crafting, result2.state);
}


TEST_F(SegmentEntityStoreTest, ReadSegmentsFromToOffset_AcceptsNegativeOffsets_returnsEmptyCollection) {
  auto result = subject.readSegmentsFromToOffset(-1, -1);

  ASSERT_EQ(0L, result.size());
}


TEST_F(SegmentEntityStoreTest, ReadSegmentsFromToOffset_TrimsIfEndOffsetOutOfBounds) {
  auto result = subject.readSegmentsFromToOffset(2, 12);

  ASSERT_EQ(3L, result.size());
}


TEST_F(SegmentEntityStoreTest, ReadSegmentsFromToOffset_OnlyOneIfEndOffsetSameAsStart) {
  auto result = subject.readSegmentsFromToOffset(2, 2);

  ASSERT_EQ(1L, result.size());
}


TEST_F(SegmentEntityStoreTest, ReadSegmentsFromToOffset_EmptyIfStartOffsetOutOfBounds) {
  auto result = subject.readSegmentsFromToOffset(14, 17);

  ASSERT_EQ(0, result.size());
}


TEST_F(SegmentEntityStoreTest, ReadAllSegments) {
  auto result = subject.readAllSegments();

  ASSERT_EQ(5L, result.size());
  auto it = result.begin();

  Segment result0 = *it;
  ASSERT_EQ(Segment::State::Crafted, result0.state);

  ++it;
  Segment result1 = *it;
  ASSERT_EQ(Segment::State::Crafting, result1.state);

  ++it;
  Segment result2 = *it;
  ASSERT_EQ(Segment::State::Crafted, result2.state);

  ++it;
  Segment result3 = *it;
  ASSERT_EQ(Segment::State::Crafting, result3.state);

  ++it;
  Segment result4 = *it;
  ASSERT_EQ(Segment::State::Planned, result4.state);
}

/**
 List of Segments returned should not be more than a dozen or so https://github.com/xjmusic/xjmusic/issues/302
 */
TEST_F(SegmentEntityStoreTest, ReadAll_hasNoLimit) {
  Chain chain5 = subject.put(
      SegmentFixtures::buildChain(project1, "Test Print #1", Chain::Type::Production, Chain::State::Fabricate,
                                  template1, "barnacles"));
  for (int i = 0; i < 20; i++)
    subject.put(SegmentFixtures::buildSegment(
        chain5,
        Segment::Type::Continue,
        i,
        i * 64,
        Segment::State::Crafting,
        "C# minor 7 b9",
        64,
        0.74,
        120.0,
        "chains-1-segments-9f7s89d8a7892.wav",
        true));

  auto result = subject.readAllSegments();

  ASSERT_EQ(20L, result.size());
}


TEST_F(SegmentEntityStoreTest, UpdateSegment) {
  Segment inputData;
  inputData.id = 1;
  inputData.chainId = chain3.id;
  inputData.state = Segment::State::Crafted;
  inputData.delta = 0;
  inputData.type = Segment::Type::Continue;
  inputData.beginAtChainMicros = 4 * 32 * ValueUtils::MICROS_PER_SECOND;
  inputData.durationMicros = 32 * ValueUtils::MICROS_PER_SECOND;
  inputData.storageKey = "chains-1-segments-9f7s89d8a7892.wav";
  inputData.total = 64;
  inputData.intensity = 0.74;
  inputData.waveformPreroll = 0.0123;
  inputData.key = "C# minor 7 b9";
  inputData.tempo = 120.0;

  subject.updateSegment(inputData);

  Segment result = subject.readSegment(segment2.id).value();
  ASSERT_EQ("C# minor 7 b9", result.key);
  ASSERT_EQ(chain3.id, result.chainId);
  ASSERT_EQ(Segment::State::Crafted, result.state);
  ASSERT_NEAR(0.0123, result.waveformPreroll, 0.001);
  ASSERT_EQ(4 * 32 * ValueUtils::MICROS_PER_SECOND, (long) result.beginAtChainMicros);
  ASSERT_TRUE(result.durationMicros.has_value());
  ASSERT_EQ(32 * ValueUtils::MICROS_PER_SECOND, (long) result.durationMicros.value());
}

/**
 persist Segment content, then read prior Segment content
 */
TEST_F(SegmentEntityStoreTest, UpdateSegment_PersistPriorSegmentContent) {
  Segment segmentX;
  segmentX.id = 3;
  segmentX.type = Segment::Type::Continue;
  segmentX.delta = 0;
  segmentX.chainId = chain3.id;
  segmentX.state = Segment::State::Crafted;
  segmentX.beginAtChainMicros = 4 * 32 * ValueUtils::MICROS_PER_SECOND;
  segmentX.durationMicros = 32 * ValueUtils::MICROS_PER_SECOND;
  segmentX.total = 64;
  segmentX.intensity = 0.74;
  segmentX.key = "C# minor 7 b9";
  segmentX.storageKey = "chains-1-segments-9f7s89d8a7892.wav";
  segmentX.tempo = 120.0;

  subject.updateSegment(segmentX);

  Segment result = subject.readSegment(segment2.id).value();

  ASSERT_EQ("Db minor", result.key);
}


TEST_F(SegmentEntityStoreTest, UpdateSegment_FailsToTransitionFromPlannedToCrafted) {
  Segment inputData;
  inputData.id = 4;
  inputData.chainId = segment5.chainId;
  inputData.state = Segment::State::Crafted;
  inputData.delta = 0;
  inputData.type = Segment::Type::Continue;
  inputData.beginAtChainMicros = 4 * 32 * ValueUtils::MICROS_PER_SECOND;
  inputData.durationMicros = 32 * ValueUtils::MICROS_PER_SECOND;
  inputData.total = 64;
  inputData.intensity = 0.74;
  inputData.key = "C# minor 7 b9";
  inputData.tempo = 120.0;

  try {
    subject.updateSegment(inputData);
    FAIL() << "Expected FabricationException";
  } catch (const FabricationException &e) {
    EXPECT_TRUE(std::string(e.what()).find("transition to Crafted not in allowed") != std::string::npos);
  }
}


TEST_F(SegmentEntityStoreTest, UpdateSegment_FailsToChangeChain) {
  Segment inputData;
  inputData.id = 4;
  inputData.chainId = EntityUtils::computeUniqueId();
  inputData.delta = 0;
  inputData.type = Segment::Type::Continue;
  inputData.state = Segment::State::Crafting;
  inputData.beginAtChainMicros = 4 * 32 * ValueUtils::MICROS_PER_SECOND;
  inputData.durationMicros = 32 * ValueUtils::MICROS_PER_SECOND;
  inputData.total = 64;
  inputData.intensity = 0.74;
  inputData.key = "C# minor 7 b9";
  inputData.tempo = 120.0;

  try {
    subject.updateSegment(inputData);
    FAIL() << "Expected FabricationException";
  } catch (const FabricationException &e) {
    EXPECT_TRUE(std::string(e.what()).find("cannot modify chainId of a Segment") != std::string::npos);
  }
  Segment result = subject.readSegment(segment2.id).value();
  ASSERT_EQ("Db minor", result.key);
  ASSERT_EQ(chain3.id, result.chainId);
}


TEST_F(SegmentEntityStoreTest, GetSegmentCount) {
  int result = subject.getSegmentCount();

  ASSERT_EQ(5, result);
}


TEST_F(SegmentEntityStoreTest, IsSegmentsEmpty) {
  subject.clear();
  ASSERT_TRUE(subject.isEmpty());

  subject.put(SegmentFixtures::buildSegment(fakeChain,
                                            0,
                                            Segment::State::Crafted,
                                            "D Major",
                                            64,
                                            0.73f,
                                            120.0f,
                                            "chains-3-segments-9f7s89d8a7892.wav"
  ));

  ASSERT_FALSE(subject.isEmpty());
}


TEST_F(SegmentEntityStoreTest, DeleteSegment) {
  for (int i = 0; i < 10; i++)
    subject.put(SegmentFixtures::buildSegment(fakeChain,
                                              i,
                                              Segment::State::Crafted,
                                              "D Major",
                                              64,
                                              0.73f,
                                              120.0f,
                                              "chains-3-segments-9f7s89d8a7892.wav"
    ));

  subject.deleteSegment(5);

  ASSERT_EQ(9, subject.getSegmentCount());
  ASSERT_FALSE(subject.readSegment(5).has_value());
}


TEST_F(SegmentEntityStoreTest, DeleteSegmentsAfter) {
  for (int i = 0; i < 10; i++)
    subject.put(SegmentFixtures::buildSegment(fakeChain,
                                              i,
                                              Segment::State::Crafted,
                                              "D Major",
                                              64,
                                              0.73f,
                                              120.0f,
                                              "chains-3-segments-9f7s89d8a7892.wav"
    ));

  subject.deleteSegmentsAfter(5);

  ASSERT_EQ(6, subject.getSegmentCount());
  ASSERT_TRUE(subject.readSegment(0).has_value());
  ASSERT_TRUE(subject.readSegment(1).has_value());
  ASSERT_TRUE(subject.readSegment(2).has_value());
  ASSERT_TRUE(subject.readSegment(3).has_value());
  ASSERT_TRUE(subject.readSegment(4).has_value());
  ASSERT_TRUE(subject.readSegment(5).has_value());
  ASSERT_FALSE(subject.readSegment(6).has_value());
  ASSERT_FALSE(subject.readSegment(7).has_value());
  ASSERT_FALSE(subject.readSegment(8).has_value());
  ASSERT_FALSE(subject.readSegment(9).has_value());
}


TEST_F(SegmentEntityStoreTest, DeleteSegmentsBefore) {
  for (int i = 0; i < 10; i++)
    subject.put(SegmentFixtures::buildSegment(fakeChain,
                                              i,
                                              Segment::State::Crafted,
                                              "D Major",
                                              64,
                                              0.73f,
                                              120.0f,
                                              "chains-3-segments-9f7s89d8a7892.wav"
    ));

  subject.deleteSegmentsBefore(5);

  ASSERT_EQ(5, subject.getSegmentCount());
  ASSERT_FALSE(subject.readSegment(0).has_value());
  ASSERT_FALSE(subject.readSegment(1).has_value());
  ASSERT_FALSE(subject.readSegment(2).has_value());
  ASSERT_FALSE(subject.readSegment(3).has_value());
  ASSERT_FALSE(subject.readSegment(4).has_value());
  ASSERT_TRUE(subject.readSegment(5).has_value());
  ASSERT_TRUE(subject.readSegment(6).has_value());
  ASSERT_TRUE(subject.readSegment(7).has_value());
  ASSERT_TRUE(subject.readSegment(8).has_value());
  ASSERT_TRUE(subject.readSegment(9).has_value());
}

