// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.craft.basis.impl;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import io.xj.core.CoreModule;
import io.xj.core.dao.ArrangementDAO;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.dao.ChainConfigDAO;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.dao.SegmentChordDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.dao.SegmentMemeDAO;
import io.xj.core.dao.SegmentMessageDAO;
import io.xj.core.dao.SequenceDAO;
import io.xj.core.dao.SequenceMemeDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternMemeDAO;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.dao.PatternEventDAO;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.pick.Pick;
import io.xj.craft.CraftModule;
import io.xj.craft.basis.Basis;
import io.xj.craft.basis.BasisFactory;
import io.xj.music.Tuning;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BasisImplTest {
  private Injector injector;
  private Basis subject;
  private BasisFactory basisFactory;
  @Mock private ArrangementDAO arrangementDAO;
  @Mock private AudioDAO audioDAO;
  @Mock private AudioEventDAO audioEventDAO;
  @Mock private ChainConfigDAO chainConfigDAO;
  @Mock private ChoiceDAO choiceDAO;
  @Mock private SequenceDAO sequenceDAO;
  @Mock private SequenceMemeDAO sequenceMemeDAO;
  @Mock private InstrumentMemeDAO instrumentMemeDAO;
  @Mock private SegmentChordDAO segmentChordDAO;
  @Mock private SegmentDAO segmentDAO;
  @Mock private SegmentMemeDAO segmentMemeDAO;
  @Mock private SegmentMessageDAO segmentMessageDAO;
  @Mock private PatternDAO patternDAO;
  @Mock private PatternMemeDAO patternMemeDAO;
  @Mock private Tuning tuning;
  @Mock private VoiceDAO voiceDAO;
  @Mock private PatternEventDAO patternEventDAO;
  @Mock private Choice choice;

  @Before
  public void setUp() throws Exception {
    createInjector();
    basisFactory = injector.getInstance(BasisFactory.class);
  }

  @After
  public void tearDown() throws Exception {
    subject = null;
    injector = null;
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule(), new CraftModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ArrangementDAO.class).toInstance(arrangementDAO);
          bind(AudioDAO.class).toInstance(audioDAO);
          bind(AudioEventDAO.class).toInstance(audioEventDAO);
          bind(ChainConfigDAO.class).toInstance(chainConfigDAO);
          bind(ChoiceDAO.class).toInstance(choiceDAO);
          bind(SequenceDAO.class).toInstance(sequenceDAO);
          bind(SequenceMemeDAO.class).toInstance(sequenceMemeDAO);
          bind(InstrumentMemeDAO.class).toInstance(instrumentMemeDAO);
          bind(SegmentChordDAO.class).toInstance(segmentChordDAO);
          bind(SegmentDAO.class).toInstance(segmentDAO);
          bind(SegmentMemeDAO.class).toInstance(segmentMemeDAO);
          bind(SegmentMessageDAO.class).toInstance(segmentMessageDAO);
          bind(PatternDAO.class).toInstance(patternDAO);
          bind(PatternMemeDAO.class).toInstance(patternMemeDAO);
          bind(Tuning.class).toInstance(tuning);
          bind(VoiceDAO.class).toInstance(voiceDAO);
          bind(PatternEventDAO.class).toInstance(patternEventDAO);
        }
      }));
  }

  @Test
  public void outputFilePath() throws Exception {
  }

  @Test
  public void outputAudioFormat() throws Exception {
  }

  @Test
  public void type() throws Exception {
  }

  @Test
  public void segment() throws Exception {
  }

  @Test
  public void isInitialSegment() throws Exception {
  }

  @Test
  public void segmentId() throws Exception {
  }

  @Test
  public void chainId() throws Exception {
  }

  @Test
  public void chainConfig() throws Exception {
  }

  @Test
  public void segmentBeginAt() throws Exception {
  }

  @Test
  public void previousSegment() throws Exception {
  }

  @Test
  public void previousMacroChoice() throws Exception {
  }

  @Test
  public void previousMainChoice() throws Exception {
  }

  @Test
  public void previousRhythmChoice() throws Exception {
  }

  @Test
  public void previousPercussiveArrangements() throws Exception {
  }

  @Test
  public void currentMacroChoice() throws Exception {
  }

  @Test
  public void currentMainChoice() throws Exception {
  }

  @Test
  public void currentRhythmChoice() throws Exception {
  }

  @Test
  public void previousMacroPattern() throws Exception {
  }

  @Test
  public void previousMacroNextPattern() throws Exception {
  }

  @Test
  public void sequence() throws Exception {
  }

  @Test
  public void chainConfigs() throws Exception {
  }

  @Test
  public void choiceArrangements() throws Exception {
  }

  @Test
  public void chordAt() throws Exception {
  }

  @Test
  public void pitch() throws Exception {
  }

  @Test
  public void note() throws Exception {
  }

  @Test
  public void secondsAtPosition_zero() throws Exception {
    when(segmentDAO.readOneAtChainOffset(any(), eq(BigInteger.valueOf(977)), eq(BigInteger.valueOf(1)))).thenReturn(new Segment(4212)
      .setOffset(BigInteger.valueOf(1))
      .setDensity(0.6)
      .setKey("F major")
      .setState("Crafted")
      .setTempo(60.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:08.000000")
      .setEndAt("2017-12-12 01:00:16.000000"));
    subject = basisFactory.createBasis(new Segment(4213)
      .setOffset(BigInteger.valueOf(2))
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(120.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:16.000000")
      .setEndAt("2017-12-12 01:00:22.000000"));

    assertEquals(Double.valueOf(0), subject.secondsAtPosition(0));
  }

  @Test
  public void secondsAtPosition_tempoChangeMiddle() throws Exception {
    when(segmentDAO.readOneAtChainOffset(any(), eq(BigInteger.valueOf(977)), eq(BigInteger.valueOf(1)))).thenReturn(new Segment(4212)
      .setOffset(BigInteger.valueOf(1))
      .setDensity(0.6)
      .setKey("F major")
      .setState("Crafted")
      .setTempo(60.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:08.000000")
      .setEndAt("2017-12-12 01:00:16.000000"));
    subject = basisFactory.createBasis(new Segment(4213)
      .setOffset(BigInteger.valueOf(2))
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(120.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:16.000000")
      .setEndAt("2017-12-12 01:00:22.000000"));

    assertEquals(Double.valueOf(3.53125), subject.secondsAtPosition(4));
  }

  @Test
  public void secondsAtPosition_tempoChangeEnd() throws Exception {
    when(segmentDAO.readOneAtChainOffset(any(), eq(BigInteger.valueOf(977)), eq(BigInteger.valueOf(1)))).thenReturn(new Segment(4212)
      .setOffset(BigInteger.valueOf(1))
      .setDensity(0.6)
      .setKey("F major")
      .setState("Crafted")
      .setTempo(60.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:08.000000")
      .setEndAt("2017-12-12 01:00:16.000000"));
    subject = basisFactory.createBasis(new Segment(4213)
      .setOffset(BigInteger.valueOf(2))
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(120.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:16.000000")
      .setEndAt("2017-12-12 01:00:22.000000"));

    assertEquals(Double.valueOf(6.0625), subject.secondsAtPosition(8));
  }

  @Test
  public void sequenceMemes() throws Exception {
  }

  @Test
  public void patternEvents() throws Exception {
  }

  @Test
  public void instrumentAudioEvents() throws Exception {
  }

  @Test
  public void instrumentAudios() throws Exception {
  }

  @Test
  public void segmentAudio() throws Exception {
  }

  @Test
  public void segmentAudios() throws Exception {
  }

  @Test
  public void segmentAudioIds() throws Exception {
  }

  @Test
  public void segmentChords() throws Exception {
  }

  @Test
  public void segmentMemes() throws Exception {
  }

  @Test
  public void pick_returned_by_picks() throws Exception {
    subject = basisFactory.createBasis(new Segment());
    subject.pick(new Pick()
      .setArrangementId(BigInteger.valueOf(1234))
      .setAudioId(BigInteger.valueOf(78874))
      .setStart(0.273)
      .setLength(1.571)
      .setAmplitude(0.8)
      .setPitch(432.0));

    Collection<Pick> result = subject.picks();
    Pick resultPick = result.iterator().next();
      assertEquals(BigInteger.valueOf(1234), resultPick.getArrangementId());
      assertEquals(BigInteger.valueOf(78874), resultPick.getAudioId());
      assertEquals(0.273, resultPick.getStart(),0.001);
      assertEquals(1.571, resultPick.getLength(),0.001);
      assertEquals(0.8, resultPick.getAmplitude(),0.1);
      assertEquals(432.0, resultPick.getPitch(),0.1);
  }

  @Test
  public void segmentTotalLength() throws Exception {
  }

  @Test
  public void segmentMeme() throws Exception {
  }

  @Test
  public void patternMemes() throws Exception {
  }

  @Test
  public void patternByOffset() throws Exception {
  }

  @Test
  public void segmentByOffset() throws Exception {
  }

  @Test
  public void segmentChoiceByType() throws Exception {
    when(choiceDAO.readOneSegmentTypeWithAvailablePatternOffsets(any(), eq(BigInteger.valueOf(123)), eq(SequenceType.Rhythm))).thenReturn(choice);
    subject = basisFactory.createBasis(new Segment(4213)
      .setOffset(BigInteger.valueOf(2))
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(120.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:16.000000")
      .setEndAt("2017-12-12 01:00:22.000000"));

    Choice result = subject.segmentChoiceByType(BigInteger.valueOf(123), SequenceType.Rhythm);
    assertNotNull(result);
    assertEquals(choice, result);
  }

  @Test
  public void segmentChoiceByType_nullPassesThrough() throws Exception {
    when(choiceDAO.readOneSegmentTypeWithAvailablePatternOffsets(any(), eq(BigInteger.valueOf(123)), eq(SequenceType.Rhythm))).thenReturn(null);
    subject = basisFactory.createBasis(new Segment(4213)
      .setOffset(BigInteger.valueOf(2))
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(120.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:16.000000")
      .setEndAt("2017-12-12 01:00:22.000000"));

    Choice result = subject.segmentChoiceByType(BigInteger.valueOf(123), SequenceType.Rhythm);
    assertNull(result);
  }

  @Test
  public void voices() throws Exception {
  }

  @Test
  public void updateSegment() throws Exception {
  }

  @Test
  public void report() throws Exception {
  }

  @Test
  public void sendReport() throws Exception {
  }

  @Test
  public void atMicros() throws Exception {
  }

}
