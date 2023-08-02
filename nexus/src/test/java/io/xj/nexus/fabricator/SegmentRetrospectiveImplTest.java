// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.fabricator;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.xj.hub.HubTopology;
import io.xj.nexus.hub_client.HubClient;
import io.xj.hub.ingest.HubContent;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.music.StickyBun;
import io.xj.lib.notification.NotificationProvider;
import io.xj.nexus.NexusException;
import io.xj.test_fixtures.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.Chains;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManagerImpl;
import io.xj.nexus.persistence.Segments;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildSegmentMeta;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SegmentRetrospectiveImplTest {
  static int SEQUENCE_TOTAL_BEATS = 64;
  final UUID patternId = UUID.randomUUID();
  @Mock
  public HubClient hubClient;
  @Mock
  public NotificationProvider notificationProvider;
  JsonProvider jsonProvider;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  NexusEntityStore store;
  NexusIntegrationTestingFixtures fake;
  Segment segment0;
  Segment segment1;
  Segment segment3;
  Segment segment4;

  @Before
  public void setUp() throws Exception {
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    store = new NexusEntityStoreImpl(entityFactory);
    var segmentManager = new SegmentManagerImpl(entityFactory, store);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    jsonProvider = new JsonProviderImpl();
    fabricatorFactory = new FabricatorFactoryImpl(
      segmentManager,
      jsonapiPayloadFactory,
      jsonProvider
    );
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Stream.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB2().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    Chain chain1 = store.put(NexusIntegrationTestingFixtures.buildChain(fake.account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));
    segment0 = constructSegmentAndChoices(chain1, SegmentType.CONTINUE, 10, 4, fake.program4, fake.program4_sequence1_binding0, fake.program15, fake.program15_sequence1_binding0);
    segment1 = constructSegmentAndChoices(chain1, SegmentType.NEXTMAIN, 11, 0, fake.program4, fake.program4_sequence1_binding0, fake.program5, fake.program5_sequence0_binding0);
    constructSegmentAndChoices(chain1, SegmentType.CONTINUE, 12, 1, fake.program4, fake.program4_sequence1_binding0, fake.program5, fake.program5_sequence1_binding0);
    segment3 = constructSegmentAndChoices(chain1, SegmentType.CONTINUE, 13, 2, fake.program4, fake.program4_sequence1_binding0, fake.program5, fake.program5_sequence1_binding0);
    segment4 = constructSegmentAndChoices(chain1, SegmentType.NEXTMACRO, 14, 0, fake.program3, fake.program3_sequence0_binding0, fake.program15, fake.program15_sequence0_binding0);
  }

  Segment constructSegmentAndChoices(Chain chain, SegmentType type, int offset, int delta, Program macro, ProgramSequenceBinding macroSB, Program main, ProgramSequenceBinding mainSB) throws NexusException {
    var start = Instant.parse("2017-02-14T12:01:00.000001Z").plusSeconds((long) (SEQUENCE_TOTAL_BEATS * 0.5 * offset));
    var end = start.plusSeconds((long) (SEQUENCE_TOTAL_BEATS * 0.5));
    var segment = store.put(buildSegment(
      chain,
      type,
      offset,
      delta,
      SegmentState.CRAFTED,
      "D major",
      SEQUENCE_TOTAL_BEATS,
      0.73,
      120.0,
      String.format("chains-%s-segments-%s", Chains.getIdentifier(chain), offset),
      true));
    store.put(buildSegmentChoice(
      segment,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      macro,
      macroSB));
    store.put(buildSegmentChoice(
      segment,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      main,
      mainSB));

    return segment;
  }

  @Test
  public void getPreviousChoiceOfType() throws NexusException, FabricationFatalException {
    var subject = fabricatorFactory.loadRetrospective(segment3, sourceMaterial);

    var result = subject.getPreviousChoiceOfType(ProgramType.Main);

    assertTrue(result.isPresent());
    assertEquals(result.get().getProgramId(), fake.program5.getId());
  }

  @Test
  public void getPreviousChoiceOfType_forNextMacroSegment() throws NexusException, FabricationFatalException {
    var subject = fabricatorFactory.loadRetrospective(segment4, sourceMaterial);

    var result = subject.getPreviousChoiceOfType(ProgramType.Main);

    assertTrue(result.isPresent());
    assertEquals(result.get().getProgramId(), fake.program5.getId());
  }

  @Test
  public void getPreviousChoiceOfType_forNextMainSegment() throws NexusException, FabricationFatalException {
    var subject = fabricatorFactory.loadRetrospective(segment1, sourceMaterial);

    var result = subject.getPreviousChoiceOfType(ProgramType.Main);

    assertTrue(result.isPresent());
    assertEquals(result.get().getProgramId(), fake.program15.getId());
  }

  /**
   * Failure requiring a chain restart https://www.pivotaltracker.com/story/show/182131722
   */
  @Test
  public void failureToReadMainChoiceIsFatal() throws NexusException {
    for (SegmentChoice c : store.getAll(segment0.getId(), SegmentChoice.class))
      if (c.getProgramType().equals(ProgramType.Main))
        store.delete(segment0.getId(), SegmentChoice.class, c.getId());

    var e = assertThrows(FabricationFatalException.class, () -> fabricatorFactory.loadRetrospective(segment1, sourceMaterial));

    assertEquals("Retrospective sees no main choice!", e.getMessage());
  }

  /**
   * Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787
   */
  @Test
  public void getPreviousMeta() throws NexusException, FabricationFatalException, JsonProcessingException {
    var bun = new StickyBun(patternId, 1);
    var json = jsonProvider.getMapper().writeValueAsString(bun);
    store.put(buildSegmentMeta(segment3, "StickyBun_0f650ae7-42b7-4023-816d-168759f37d2e", json));
    var subject = fabricatorFactory.loadRetrospective(segment4, sourceMaterial);

    var result = subject.getPreviousMeta("StickyBun_0f650ae7-42b7-4023-816d-168759f37d2e");

    assertTrue(result.isPresent());
    assertEquals(json, result.get().getValue());
  }
}
