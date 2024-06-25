// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft.macro_main;

import io.xj.engine.fabricator->SegmentEntityStoreImpl;
import io.xj.model.ContentEntityStore;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.FabricationTopology;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator->Fabricator;
import io.xj.engine.fabricator->FabricatorFactory;
import io.xj.engine.fabricator->FabricatorFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.engine.fabricator->SegmentEntityStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.xj.engine.SegmentFixtures::buildChain;
import static io.xj.engine.SegmentFixtures::buildSegment;
import static org.junit.jupiter.api.Assertions.ASSERT_EQ;

@ExtendWith(MockitoExtension.class)
public class CraftSegmentOutputEncoderTest {
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory * fabricatorFactory = nullptr;
  SegmentEntityStore *store = nullptr;
  Segment segment6;
  ContentEntityStore * sourceMaterial = nullptr;

  void SetUp() override {


    craftFactory = new CraftFactory();



    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    auto fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore(fake->setupFixtureB1());

    // Chain "Print #2" has 1 initial planned segment
    Chain chain2 = store->put(buildChain(
      fake->project1,
      "Print #2",
      Chain::Type::Production,
      Chain::State::Fabricate,
      fake->template1
    ));
    segment6 = store->put(SegmentFixtures::buildSegment(
      chain2,
      0,
      Segment::State::Planned,
      "C",
      8,
      0.8f,
      120.0f,
      "chain-1-waveform-12345.wav"));
  }

  @Test
  public void craftFoundationInitial()  {
    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment6->id, 48000.0f, 2, null);

    craftFactory->macroMain(fabricator, null, null).doWork();

    auto result = store->readSegment(segment6->id).value();
    ASSERT_EQ(segment6->id, result->id);
    ASSERT_EQ(Segment::Type::Initial, result->type);
  }
}
