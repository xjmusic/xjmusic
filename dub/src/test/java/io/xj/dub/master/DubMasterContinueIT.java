// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.program.ProgramType;
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
    segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    dubFactory = injector.getInstance(DubFactory.class);

    // Fixtures
    reset();
    insertFixtureB1();
    insertFixtureB_Instruments();

    // Chain "Test Print #1" has 5 total segments
    insert(newChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, now(), newChainBinding(library2)));
    insert(newSegment(1, 1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-97898asdf7892"));
    insert(newSegment(2, 1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-97898asdf7892"));

    // Chain "Test Print #1" has this segment that was just dubbed
    segment3 = newSegment(3, 1, 2, SegmentState.Dubbed, Instant.parse("2017-02-14T12:02:04.000001Z"), Instant.parse("2017-02-14T12:02:36.000001Z"), "F Major", 64, 0.30, 120.0, "chains-1-segments-9f7s89d8a7892.wav");
    segment3.add(newChoice(ProgramType.Macro, 4, program4_binding1.getId(), 3));
    segment3.add(newChoice(ProgramType.Main, 5, program5_binding0.getId(), 5));
    insert(segment3);

    // Chain "Test Print #1" has this segment dubbing - Structure is complete
    segment4 = newSegment(4, 1, 3, SegmentState.Dubbing, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:03:15.836735Z"), "D Major", 16, 0.45, 120.0, "chains-1-segments-9f7s89d8a7892.wav");
    segment4.add(newChoice(ProgramType.Macro, 4, program4_binding1.getId(), 3));
    segment4.add(newChoice(ProgramType.Main, 5, program5_binding1.getId(), -5));
    Choice choice1 = segment4.add(newChoice(ProgramType.Rhythm, 35, null, 3));
    segment4.add(newSegmentMeme("Cozy"));
    segment4.add(newSegmentMeme("Classic"));
    segment4.add(newSegmentMeme("Outlook"));
    segment4.add(newSegmentMeme("Rosy"));
    segment4.add(newSegmentChord(0.0, "A minor"));
    segment4.add(newSegmentChord(8.0, "D major"));
    segment4.add(newArrangement(choice1, voiceDrums, instrument201));
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

    Fabricator fabricator = fabricatorFactory.fabricate(segment4);
    dubFactory.master(fabricator).doWork();

    // future test: success of dub master continue test
  }

}

