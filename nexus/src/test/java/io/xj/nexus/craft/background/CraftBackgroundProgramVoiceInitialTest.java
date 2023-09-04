// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.background;

import io.xj.hub.HubContent;
import io.xj.hub.TemplateConfig;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.entity.EntityUtils;
import io.xj.lib.LabUrlProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.notification.NotificationProvider;
import io.xj.nexus.NexusException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.nexus.NexusIntegrationTestingFixtures.*;

@ExtendWith(MockitoExtension.class)
public class CraftBackgroundProgramVoiceInitialTest {
  @Mock
  public HubClient hubClient;
  @Mock
  public NotificationProvider notificationProvider;
  Chain chain2;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  NexusEntityStore store;
  NexusIntegrationTestingFixtures fake;
  Segment segment0;

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

    // force known background selection by destroying program 35
    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Stream.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB3().stream())
      .filter(entity -> !EntityUtils.isSame(entity, fake.program35) && !EntityUtils.isChild(entity, fake.program35))
      .collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    chain2 = new Chain();
    chain2.setId(UUID.randomUUID());
    chain2.setAccountId(fake.account1.getId());
    chain2.name("Print #2");
    chain2.setTemplateConfig(TemplateConfig.DEFAULT);
    chain2.setType(ChainType.PRODUCTION);
    chain2.setState(ChainState.FABRICATE);
    store.put(chain2);
  }

  @Test
  public void craftBackgroundVoiceInitial() throws Exception {
    insertSegment();

    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment0);

    craftFactory.background(fabricator).doWork();

//    Segment result = store.getSegment(segment0.getId()).orElseThrow();
//    assertFalse(store.getAll(result.getId(), SegmentChoice.class).isEmpty());
//    // test vector for persist Audio pick in memory https://www.pivotaltracker.com/story/show/154014731
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
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment0);

    craftFactory.background(fabricator).doWork();
  }

  /**
   Insert fixture segment 6, including the background choice only if specified
   */
  void insertSegment() throws NexusException {
    segment0 = store.put(buildSegment(
      chain2,
      0,
      SegmentState.CRAFTING,
      "D Major",
      32,
      0.55,
      130.0,
      "chains-1-segments-9f7s89d8a7892.wav"
    ));
    store.put(buildSegmentChoice(segment0, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, fake.program4, fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(segment0, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, fake.program5, fake.program5_sequence0_binding0));
    for (String memeName : List.of("Special", "Wild", "Pessimism", "Outlook"))
      store.put(buildSegmentMeme(segment0, memeName));

    store.put(buildSegmentChord(segment0, 0.0, "C minor"));
    store.put(buildSegmentChord(segment0, 8.0, "Db minor"));
  }

}
