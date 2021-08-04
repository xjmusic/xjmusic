// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub.master;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.mixer.InternalResource;
import io.xj.lib.mixer.Mixer;
import io.xj.lib.mixer.MixerFactory;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.dao.Segments;
import io.xj.nexus.dub.DubFactory;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.testing.NexusTestConfiguration;
import io.xj.nexus.work.NexusWorkModule;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DubDubMasterContinueTest {
  private static final String testResourceFilePath = "test_audio" + File.separator + "F32LSB_48kHz_Stereo.wav";
  @Mock
  public FileStoreProvider fileStoreProvider;
  @Mock
  public HubClient hubClient;
  @Mock
  public Mixer mixer;
  @Mock
  public MixerFactory mixerFactory;
  private DubFactory dubFactory;
  private FabricatorFactory fabricatorFactory;
  private HubContent sourceMaterial;
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Environment env = Environment.getDefault();
    var injector = AppConfiguration.inject(config, env,
      ImmutableSet.of(Modules.override(new NexusWorkModule())
        .with(new AbstractModule() {
          @Override
          public void configure() {
            bind(FileStoreProvider.class).toInstance(fileStoreProvider);
            bind(HubClient.class).toInstance(hubClient);
            bind(MixerFactory.class).toInstance(mixerFactory);
          }
        })));
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    dubFactory = injector.getInstance(DubFactory.class);
    when(mixerFactory.createMixer(any())).thenReturn(mixer);
    var entityFactory = injector.getInstance(EntityFactory.class);
    Topology.buildHubApiTopology(entityFactory);
    Topology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    NexusEntityStore store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB3().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    Chain chain1 = store.put(NexusIntegrationTestingFixtures.makeChain(fake.account1, "Test Print #1", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setTargetId(fake.library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(0)
      .setState(Segment.State.Dubbed)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z")
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120)
      .setStorageKey("chains-1-segments-97898a2sdf7892")
      .build());
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(1)
      .setState(Segment.State.Dubbing)
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setEndAt("2017-02-14T12:02:04.000001Z")
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120)
      .setStorageKey("chains-1-segments-97898a2sdf7892")
      .build());

    // Chain "Test Print #1" has this segment that was just dubbed
    Segment segment3 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(2)
      .setState(Segment.State.Dubbed)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());
    store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment3.getId())
      .setDeltaIn(Segments.DELTA_UNLIMITED)
      .setDeltaOut(Segments.DELTA_UNLIMITED)
      .setProgramType(Program.Type.Macro)
      .setProgramId(fake.program4_sequence1_binding0.getProgramId())
      .setProgramSequenceBindingId(fake.program4_sequence1_binding0.getId())
      .build());
    store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment3.getId())
      .setDeltaIn(Segments.DELTA_UNLIMITED)
      .setDeltaOut(Segments.DELTA_UNLIMITED)
      .setProgramType(Program.Type.Main)
      .setProgramId(fake.program5_sequence0_binding0.getProgramId())
      .setProgramSequenceBindingId(fake.program5_sequence0_binding0.getId())
      .build());

    // Chain "Test Print #1" has this segment dubbing - Structure is complete
    segment4 = store.put(NexusIntegrationTestingFixtures.makeSegment(chain1, 3, Segment.State.Dubbing, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:03:15.836735Z"), "D Major", 16, 0.45, 120.0, "chains-1-segments-9f7s89d8a7892", "wav"));
    store.put(NexusIntegrationTestingFixtures.makeSegmentChoice(segment4, Program.Type.Macro, fake.program4_sequence1_binding0));
    store.put(NexusIntegrationTestingFixtures.makeSegmentChoice(segment4, Program.Type.Main, fake.program5_sequence1_binding0));
    SegmentChoice choice1 = store.put(NexusIntegrationTestingFixtures.makeSegmentChoice(segment4, fake.program35, fake.program35_sequence0, fake.program35_voice0, fake.instrument8));
    store.put(NexusIntegrationTestingFixtures.makeMeme(segment4, "Cozy"));
    store.put(NexusIntegrationTestingFixtures.makeMeme(segment4, "Classic"));
    store.put(NexusIntegrationTestingFixtures.makeMeme(segment4, "Outlook"));
    store.put(NexusIntegrationTestingFixtures.makeMeme(segment4, "Rosy"));
    store.put(NexusIntegrationTestingFixtures.makeChord(segment4, 0.0, "A minor"));
    store.put(NexusIntegrationTestingFixtures.makeChord(segment4, 8.0, "D major"));
    store.put(NexusIntegrationTestingFixtures.makeArrangement(choice1));

    // FUTURE: determine new test vector for [#154014731] persist Audio pick in memory
  }

  @After
  public void tearDown() {

  }

  @Test
  public void dubMasterContinue() throws Exception {
    InternalResource testAudioResource = new InternalResource(testResourceFilePath);
    // it's necessary to have two separate streams for this mock of two separate file reads
    FileInputStream audioStreamOne = FileUtils.openInputStream(testAudioResource.getFile());
    FileInputStream audioStreamTwo = FileUtils.openInputStream(testAudioResource.getFile());
    when(fileStoreProvider.streamS3Object("my-test-bucket",
      "19801735098q47895897895782138975898")).thenReturn(audioStreamOne);
    when(fileStoreProvider.streamS3Object("my-test-bucket",
      "a1g9f8u0k1v7f3e59o7j5e8s98")).thenReturn(audioStreamTwo);

    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment4);
    dubFactory.master(fabricator).doWork();

    // future test: success of dub master continue test
  }

}

