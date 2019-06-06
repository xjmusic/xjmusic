// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.exception.CoreException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.sequence.SequenceType;
import io.xj.craft.CraftModule;
import io.xj.dub.BaseIT;
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
import java.math.BigInteger;
import java.time.Instant;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DubMasterContinueIT extends BaseIT {
  private static final String testResourceFilePath = "test_audio" + File.separator + "F32LSB_48kHz_Stereo.wav";
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  AmazonProvider amazonProvider;
  private DubFactory dubFactory;
  private FabricatorFactory fabricatorFactory;

  // Test subject
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(Modules.override(new CoreModule(), new CraftModule(), new DubModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      }));
    SegmentFactory segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    dubFactory = injector.getInstance(DubFactory.class);

    // Fixtures
    IntegrationTestEntity.reset();
    insertLibraryA();

    // Chain "Test Print #1" has 5 total segments
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);
    IntegrationTestEntity.insertSegment_NoContent(1, 1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chain-1-segment-97898asdf7892");
    IntegrationTestEntity.insertSegment_NoContent(2, 1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chain-1-segment-97898asdf7892");

    // Chain "Test Print #1" has this segment that was just dubbed
    Segment seg3 = segmentFactory.newSegment(BigInteger.valueOf(3))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(2))
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chain-1-segment-9f7s89d8a7892.wav");
    seg3.add(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setSequencePatternId(BigInteger.valueOf(441))
      .setTypeEnum(SequenceType.Macro)
      .setTranspose(3));
    seg3.add(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setSequencePatternId(BigInteger.valueOf(1550))
      .setTypeEnum(SequenceType.Main)
      .setTranspose(5));
    IntegrationTestEntity.insert(seg3);

    // Chain "Test Print #1" is dubbing - Structure is complete
    segment4 = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(3))
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("D Major")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(120.0)
      .setWaveformKey("chain-1-segment-9f7s89d8a7892.wav");
    segment4.add(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(441))
      .setTypeEnum(SequenceType.Macro)
      .setTranspose(3));
    segment4.add(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(1651))
      .setTypeEnum(SequenceType.Main)
      .setTranspose(-5));
    Choice choice1 = segment4.add(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequenceId(BigInteger.valueOf(35))
      .setTypeEnum(SequenceType.Rhythm)
      .setTranspose(3));
    ImmutableList.of("Cozy", "Classic", "Outlook", "Rosy").forEach(memeName -> {
      try {
        segment4.add(new SegmentMeme()
          .setSegmentId(BigInteger.valueOf(4))
          .setName(memeName));
      } catch (CoreException ignored) {
      }
    });
    segment4.add(new SegmentChord()
      .setSegmentId(BigInteger.valueOf(4))
      .setPosition(0.0)
      .setName("A minor"));
    segment4.add(new SegmentChord()
      .setSegmentId(BigInteger.valueOf(4))
      .setPosition(8.0)
      .setName("D major"));
    segment4.add(new Arrangement()
      .setChoiceUuid(choice1.getUuid())
      .setVoiceId(BigInteger.valueOf(1))
      .setInstrumentId(BigInteger.valueOf(1)));
    IntegrationTestEntity.insert(segment4);

    // Instrument "808"
    IntegrationTestEntity.insertInstrument(1, 2, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrumentMeme(1, "heavy");

    // Audio "Kick"
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Kick", "19801735098q47895897895782138975898", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioEvent(1, 2.5, 1, "KICK", "Eb", 0.8, 1.0);

    // Audio "Snare"
    IntegrationTestEntity.insertAudio(2, 1, "Published", "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(2, 3, 1, "SNARE", "Ab", 0.1, 0.8);

    // FUTURE: determine new test vector for [#154014731] persist Audio pick in memory

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 2);

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

