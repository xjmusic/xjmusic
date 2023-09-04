// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.hook;

import io.xj.hub.HubContent;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.LabUrlProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.notification.NotificationProvider;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubTopology;
import io.xj.nexus.model.*;
import io.xj.nexus.persistence.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.nexus.NexusIntegrationTestingFixtures.*;

@ExtendWith(MockitoExtension.class)
public class CraftHookContinueTest {
  @Mock
  public HubClient hubClient;
  @Mock
  public NotificationProvider notificationProvider;
  Chain chain1;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  NexusEntityStore store;
  NexusIntegrationTestingFixtures fake;
  Segment segment4;

  @BeforeEach
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    LabUrlProvider labUrlProvider = new LabUrlProvider("");
    craftFactory = new CraftFactoryImpl(labUrlProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    store = new NexusEntityStoreImpl(entityFactory);
    SegmentManager segmentManager = new SegmentManagerImpl(entityFactory, store);
    fabricatorFactory = new FabricatorFactoryImpl(
      segmentManager,
      jsonapiPayloadFactory,
      jsonProvider
    );

    // Manipulate the underlying entity store; reset before each test
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Stream.concat(
      Stream.concat(fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()),
      fake.setupFixtureB3().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" is fabricating segments
    chain1 = store.put(buildChain(fake.account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));
    store.put(buildSegment(
      chain1,
      SegmentType.INITIAL,
      0,
      0,
      SegmentState.CRAFTED,
      "D major",
      64,
      0.73,
      120.0,
      "chains-1-segments-9f7s89d8a7892",
      true));
    store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      1,
      1,
      SegmentState.CRAFTING,
      "Db minor",
      64,
      0.85,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav",
      true));
  }

  @AfterEach
  public void tearDown() {

  }

  @Test
  public void craftHookContinue() throws Exception {
    insertSegments3and4(false);
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4);

//    craftFactory.hook(fabricator).doWork();
//    // assert choice of hook-type sequence
//    Collection<SegmentChoice> segmentChoices =
//      store.getAll(segment4.getId(), SegmentChoice.class);
//    assertNotNull(Segments.findFirstOfType(segmentChoices, InstrumentType.Hook));
  }

  /**
   Insert fixture segments 3 and 4, including the hook choice for segment 3 only if specified

   @param excludeHookChoiceForSegment3 if desired for the purpose of this test
   */
  void insertSegments3and4(boolean excludeHookChoiceForSegment3) throws Exception {
    // segment just crafted
    Segment segment3 = store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      2,
      2,
      SegmentState.CRAFTED,
      "F Major",
      64,
      0.30,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store.put(buildSegmentChoice(
      segment3,
      SegmentUtils.DELTA_UNLIMITED,
      SegmentUtils.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment3,
      SegmentUtils.DELTA_UNLIMITED,
      SegmentUtils.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence0_binding0));
    if (!excludeHookChoiceForSegment3)
      store.put(buildSegmentChoice(
        segment3,
        SegmentUtils.DELTA_UNLIMITED,
        SegmentUtils.DELTA_UNLIMITED,
        fake.program35,
        InstrumentType.Hook,
        InstrumentMode.Loop));

    // segment crafting
    segment4 = store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      3,
      3,
      SegmentState.CRAFTING,
      "D Major",
      16,
      0.45,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav",
      true));
    store.put(buildSegmentChoice(
      segment4,
      SegmentUtils.DELTA_UNLIMITED,
      SegmentUtils.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment4,
      SegmentUtils.DELTA_UNLIMITED,
      SegmentUtils.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence1_binding0));
    for (String memeName : List.of("Cozy", "Classic", "Outlook", "Rosy"))
      store.put(buildSegmentMeme(segment4, memeName));
    store.put(buildSegmentChord(segment4, 0.0, "A minor"));
    store.put(buildSegmentChord(segment4, 8.0, "D Major"));
  }

  @Test
  public void craftHookContinue_okEvenWithoutPreviousSegmentHookChoice() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4);
    craftFactory.hook(fabricator).doWork();

/*
    // assert choice of hook-type sequence
    Collection<SegmentChoice> segmentChoices =
      store.getAll(segment4.getId(), SegmentChoice.class);
    assertNotNull(Segments.findFirstOfType(segmentChoices, InstrumentType.Hook));
*/
  }
}
