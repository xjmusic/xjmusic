//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.fabricator;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.dao.ChainConfigDAO;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternEventDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.dao.SequenceDAO;
import io.xj.core.dao.SequenceMemeDAO;
import io.xj.core.dao.SequencePatternMemeDAO;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.sequence.SequenceType;
import io.xj.music.Tuning;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FabricatorImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Fabricator subject;
  private FabricatorFactory fabricatorFactory;
  @Mock
  private AudioDAO audioDAO;
  @Mock
  private AudioEventDAO audioEventDAO;
  @Mock
  private ChainConfigDAO chainConfigDAO;
  @Mock
  private SequenceDAO sequenceDAO;
  @Mock
  private SequenceMemeDAO sequenceMemeDAO;
  @Mock
  private InstrumentMemeDAO instrumentMemeDAO;
  @Mock
  private SegmentDAO segmentDAO;
  @Mock
  private PatternDAO patternDAO;
  @Mock
  private SequencePatternMemeDAO sequencePatternMemeDAO;
  @Mock
  private Tuning tuning;
  @Mock
  private VoiceDAO voiceDAO;
  @Mock
  private PatternEventDAO patternEventDAO;
  private SegmentFactory segmentFactory;

  @Before
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AudioDAO.class).toInstance(audioDAO);
          bind(AudioEventDAO.class).toInstance(audioEventDAO);
          bind(ChainConfigDAO.class).toInstance(chainConfigDAO);
          bind(SequenceDAO.class).toInstance(sequenceDAO);
          bind(SequenceMemeDAO.class).toInstance(sequenceMemeDAO);
          bind(InstrumentMemeDAO.class).toInstance(instrumentMemeDAO);
          bind(SegmentDAO.class).toInstance(segmentDAO);
          bind(PatternDAO.class).toInstance(patternDAO);
          bind(SequencePatternMemeDAO.class).toInstance(sequencePatternMemeDAO);
          bind(Tuning.class).toInstance(tuning);
          bind(VoiceDAO.class).toInstance(voiceDAO);
          bind(PatternEventDAO.class).toInstance(patternEventDAO);
        }
      }));
    segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
  }

  @Test
  public void secondsAtPosition_zero() throws Exception {
    setupSecondsAtPositionTest();

    assertEquals(Double.valueOf(0), subject.computeSecondsAtPosition(0));
  }

  @Test
  public void secondsAtPosition_beforeZero() throws Exception {
    setupSecondsAtPositionTest();

    assertEquals(Double.valueOf(-1), subject.computeSecondsAtPosition(-1));
  }

  @Test
  public void secondsAtPosition_afterEnd() throws Exception {
    setupSecondsAtPositionTest();

    assertEquals(Double.valueOf(8.0625), subject.computeSecondsAtPosition(12));
  }

  @Test
  public void secondsAtPosition_tempoChangeMiddle() throws Exception {
    setupSecondsAtPositionTest();

    assertEquals(Double.valueOf(3.53125), subject.computeSecondsAtPosition(4));
  }

  @Test
  public void secondsAtPosition_tempoChangeEnd() throws Exception {
    setupSecondsAtPositionTest();

    assertEquals(Double.valueOf(6.0625), subject.computeSecondsAtPosition(8));
  }

  @Test
  public void pick_returned_by_picks() throws Exception {
    setupSecondsAtPositionTest();
    Choice choice = subject.add(new Choice()
      .setSequenceId(BigInteger.valueOf(5))
      .setTypeEnum(SequenceType.Rhythm)
      .setTranspose(-5)
      .setSegmentId(BigInteger.valueOf(1)));
    Arrangement arrangement = subject.add(new Arrangement()
      .setInstrumentId(BigInteger.valueOf(1))
      .setChoiceUuid(choice.getUuid())
      .setVoiceId(BigInteger.valueOf(2)));
    subject.add(new Pick()
      .setArrangementUuid(arrangement.getUuid())
      .setVoiceId(BigInteger.valueOf(782L))
      .setAudioId(BigInteger.valueOf(78874))
      .setPatternEventId(BigInteger.valueOf(73))
      .setStart(0.273)
      .setLength(1.571)
      .setAmplitude(0.8)
      .setPitch(432.0));

    Collection<Pick> result = subject.getSegment().getPicks();
    Pick resultPick = result.iterator().next();
    assertEquals(arrangement.getUuid(), resultPick.getArrangementUuid());
    assertEquals(BigInteger.valueOf(78874), resultPick.getAudioId());
    assertEquals(0.273, resultPick.getStart(), 0.001);
    assertEquals(1.571, resultPick.getLength(), 0.001);
    assertEquals(0.8, resultPick.getAmplitude(), 0.1);
    assertEquals(432.0, resultPick.getPitch(), 0.1);
  }

  /**
   secondsAtPosition test have shared fixture

   @throws CoreException on failure to setup
   */
  private void setupSecondsAtPositionTest() throws CoreException {
    when(segmentDAO.readOneAtChainOffset(any(), eq(BigInteger.valueOf(977)), eq(BigInteger.valueOf(1)))).thenReturn(segmentFactory.newSegment(BigInteger.valueOf(4212))
      .setOffset(BigInteger.valueOf(1))
      .setDensity(0.6)
      .setKey("F major")
      .setState("Crafted")
      .setTempo(60.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:08.000000")
      .setEndAt("2017-12-12 01:00:16.000000"));
    subject = fabricatorFactory.fabricate(segmentFactory.newSegment(BigInteger.valueOf(4213))
      .setOffset(BigInteger.valueOf(2))
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(120.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:16.000000")
      .setEndAt("2017-12-12 01:00:22.000000"));
  }


}
