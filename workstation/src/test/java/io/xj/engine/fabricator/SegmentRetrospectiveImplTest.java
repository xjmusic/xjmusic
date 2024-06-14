// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.fabricator;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.xj.model.HubContent;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.enums.ProgramType;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.model.music.StickyBun;
import io.xj.model.pojos.Program;
import io.xj.model.pojos.ProgramSequenceBinding;
import io.xj.engine.FabricationException;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.FabricationTopology;
import io.xj.model.pojos.Chain;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.engine.SegmentFixtures.buildSegment;
import static io.xj.engine.SegmentFixtures.buildSegmentChoice;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class SegmentRetrospectiveImplTest {
  static int SEQUENCE_TOTAL_BEATS = 64;
  final UUID patternId = UUID.randomUUID();
  JsonProvider jsonProvider;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  SegmentEntityStore store;
  SegmentFixtures fake;
  Segment segment0;
  Segment segment1;
  Segment segment3;
  Segment segment4;

  @BeforeEach
  public void setUp() throws Exception {
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    store = new SegmentEntityStoreImpl(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    jsonProvider = new JsonProviderImpl();
    fabricatorFactory = new FabricatorFactoryImpl(
      store,
      jsonapiPayloadFactory,
      jsonProvider
    );
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
    Chain chain1 = store.put(SegmentFixtures.buildChain(fake.project1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));
    segment0 = constructSegmentAndChoices(chain1, SegmentType.CONTINUE, 10, 4, fake.program4, fake.program4_sequence1_binding0, fake.program15, fake.program15_sequence1_binding0);
    segment1 = constructSegmentAndChoices(chain1, SegmentType.NEXT_MAIN, 11, 0, fake.program4, fake.program4_sequence1_binding0, fake.program5, fake.program5_sequence0_binding0);
    constructSegmentAndChoices(chain1, SegmentType.CONTINUE, 12, 1, fake.program4, fake.program4_sequence1_binding0, fake.program5, fake.program5_sequence1_binding0);
    segment3 = constructSegmentAndChoices(chain1, SegmentType.CONTINUE, 13, 2, fake.program4, fake.program4_sequence1_binding0, fake.program5, fake.program5_sequence1_binding0);
    segment4 = constructSegmentAndChoices(chain1, SegmentType.NEXT_MACRO, 14, 0, fake.program3, fake.program3_sequence0_binding0, fake.program15, fake.program15_sequence0_binding0);
  }

  Segment constructSegmentAndChoices(Chain chain, SegmentType type, int offset, int delta, Program macro, ProgramSequenceBinding macroSB, Program main, ProgramSequenceBinding mainSB) throws FabricationException {
    var segment = store.put(SegmentFixtures.buildSegment(
      chain,
      type,
      offset,
      delta,
      SegmentState.CRAFTED,
      "D major",
      SEQUENCE_TOTAL_BEATS,
      0.73f,
      120.0f,
      String.format("chains-%s-segments-%s", ChainUtils.getIdentifier(chain), offset),
      true));
    store.put(buildSegmentChoice(
      segment,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      macro,
      macroSB));
    store.put(buildSegmentChoice(
      segment,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      main,
      mainSB));

    return segment;
  }

  @Test
  public void getPreviousChoiceOfType() throws FabricationException, FabricationFatalException {
    var subject = fabricatorFactory.loadRetrospective(segment3.getId());

    var result = subject.getPreviousChoiceOfType(ProgramType.Main);

    assertTrue(result.isPresent());
    assertEquals(result.get().getProgramId(), fake.program5.getId());
  }

  @Test
  public void getPreviousChoiceOfType_forNextMacroSegment() throws FabricationException, FabricationFatalException {
    var subject = fabricatorFactory.loadRetrospective(segment4.getId());

    var result = subject.getPreviousChoiceOfType(ProgramType.Main);

    assertTrue(result.isPresent());
    assertEquals(result.get().getProgramId(), fake.program5.getId());
  }

  @Test
  public void getPreviousChoiceOfType_forNextMainSegment() throws FabricationException, FabricationFatalException {
    var subject = fabricatorFactory.loadRetrospective(segment1.getId());

    var result = subject.getPreviousChoiceOfType(ProgramType.Main);

    assertTrue(result.isPresent());
    assertEquals(result.get().getProgramId(), fake.program15.getId());
  }

  /**
   Failure requiring a chain restart https://github.com/xjmusic/workstation/issues/263
   */
  @Test
  public void failureToReadMainChoiceIsFatal() throws FabricationException {
    for (SegmentChoice c : store.readAll(segment0.getId(), SegmentChoice.class))
      if (c.getProgramType().equals(ProgramType.Main))
        store.delete(segment0.getId(), SegmentChoice.class, c.getId());

    var e = assertThrows(FabricationFatalException.class, () -> fabricatorFactory.loadRetrospective(segment1.getId()));

    assertEquals("Retrospective sees no main choice!", e.getMessage());
  }

  /**
   Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://github.com/xjmusic/workstation/issues/222
   */
  @Test
  public void getPreviousMeta() throws FabricationException, FabricationFatalException, JsonProcessingException {
    var bun = new StickyBun(patternId, 1);
    var json = jsonProvider.getMapper().writeValueAsString(bun);
    store.put(SegmentFixtures.buildSegmentMeta(segment3, "StickyBun_0f650ae7-42b7-4023-816d-168759f37d2e", json));
    var subject = fabricatorFactory.loadRetrospective(segment4.getId());

    var result = subject.getPreviousMeta("StickyBun_0f650ae7-42b7-4023-816d-168759f37d2e");

    assertTrue(result.isPresent());
    assertEquals(json, result.get().getValue());
  }
}
