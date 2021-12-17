// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import io.xj.nexus.hub_client.client.HubClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CraftSegmentPatternMemeTest {
  private static final int TEST_REPEAT_ITERATIONS = 14;

  @Mock
  public HubClient hubClient;

  /**
   Test to ensure that the following Macro-Program is based on its first sequence-binding meme
   matching the last sequence-binding meme of the preceding Macro-Program
   <p>
   [#165803886] Segment memes expected to be taken directly of sequence_pattern binding
   [#176728582] Macro program sequence should advance after each main program
   */
  @Test
  public void craftSegment() throws Exception {
/*
  // FUTURE bring back this test
    for (int i = 0; i < TEST_REPEAT_ITERATIONS; i++) {
      Environment env = Environment.getDefault();
      Injector injector = Guice.createInjector(Modules.override(new NexusWorkModule())
        .with(new AbstractModule() {
          @Override
          public void configure() {
            bind(Environment.class).toInstance(env);
            bind(HubClient.class).toInstance(hubClient);
          }
        }));
      CraftFactory craftFactory = injector.getInstance(CraftFactory.class);
      FabricatorFactory fabricatorFactory = injector.getInstance(FabricatorFactory.class);
      var entityFactory = injector.getInstance(EntityFactory.class);
      HubTopology.buildHubApiTopology(entityFactory);
      NexusTopology.buildNexusApiTopology(entityFactory);

      // Manipulate the underlying entity store; reset before each test
      NexusEntityStore store = injector.getInstance(NexusEntityStore.class);
      store.deleteAll();

      // Mock request via HubClient returns fake generated library of hub content
      NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
      HubContent sourceMaterial = new HubContent(Streams.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()
      ).collect(Collectors.toList()));

      // Chain "Test Print #1" has 5 total segments
      Chain chain = store.put(buildChain(fake.account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));

      // Preceding Segment
      Segment previousSegment = store.put(buildSegment(
        chain,
        1,
        SegmentState.CRAFTING,
        Instant.parse("2017-02-14T12:02:04.000001Z"),
        Instant.parse("2017-02-14T12:02:36.000001Z"),
        "F Major",
        64,
        0.30,
        120.0,
        "chains-1-segments-9f7s89d8a7892.wav",
        "ogg"));
      store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(previousSegment, ProgramType.Macro, fake.program4_sequence1_binding0));
      store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(previousSegment, ProgramType.Main, fake.program5_sequence1_binding0));

      // Following Segment
      Segment segment = store.put(buildSegment(chain, 2, SegmentState.PLANNED, Instant.parse(previousSegment.getEndAt()), null, "C", 8, 0.8, 120, "chain-1-waveform-12345", "wav"));

      craftFactory.macroMain(fabricatorFactory.fabricate(sourceMaterial, segment)).doWork();

      var result = store.getSegment(segment.getId()).orElseThrow();
      assertEquals(SegmentType.NEXTMACRO, result.getType());
      assertSameItems(Lists.newArrayList("REGRET", "HINDSIGHT", "CHUNKY", "TANGY"),
        Entities.namesOf(store.getAll(result.getId(), SegmentMeme.class)));
    }
*/
  }
}
