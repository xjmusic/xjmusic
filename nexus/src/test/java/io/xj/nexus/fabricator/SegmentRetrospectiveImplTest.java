// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.fabricator;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.xj.hub.HubContent;
import io.xj.hub.HubTopology;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.hub.music.StickyBun;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.ChainUtils;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentMeta;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class SegmentRetrospectiveImplTest {
  static int SEQUENCE_TOTAL_BEATS = 64;
  final UUID patternId = UUID.randomUUID();
  @Mock
  public HubClient hubClient;
  JsonProvider jsonProvider;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  NexusEntityStore store;
  NexusIntegrationTestingFixtures fake;
  Segment segment0;
  Segment segment1;
  Segment segment3;
  Segment segment4;

  @BeforeEach
  public void setUp() throws Exception {
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    store = new NexusEntityStoreImpl(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    jsonProvider = new JsonProviderImpl();
    fabricatorFactory = new FabricatorFactoryImpl(
      store,
      jsonapiPayloadFactory,
      jsonProvider
    );
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store.clear();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Stream.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB2().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    Chain chain1 = store.put(NexusIntegrationTestingFixtures.buildChain(fake.project1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));
    segment0 = constructSegmentAndChoices(chain1, SegmentType.CONTINUE, 10, 4, fake.program4, fake.program4_sequence1_binding0, fake.program15, fake.program15_sequence1_binding0);
    segment1 = constructSegmentAndChoices(chain1, SegmentType.NEXT_MAIN, 11, 0, fake.program4, fake.program4_sequence1_binding0, fake.program5, fake.program5_sequence0_binding0);
    constructSegmentAndChoices(chain1, SegmentType.CONTINUE, 12, 1, fake.program4, fake.program4_sequence1_binding0, fake.program5, fake.program5_sequence1_binding0);
    segment3 = constructSegmentAndChoices(chain1, SegmentType.CONTINUE, 13, 2, fake.program4, fake.program4_sequence1_binding0, fake.program5, fake.program5_sequence1_binding0);
    segment4 = constructSegmentAndChoices(chain1, SegmentType.NEXT_MACRO, 14, 0, fake.program3, fake.program3_sequence0_binding0, fake.program15, fake.program15_sequence0_binding0);
  }

  Segment constructSegmentAndChoices(Chain chain, SegmentType type, int offset, int delta, Program macro, ProgramSequenceBinding macroSB, Program main, ProgramSequenceBinding mainSB) throws NexusException {
    var segment = store.put(buildSegment(
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
  public void getPreviousChoiceOfType() throws NexusException, FabricationFatalException {
    var subject = fabricatorFactory.loadRetrospective(segment3.getId());

    var result = subject.getPreviousChoiceOfType(ProgramType.Main);

    assertTrue(result.isPresent());
    assertEquals(result.get().getProgramId(), fake.program5.getId());
  }

  @Test
  public void getPreviousChoiceOfType_forNextMacroSegment() throws NexusException, FabricationFatalException {
    var subject = fabricatorFactory.loadRetrospective(segment4.getId());

    var result = subject.getPreviousChoiceOfType(ProgramType.Main);

    assertTrue(result.isPresent());
    assertEquals(result.get().getProgramId(), fake.program5.getId());
  }

  @Test
  public void getPreviousChoiceOfType_forNextMainSegment() throws NexusException, FabricationFatalException {
    var subject = fabricatorFactory.loadRetrospective(segment1.getId());

    var result = subject.getPreviousChoiceOfType(ProgramType.Main);

    assertTrue(result.isPresent());
    assertEquals(result.get().getProgramId(), fake.program15.getId());
  }

  /**
   Failure requiring a chain restart https://www.pivotaltracker.com/story/show/182131722
   */
  @Test
  public void failureToReadMainChoiceIsFatal() throws NexusException {
    for (SegmentChoice c : store.readAll(segment0.getId(), SegmentChoice.class))
      if (c.getProgramType().equals(ProgramType.Main))
        store.delete(segment0.getId(), SegmentChoice.class, c.getId());

    var e = assertThrows(FabricationFatalException.class, () -> fabricatorFactory.loadRetrospective(segment1.getId()));

    assertEquals("Retrospective sees no main choice!", e.getMessage());
  }

  /**
   Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787
   */
  @Test
  public void getPreviousMeta() throws NexusException, FabricationFatalException, JsonProcessingException {
    var bun = new StickyBun(patternId, 1);
    var json = jsonProvider.getMapper().writeValueAsString(bun);
    store.put(buildSegmentMeta(segment3, "StickyBun_0f650ae7-42b7-4023-816d-168759f37d2e", json));
    var subject = fabricatorFactory.loadRetrospective(segment4.getId());

    var result = subject.getPreviousMeta("StickyBun_0f650ae7-42b7-4023-816d-168759f37d2e");

    assertTrue(result.isPresent());
    assertEquals(json, result.get().getValue());
  }
}
