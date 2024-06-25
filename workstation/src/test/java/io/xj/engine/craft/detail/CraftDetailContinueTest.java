// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.detail;

import io.xj.engine.FabricationTopology;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator.*;
import io.xj.model.HubContent;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.enums.*;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.engine.SegmentFixtures.buildChain;
import static io.xj.engine.SegmentFixtures.buildSegmentChoice;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class CraftDetailContinueTest {
  Chain chain1;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  SegmentEntityStore store;
  SegmentFixtures fake;
  Segment segment4;

  @BeforeEach
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    craftFactory = new CraftFactoryImpl();
    HubTopology.buildHubApiTopology(entityFactory);
    FabricationTopology.buildFabricationTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    store = new SegmentEntityStoreImpl(entityFactory);
    fabricatorFactory = new FabricatorFactoryImpl(
      store,
      jsonapiPayloadFactory,
      jsonProvider
    );

    // Manipulate the underlying entity store; reset before each test
    store.clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new SegmentFixtures();
    sourceMaterial = new HubContent(Stream.concat(
      Stream.concat(fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()),
      fake.setupFixtureB4_DetailBass().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" is fabricating segments
    chain1 = store.put(buildChain(
      fake.project1,
      fake.template1,
      "Test Print #1",
      ChainType.PRODUCTION,
      ChainState.FABRICATE
    ));
    store.put(SegmentFixtures.buildSegment(
      chain1,
      SegmentType.INITIAL,
      0,
      0,
      SegmentState.CRAFTED,
      "D major",
      64,
      0.73f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892",
      true));
    store.put(SegmentFixtures.buildSegment(
      chain1,
      SegmentType.CONTINUE,
      1,
      1,
      SegmentState.CRAFTING,
      "Db minor",
      64,
      0.85f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892",
      true));
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   Insert fixture segments 3 and 4, including the detail choice for segment 3 only if specified

   @param excludeDetailChoiceForSegment3 if desired for the purpose of this test
   */
  void insertSegments3and4(boolean excludeDetailChoiceForSegment3) throws Exception {
    // segment just crafted
    Segment segment3 = store.put(SegmentFixtures.buildSegment(chain1,
            SegmentType.CONTINUE,
            2,
            0,
            SegmentState.CRAFTED,
            "F Major",
            64,
            0.30f,
            120.0f,
            "chains-1-segments-9f7s89d8a7892",
            true));
    store.put(buildSegmentChoice(
            segment3,
            Segment.DELTA_UNLIMITED,
            Segment.DELTA_UNLIMITED,
            fake.program4,
            fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
            segment3,
            Segment.DELTA_UNLIMITED,
            Segment.DELTA_UNLIMITED,
            fake.program5,
            fake.program5_sequence0_binding0));
    if (!excludeDetailChoiceForSegment3)
      store.put(SegmentFixtures.buildSegmentChoice(
              segment3,
              Segment.DELTA_UNLIMITED,
              Segment.DELTA_UNLIMITED,
              fake.program10));

    // segment crafting
    segment4 = store.put(SegmentFixtures.buildSegment(
            chain1,
            SegmentType.CONTINUE,
            3,
            16,
            SegmentState.CRAFTING,
            "D Major",
            16,
            0.45f,
            120.0f,
            "chains-1-segments-9f7s89d8a7892",
            true));
    store.put(buildSegmentChoice(
            segment4,
            Segment.DELTA_UNLIMITED,
            Segment.DELTA_UNLIMITED,
            fake.program4,
            fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
            segment4,
            Segment.DELTA_UNLIMITED,
            Segment.DELTA_UNLIMITED,
            fake.program5,
            fake.program5_sequence1_binding0));
    for (String memeName : List.of("Cozy", "Classic", "Outlook", "Rosy"))
      store.put(SegmentFixtures.buildSegmentMeme(segment4, memeName));
    SegmentChord chord0 = store.put(SegmentFixtures.buildSegmentChord(segment4, 0.0f, "A minor"));
    store.put(SegmentFixtures.buildSegmentChordVoicing(chord0, InstrumentType.Bass, "A2, C3, E3"));
    SegmentChord chord1 = store.put(SegmentFixtures.buildSegmentChord(segment4, 8.0f, "D major"));
    store.put(SegmentFixtures.buildSegmentChordVoicing(chord1, InstrumentType.Bass, "D2, F#2, A2"));
  }

  @Test
  public void craftDetailContinue() throws Exception {
    insertSegments3and4(false);
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4.getId(), 48000.0f, 2, null);

    craftFactory.detail(fabricator).doWork();
    // assert choice of detail-type sequence
    Collection<SegmentChoice> segmentChoices =
      store.readAll(segment4.getId(), SegmentChoice.class);
    assertNotNull(SegmentUtils.findFirstOfType(segmentChoices, ProgramType.Detail));
  }

  @Test
  public void craftDetailContinue_okEvenWithoutPreviousSegmentDetailChoice() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4.getId(), 48000.0f, 2, null);
    craftFactory.detail(fabricator).doWork();

    // assert choice of detail-type sequence
    Collection<SegmentChoice> segmentChoices =
      store.readAll(segment4.getId(), SegmentChoice.class);
    assertNotNull(SegmentUtils.findFirstOfType(segmentChoices, ProgramType.Detail));
  }
}
