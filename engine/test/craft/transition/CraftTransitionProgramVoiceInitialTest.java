// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft.transition;

import io.xj.engine.fabricator->SegmentEntityStore;
import io.xj.engine.fabricator->SegmentEntityStoreImpl;
import io.xj.model.ContentEntityStore;
import io.xj.model.HubTopology;
import io.xj.model.TemplateConfig;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.entity.EntityUtils;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.engine.FabricationException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.engine.SegmentFixtures::buildSegment;
import static io.xj.engine.SegmentFixtures::buildSegmentChoice;

@ExtendWith(MockitoExtension.class)
public class CraftTransitionProgramVoiceInitialTest {
  Chain chain2;
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory * fabricatorFactory = nullptr;
  ContentEntityStore * sourceMaterial = nullptr;
  SegmentEntityStore *store = nullptr;
  ContentFixtures *fake = nullptr;
  Segment segment0;

  void SetUp() override {


    craftFactory = new CraftFactory();



    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // force known transition selection by destroying program 35
    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore(Stream.concat(
        fake->setupFixtureB1().stream(),
        fake->setupFixtureB3().stream())
      .filter(entity -> !EntityUtils.isSame(entity, fake->program35) && !EntityUtils.isChild(entity, fake->program35))
      .collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    chain2 = new Chain();
    chain2.setId(EntityUtils::computeUniqueId());
    chain2.setProjectId(fake->project1->id);
    chain2.name("Print #2");
    chain2.setTemplateConfig(TemplateConfig.DEFAULT);
    chain2.setType(Chain::Type::Production);
    chain2.setState(Chain::State::Fabricate);
    store->put(chain2);
  }

  @Test
  public void craftTransitionVoiceInitial()  {
    insertSegment();

    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment0->id, 48000.0f, 2, null);

    craftFactory->transition(fabricator).doWork();

//    auto result = store->getSegment(segment0->id).value();
//    assertFalse(store->getAll(result->id, SegmentChoice.class).empty());
//    
//    int pickedKick = 0;
//    int pickedSnare = 0;
//    int pickedBleep = 0;
//    int pickedToot = 0;
//    Collection<SegmentChoiceArrangementPick> picks = fabricator->getPicks();
//    for (SegmentChoiceArrangementPick pick : picks) {
//      if (pick.getInstrumentAudioId().equals(fake->instrument8_audio8kick->id))
//        pickedKick++;
//      if (pick.getInstrumentAudioId().equals(fake->instrument8_audio8snare->id))
//        pickedSnare++;
//      if (pick.getInstrumentAudioId().equals(fake->instrument8_audio8bleep->id))
//        pickedBleep++;
//      if (pick.getInstrumentAudioId().equals(fake->instrument8_audio8toot->id))
//        pickedToot++;
//    }
//    ASSERT_EQ(12, pickedKick);
//    ASSERT_EQ(12, pickedSnare);
//    ASSERT_EQ(4, pickedBleep);
//    ASSERT_EQ(4, pickedToot);
  }

  @Test
  public void craftTransitionVoiceInitial_okWhenNoTransitionChoice()  {
    insertSegment();
    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment0->id, 48000.0f, 2, null);

    craftFactory->transition(fabricator).doWork();
  }

  /**
   Insert fixture segment 6, including the transition choice only if specified
   */
  void insertSegment() throws FabricationException {
    segment0 = store->put(SegmentFixtures::buildSegment(
      chain2,
      0,
      Segment::State::Crafting,
      "D Major",
      32,
      0.55f,
      130.0f,
      "chains-1-segments-9f7s89d8a7892.wav"
    ));
    store->put(SegmentFixtures::buildSegmentChoice(segment0, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED, fake->program4, fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(segment0, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED, fake->program5, fake->program5_sequence0_binding0));
    for (std::string memeName : List.of("Special", "Wild", "Pessimism", "Outlook"))
      store->put(SegmentFixtures::buildSegmentMeme(segment0, memeName));

    store->put(SegmentFixtures::buildSegmentChord(segment0, 0.0f, "C minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment0, 8.0f, "Db minor"));
  }

}
