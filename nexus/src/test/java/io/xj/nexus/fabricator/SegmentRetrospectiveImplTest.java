// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.fabricator;

import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.api.*;
import io.xj.hub.HubTopology;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.hub.client.HubClient;
import io.xj.hub.client.HubContent;
import io.xj.nexus.persistence.Chains;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.Segments;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.stream.Collectors;

import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SegmentRetrospectiveImplTest {
  static int SEQUENCE_TOTAL_BEATS = 64;

  @Mock
  public HubClient hubClient;
  private FabricatorFactory fabricatorFactory;
  private HubContent sourceMaterial;
  private NexusEntityStore store;
  private NexusIntegrationTestingFixtures fake;
  private Segment segment0;
  private Segment segment1;
  private Segment segment3;
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
    Environment env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(new NexusWorkModule())
      .with(new AbstractModule() {
        @Override
        public void configure() {
          bind(Environment.class).toInstance(env);
          bind(HubClient.class).toInstance(hubClient);
        }
      }));
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB2().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    Chain chain1 = store.put(NexusIntegrationTestingFixtures.buildChain(fake.account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    segment0 = constructSegmentAndChoices(chain1, SegmentType.CONTINUE, 10, 4, fake.program4, fake.program4_sequence1_binding0, fake.program15, fake.program15_sequence1_binding0);
    segment1 = constructSegmentAndChoices(chain1, SegmentType.NEXTMAIN, 11, 0, fake.program4, fake.program4_sequence1_binding0, fake.program5, fake.program5_sequence0_binding0);
    constructSegmentAndChoices(chain1, SegmentType.CONTINUE, 12, 1, fake.program4, fake.program4_sequence1_binding0, fake.program5, fake.program5_sequence1_binding0);
    segment3 = constructSegmentAndChoices(chain1, SegmentType.CONTINUE, 13, 2, fake.program4, fake.program4_sequence1_binding0, fake.program5, fake.program5_sequence1_binding0);
    segment4 = constructSegmentAndChoices(chain1, SegmentType.NEXTMACRO, 14, 0, fake.program3, fake.program3_sequence0_binding0, fake.program15, fake.program15_sequence0_binding0);
  }

  private Segment constructSegmentAndChoices(Chain chain, SegmentType type, int offset, int delta, Program macro, ProgramSequenceBinding macroSB, Program main, ProgramSequenceBinding mainSB) throws NexusException {
    var start = Instant.parse("2017-02-14T12:01:00.000001Z").plusSeconds((long) (SEQUENCE_TOTAL_BEATS * 0.5 * offset));
    var end = start.plusSeconds((long) (SEQUENCE_TOTAL_BEATS * 0.5));
    var segment = store.put(buildSegment(
      chain,
      type,
      offset,
      delta,
      SegmentState.DUBBED,
      start,
      end,
      "D major",
      SEQUENCE_TOTAL_BEATS,
      0.73,
      120.0,
      String.format("chains-%s-segments-%s", Chains.getIdentifier(chain), offset),
      "wav"));
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
   Failure requiring a chain restart https://www.pivotaltracker.com/story/show/182131722
   */
  @Test
  public void failureToReadMainChoiceIsFatal() throws NexusException {
    for (SegmentChoice c : store.getAll(segment0.getId(), SegmentChoice.class))
      if (c.getProgramType().equals(ProgramType.Main.toString()))
        store.delete(segment0.getId(), SegmentChoice.class, c.getId());

    var e = assertThrows(FabricationFatalException.class, () -> fabricatorFactory.loadRetrospective(segment1, sourceMaterial));

    assertEquals("Retrospective sees no main choice!", e.getMessage());
  }


}
