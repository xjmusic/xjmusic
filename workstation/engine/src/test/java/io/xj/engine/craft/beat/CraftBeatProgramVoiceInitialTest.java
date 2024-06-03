// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.beat;

import io.xj.hub.HubContent;
import io.xj.hub.HubTopology;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.entity.EntityUtils;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.engine.FabricationException;
import io.xj.engine.NexusIntegrationTestingFixtures;
import io.xj.engine.NexusTopology;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator.Fabricator;
import io.xj.engine.fabricator.FabricatorFactory;
import io.xj.engine.fabricator.FabricatorFactoryImpl;
import io.xj.engine.model.Chain;
import io.xj.engine.model.ChainState;
import io.xj.engine.model.ChainType;
import io.xj.engine.model.Segment;
import io.xj.engine.model.SegmentChoice;
import io.xj.engine.model.SegmentState;
import io.xj.engine.model.SegmentType;
import io.xj.engine.fabricator.FabricationEntityStore;
import io.xj.engine.fabricator.FabricationEntityStoreImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.engine.NexusHubIntegrationTestingFixtures.buildTemplate;
import static io.xj.engine.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.engine.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.engine.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.engine.NexusIntegrationTestingFixtures.buildSegmentChord;
import static io.xj.engine.NexusIntegrationTestingFixtures.buildSegmentMeme;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
public class CraftBeatProgramVoiceInitialTest {
  Chain chain2;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  FabricationEntityStore store;
  NexusIntegrationTestingFixtures fake;
  Segment segment0;

  @BeforeEach
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    craftFactory = new CraftFactoryImpl();
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    store = new FabricationEntityStoreImpl(entityFactory);
    fabricatorFactory = new FabricatorFactoryImpl(
      store,
      jsonapiPayloadFactory,
      jsonProvider
    );

    // Manipulate the underlying entity store; reset before each test
    store.clear();

    // force known beat selection by destroying program 35
    // Mock request via HubClientFactory returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Stream.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB3().stream())
      .filter(entity -> !EntityUtils.isSame(entity, fake.program35) && !EntityUtils.isChild(entity, fake.program35))
      .collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    chain2 = store.put(buildChain(
      fake.project1,
      "Print #2",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      buildTemplate(fake.project1, "Tests")
    ));
  }

  @Test
  public void craftBeatVoiceInitial() throws Exception {
    insertSegment();

    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment0.getId(), 48000.0f, 2, null);

    craftFactory.beat(fabricator).doWork();

    Segment result = store.readSegment(segment0.getId()).orElseThrow();
    assertFalse(store.readAll(result.getId(), SegmentChoice.class).isEmpty());
    
  }

  @Test
  public void craftBeatVoiceInitial_okWhenNoBeatChoice() throws Exception {
    insertSegment();
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment0.getId(), 48000.0f, 2, null);

    craftFactory.beat(fabricator).doWork();
  }

  /**
   Insert fixture segment 6, including the beat choice only if specified
   */
  void insertSegment() throws FabricationException {
    segment0 = store.put(buildSegment(
      chain2,
      SegmentType.INITIAL,
      0,
      0,
      SegmentState.CRAFTING,
      "D Major",
      32,
      0.55f,
      130.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store.put(buildSegmentChoice(
      segment0,
      0,
      Segment.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment0,
      0,
      Segment.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence0_binding0));
    for (String memeName : List.of("Special", "Wild", "Pessimism", "Outlook"))
      store.put(buildSegmentMeme(segment0, memeName));

    store.put(buildSegmentChord(segment0, 0.0f, "C minor"));
    store.put(buildSegmentChord(segment0, 8.0f, "Db minor"));
  }

}
