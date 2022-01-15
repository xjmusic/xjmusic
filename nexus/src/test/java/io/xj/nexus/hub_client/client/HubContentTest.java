// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.hub_client.client;

import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.api.*;
import io.xj.hub.HubTopology;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.persistence.Chains;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.stream.Collectors;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class HubContentTest {
  static int SEQUENCE_TOTAL_BEATS = 64;

  private FabricatorFactory fabricatorFactory;
  private HubContent subject;
  private NexusEntityStore store;
  private NexusIntegrationTestingFixtures fake;
  private Segment segment1;
  private Segment segment3;
  private Segment segment4;
  private Chain chain;
  private Segment segment;

  @Before
  public void setUp() throws Exception {
    Environment env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(new NexusWorkModule())
      .with(new AbstractModule() {
        @Override
        public void configure() {
          bind(Environment.class).toInstance(env);
        }
      }));
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Test subject
    fake = new NexusIntegrationTestingFixtures();
    subject = new HubContent(Streams.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB2().stream()
    ).collect(Collectors.toList()));

    // Fake fabrication
    var account1 = buildAccount("fish");
    Template template1 = buildTemplate(account1, "Test Template 1", "test1");
    chain = store.put(NexusIntegrationTestingFixtures.buildChain(template1));
    segment = store.put(buildSegment(
      chain,
      SegmentType.CONTINUE,
      17,
      4,
      SegmentState.DUBBED,
      Instant.parse("2017-02-14T12:01:00.000001Z"),
      Instant.parse("2017-02-14T12:01:32.000001Z"),
      "D major",
      SEQUENCE_TOTAL_BEATS,
      0.73,
      120.0,
      String.format("chains-%s-segments-%s", Chains.getIdentifier(chain), 17),
      "wav"));
    ;
  }

  @Test
  public void getProgramSequence_fromSequence() throws NexusException {
    SegmentChoice choice = store.put(buildSegmentChoice(segment, ProgramType.Main, fake.program5_sequence0));

    var result = subject.getProgramSequence(choice);

    assertEquals(fake.program5_sequence0.getId(), result.orElseThrow().getId());
  }

  @Test
  public void getProgramSequence_fromSequenceBinding() throws NexusException {
    SegmentChoice choice = store.put(buildSegmentChoice(segment, ProgramType.Main, fake.program5_sequence0_binding0));

    var result = subject.getProgramSequence(choice);

    assertEquals(fake.program5_sequence0.getId(), result.orElseThrow().getId());
  }
}
