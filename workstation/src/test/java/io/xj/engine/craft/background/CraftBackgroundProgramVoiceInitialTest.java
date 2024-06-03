// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.background;

import io.xj.model.HubContent;
import io.xj.model.HubTopology;
import io.xj.model.TemplateConfig;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.entity.EntityUtils;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.engine.FabricationException;
import io.xj.engine.FabricationContentTwoFixtures;
import io.xj.engine.FabricationTopology;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator.Fabricator;
import io.xj.engine.fabricator.FabricatorFactory;
import io.xj.engine.fabricator.FabricatorFactoryImpl;
import io.xj.engine.model.Chain;
import io.xj.engine.model.ChainState;
import io.xj.engine.model.ChainType;
import io.xj.engine.model.Segment;
import io.xj.engine.model.SegmentState;
import io.xj.engine.fabricator.FabricationEntityStore;
import io.xj.engine.fabricator.FabricationEntityStoreImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.engine.FabricationContentTwoFixtures.buildSegment;
import static io.xj.engine.FabricationContentTwoFixtures.buildSegmentChoice;
import static io.xj.engine.FabricationContentTwoFixtures.buildSegmentChord;
import static io.xj.engine.FabricationContentTwoFixtures.buildSegmentMeme;

@ExtendWith(MockitoExtension.class)
public class CraftBackgroundProgramVoiceInitialTest {
  Chain chain2;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  FabricationEntityStore store;
  FabricationContentTwoFixtures fake;
  Segment segment0;

  @BeforeEach
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    craftFactory = new CraftFactoryImpl();
    HubTopology.buildHubApiTopology(entityFactory);
    FabricationTopology.buildFabricationTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    store = new FabricationEntityStoreImpl(entityFactory);
    fabricatorFactory = new FabricatorFactoryImpl(
      store,
      jsonapiPayloadFactory,
      jsonProvider
    );

    // Manipulate the underlying entity store; reset before each test
    store.clear();

    // force known background selection by destroying program 35
    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new FabricationContentTwoFixtures();
    sourceMaterial = new HubContent(Stream.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB3().stream())
      .filter(entity -> !EntityUtils.isSame(entity, fake.program35) && !EntityUtils.isChild(entity, fake.program35))
      .collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    chain2 = new Chain();
    chain2.setId(UUID.randomUUID());
    chain2.setProjectId(fake.project1.getId());
    chain2.name("Print #2");
    chain2.setTemplateConfig(TemplateConfig.DEFAULT);
    chain2.setType(ChainType.PRODUCTION);
    chain2.setState(ChainState.FABRICATE);
    store.put(chain2);
  }

  @Test
  public void craftBackgroundVoiceInitial() throws Exception {
    insertSegment();

    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment0.getId(), 48000.0f, 2, null);

    craftFactory.background(fabricator).doWork();

//    Segment result = store.getSegment(segment0.getId()).orElseThrow();
//    assertFalse(store.getAll(result.getId(), SegmentChoice.class).isEmpty());
//    
//    int pickedKick = 0;
//    int pickedSnare = 0;
//    int pickedBleep = 0;
//    int pickedToot = 0;
//    Collection<SegmentChoiceArrangementPick> picks = fabricator.getPicks();
//    for (SegmentChoiceArrangementPick pick : picks) {
//      if (pick.getInstrumentAudioId().equals(fake.instrument8_audio8kick.getId()))
//        pickedKick++;
//      if (pick.getInstrumentAudioId().equals(fake.instrument8_audio8snare.getId()))
//        pickedSnare++;
//      if (pick.getInstrumentAudioId().equals(fake.instrument8_audio8bleep.getId()))
//        pickedBleep++;
//      if (pick.getInstrumentAudioId().equals(fake.instrument8_audio8toot.getId()))
//        pickedToot++;
//    }
//    assertEquals(12, pickedKick);
//    assertEquals(12, pickedSnare);
//    assertEquals(4, pickedBleep);
//    assertEquals(4, pickedToot);
  }

  @Test
  public void craftBackgroundVoiceInitial_okWhenNoBackgroundChoice() throws Exception {
    insertSegment();
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment0.getId(), 48000.0f, 2, null);

    craftFactory.background(fabricator).doWork();
  }

  /**
   Insert fixture segment 6, including the background choice only if specified
   */
  void insertSegment() throws FabricationException {
    segment0 = store.put(FabricationContentTwoFixtures.buildSegment(
      chain2,
      0,
      SegmentState.CRAFTING,
      "D Major",
      32,
      0.55f,
      130.0f,
      "chains-1-segments-9f7s89d8a7892.wav"
    ));
    store.put(buildSegmentChoice(segment0, Segment.DELTA_UNLIMITED, Segment.DELTA_UNLIMITED, fake.program4, fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(segment0, Segment.DELTA_UNLIMITED, Segment.DELTA_UNLIMITED, fake.program5, fake.program5_sequence0_binding0));
    for (String memeName : List.of("Special", "Wild", "Pessimism", "Outlook"))
      store.put(FabricationContentTwoFixtures.buildSegmentMeme(segment0, memeName));

    store.put(FabricationContentTwoFixtures.buildSegmentChord(segment0, 0.0f, "C minor"));
    store.put(FabricationContentTwoFixtures.buildSegmentChord(segment0, 8.0f, "Db minor"));
  }

}
