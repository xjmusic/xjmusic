// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoiceArrangement;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.SegmentMeme;
import io.xj.core.model.SegmentState;
import io.xj.craft.CraftModule;
import io.xj.dub.DubFactory;
import io.xj.dub.DubModule;
import io.xj.mixer.util.InternalResource;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.InputStream;
import java.time.Instant;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DubMasterContinueIT extends FixtureIT {
  private static final String testResourceFilePath = "test_audio" + File.separator + "F32LSB_48kHz_Stereo.wav";
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  AmazonProvider amazonProvider;
  private DubFactory dubFactory;
  private FabricatorFactory fabricatorFactory;

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(Modules.override(new CoreModule(), new CraftModule(), new DubModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      }));
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    dubFactory = injector.getInstance(DubFactory.class);

    // Fixtures
    reset();
    insertFixtureB1();
    insertFixtureB_Instruments();

    // Chain "Test Print #1" has 5 total segments
    chain1 = insert(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    insert(ChainBinding.create(chain1, library2));
    segment1 = insert(Segment.create(chain1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-97898asdf7892"));
    segment2 = insert(Segment.create(chain1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-97898asdf7892"));

    // Chain "Test Print #1" has this segment that was just dubbed
    segment3 = insert(Segment.create(chain1, 2, SegmentState.Dubbed, Instant.parse("2017-02-14T12:02:04.000001Z"), Instant.parse("2017-02-14T12:02:36.000001Z"), "F Major", 64, 0.30, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create(segment3, ProgramType.Macro, program4_binding1, 3));
    insert(SegmentChoice.create(segment3, ProgramType.Main, program5_binding0, 5));

    // Chain "Test Print #1" has this segment dubbing - Structure is complete
    segment4 = insert(Segment.create(chain1, 3, SegmentState.Dubbing, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:03:15.836735Z"), "D Major", 16, 0.45, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create(segment4, ProgramType.Macro, program4_binding1, 3));
    insert(SegmentChoice.create(segment4, ProgramType.Main, program5_binding1, -5));
    SegmentChoice choice1 = insert(SegmentChoice.create(segment4, ProgramType.Rhythm, program35, 3));
    insert(SegmentMeme.create(segment4, "Cozy"));
    insert(SegmentMeme.create(segment4, "Classic"));
    insert(SegmentMeme.create(segment4, "Outlook"));
    insert(SegmentMeme.create(segment4, "Rosy"));
    insert(SegmentChord.create(segment4, 0.0, "A minor"));
    insert(SegmentChord.create(segment4, 8.0, "D major"));
    insert(SegmentChoiceArrangement.create(choice1, voiceDrums, instrument201));
    insert(segment4);

    // FUTURE: determine new test vector for [#154014731] persist Audio pick in memory

    // System properties
    System.setProperty("audio.file.bucket", "my-test-bucket");
  }


  @After
  public void tearDown() {
    System.clearProperty("audio.file.bucket");
  }

  @Test
  public void dubMasterContinue() throws Exception {
    InternalResource testAudioResource = new InternalResource(testResourceFilePath);
    // it's necessary to have two separate streams for this mock of two separate file reads
    InputStream audioStreamOne = FileUtils.openInputStream(testAudioResource.getFile());
    InputStream audioStreamTwo = FileUtils.openInputStream(testAudioResource.getFile());
    when(amazonProvider.streamS3Object("my-test-bucket",
      "19801735098q47895897895782138975898")).thenReturn(audioStreamOne);
    when(amazonProvider.streamS3Object("my-test-bucket",
      "a1g9f8u0k1v7f3e59o7j5e8s98")).thenReturn(audioStreamTwo);

    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segment4);
    dubFactory.master(fabricator).doWork();

    // future test: success of dub master continue test
  }

}

