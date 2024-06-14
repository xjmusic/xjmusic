// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.macro_main;

import io.xj.engine.fabricator.SegmentEntityStore;
import io.xj.engine.fabricator.SegmentEntityStoreImpl;
import io.xj.model.HubContent;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.entity.EntityUtils;
import io.xj.model.enums.ProgramType;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.FabricationTopology;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator.FabricationFatalException;
import io.xj.engine.fabricator.Fabricator;
import io.xj.engine.fabricator.FabricatorFactory;
import io.xj.engine.fabricator.FabricatorFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChord;
import io.xj.model.pojos.SegmentMeme;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.engine.fabricator.SegmentUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.model.util.Assertion.assertSameItems;
import static io.xj.model.util.ValueUtils.MICROS_PER_MINUTE;
import static io.xj.engine.SegmentFixtures.buildSegment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class CraftFoundationNextMainTest {
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  SegmentFixtures fake;
  Chain chain1;
  Segment segment4;
  SegmentEntityStore store;
  HubContent sourceMaterial;

  @BeforeEach
  public void setUp() throws Exception {
    var jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    store = new SegmentEntityStoreImpl(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    fabricatorFactory = new FabricatorFactoryImpl(
      store,
      jsonapiPayloadFactory,
      jsonProvider
    );
    craftFactory = new CraftFactoryImpl();
    HubTopology.buildHubApiTopology(entityFactory);
    FabricationTopology.buildFabricationTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store.clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new SegmentFixtures();
    sourceMaterial = new HubContent(Stream.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB2().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(SegmentFixtures.buildChain(fake.project1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));
    store.put(SegmentFixtures.buildSegment(
      chain1,
      0,
      SegmentState.CRAFTED,
      "D major",
      64,
      0.73f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892"
    ));
    store.put(SegmentFixtures.buildSegment(
      chain1,
      1,
      SegmentState.CRAFTING,
      "Db minor",
      64,
      0.85f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav"
    ));

    // Chain "Test Print #1" has this segment that was just crafted
    Segment segment3 = store.put(SegmentFixtures.buildSegment(
      chain1,
      2,
      SegmentState.CRAFTED,
      "F Major",
      64,
      0.30f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav"
    ));
    store.put(SegmentFixtures.buildSegmentChoice(segment3, ProgramType.Macro, fake.program4_sequence0_binding0));
    store.put(SegmentFixtures.buildSegmentChoice(segment3, ProgramType.Main, fake.program5_sequence1_binding0));

    // Chain "Test Print #1" has a planned segment
    segment4 = store.put(SegmentFixtures.buildSegment(chain1, 3, SegmentState.PLANNED, "C", 8, 0.8f, 120, "chain-1-waveform-12345"));
  }

  @AfterEach
  public void tearDown() {

  }

  @Test
  public void craftFoundationNextMain() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4.getId(), 48000.0f, 2, null);

    craftFactory.macroMain(fabricator, null, null).doWork();

    Segment result = store.readSegment(segment4.getId()).orElseThrow();
    assertEquals(SegmentType.NEXT_MAIN, result.getType());
    assertEquals(16 * MICROS_PER_MINUTE / 140, (long) Objects.requireNonNull(result.getDurationMicros()));
    assertEquals(Integer.valueOf(16), result.getTotal());
    assertEquals(0.2, result.getIntensity(), 0.01);
    assertEquals("G -", result.getKey());
    assertEquals(140, result.getTempo(), 0.01);
    // assert memes
    assertSameItems(List.of("HINDSIGHT", "TROPICAL", "COZY", "WILD", "REGRET"),
      EntityUtils.namesOf(store.readAll(result.getId(), SegmentMeme.class)));
    // assert chords
    assertSameItems(List.of("G -", "Ab -"),
      EntityUtils.namesOf(store.readAll(result.getId(), SegmentChord.class)));
    // assert choices
    Collection<SegmentChoice> segmentChoices =
      store.readAll(result.getId(), SegmentChoice.class);
    // assert macro choice
    SegmentChoice macroChoice = SegmentUtils.findFirstOfType(segmentChoices, ProgramType.Macro);
    assertEquals(fake.program4_sequence1_binding0.getId(), macroChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(1), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
    // assert main choice
    SegmentChoice mainChoice = SegmentUtils.findFirstOfType(segmentChoices, ProgramType.Main);
    assertEquals(fake.program15_sequence0_binding0.getId(), mainChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
  }

  /**
   Engineer wants a Segment to be reverted, and re-queued for Craft, in the event that such a Segment has just failed its Craft process, in order to ensure Chain fabrication fault tolerance https://github.com/xjmusic/workstation/issues/297
   */
  @Test
  public void craftFoundationNextMain_revertsAndRequeueOnFailure() throws Exception {
    // Chain "Test Print #1" has a dangling (preceded by another planned segment) planned segment
    Segment segment5 = store.put(SegmentFixtures.buildSegment(
      chain1,
      4,
      SegmentState.PLANNED,
      "C",
      8,
      0.8f,
      120.0f,
      "chain-1-waveform-12345.wav"
    ));

    assertThrows(FabricationFatalException.class, () ->
      fabricatorFactory.fabricate(sourceMaterial, segment5.getId(), 48000.0f, 2, null));
  }

}
