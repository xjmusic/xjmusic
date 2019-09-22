//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.fabricator;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.CoreTest;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment.sub.Arrangement;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.segment.sub.Pick;
import io.xj.core.model.program.ProgramType;
import io.xj.music.Tuning;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FabricatorImplTest extends CoreTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  public TimeComputerFactory timeComputerFactory;
  @Mock
  public TimeComputer timeComputer;
  @Mock
  public ProgramDAO programDAO;
  @Mock
  public ChainDAO chainDAO;
  @Mock
  public SegmentDAO segmentDAO;
  @Mock
  public Tuning tuning;
  //
  private Fabricator subject;
  private FabricatorFactory fabricatorFactory;

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ProgramDAO.class).toInstance(programDAO);
          bind(SegmentDAO.class).toInstance(segmentDAO);
          bind(ChainDAO.class).toInstance(chainDAO);
          bind(Tuning.class).toInstance(tuning);
          bind(TimeComputerFactory.class).toInstance(timeComputerFactory);
        }
      }));
    segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
  }

  @Test
  public void timeComputer() throws Exception {
    when(chainDAO.readOne(any(), eq(BigInteger.valueOf(977))))
      .thenReturn(newChain(977, 5, "test", ChainType.Production, ChainState.Fabricate, Instant.parse("2017-12-12T01:00:08.000000Z"), null, null, Instant.parse("2017-12-12T01:00:08.000000Z")));
    when(segmentDAO.readOneAtChainOffset(any(), eq(BigInteger.valueOf(977)), eq(Long.valueOf(1))))
      .thenReturn(newSegment(4212, 977, 1, SegmentState.Crafted, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 8, 0.6, 120, "seg123.ogg"));
    subject = fabricatorFactory.fabricate(segmentFactory.newSegment(BigInteger.valueOf(4213))
      .setOffset(2L)
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(240.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z"));
    when(timeComputerFactory.create(anyDouble(), anyDouble(), anyDouble())).thenReturn(timeComputer);
    when(timeComputer.getSecondsAtPosition(anyDouble())).thenReturn(Double.valueOf(0));

    assertEquals(Double.valueOf(0), subject.computeSecondsAtPosition(0)); // instantiates a time computer; see expectation above

    verify(timeComputerFactory).create(8.0, 120, 240.0);
  }

  @Test
  public void timeComputer_variedSegmentTotals() throws Exception {
    when(chainDAO.readOne(any(), eq(BigInteger.valueOf(977))))
      .thenReturn(newChain(977, 5, "test", ChainType.Production, ChainState.Fabricate, Instant.parse("2017-12-12T01:00:08.000000Z"), null, null, Instant.parse("2017-12-12T01:00:08.000000Z")));
    when(segmentDAO.readOneAtChainOffset(any(), eq(BigInteger.valueOf(977)), eq(Long.valueOf(1))))
      .thenReturn(newSegment(4212, 977, 1, SegmentState.Crafted, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 32, 0.6, 120, "seg123.ogg"));
    subject = fabricatorFactory.fabricate(segmentFactory.newSegment(BigInteger.valueOf(4213))
      .setOffset(2L)
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(240.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(16)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z"));
    when(timeComputerFactory.create(anyDouble(), anyDouble(), anyDouble())).thenReturn(timeComputer);
    when(timeComputer.getSecondsAtPosition(anyDouble())).thenReturn(Double.valueOf(0));

    assertEquals(Double.valueOf(0), subject.computeSecondsAtPosition(0)); // instantiates a time computer; see expectation above

    verify(timeComputerFactory).create(16, 120, 240.0);
  }

  @Test
  public void timeComputer_weirdSegmentTotalsAndTempos() throws Exception {
    when(chainDAO.readOne(any(), eq(BigInteger.valueOf(977))))
      .thenReturn(newChain(977, 5, "test", ChainType.Production, ChainState.Fabricate, Instant.parse("2017-12-12T01:00:08.000000Z"), null, null, Instant.parse("2017-12-12T01:00:08.000000Z")));
    when(segmentDAO.readOneAtChainOffset(any(), eq(BigInteger.valueOf(977)), eq(Long.valueOf(1))))
      .thenReturn(newSegment(4212, 977, 1, SegmentState.Crafted, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 23, 0.6, 67, "seg123.ogg"));
    subject = fabricatorFactory.fabricate(segmentFactory.newSegment(BigInteger.valueOf(4213))
      .setOffset(2L)
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(121.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(79)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z"));
    when(timeComputerFactory.create(anyDouble(), anyDouble(), anyDouble())).thenReturn(timeComputer);
    when(timeComputer.getSecondsAtPosition(anyDouble())).thenReturn(Double.valueOf(0));

    assertEquals(Double.valueOf(0), subject.computeSecondsAtPosition(0)); // instantiates a time computer; see expectation above

    verify(timeComputerFactory).create(79, 67.0, 121.0);
  }

  @Test
  public void pick_returned_by_picks() throws Exception {
    when(chainDAO.readOne(any(), eq(BigInteger.valueOf(977))))
      .thenReturn(newChain(977, 5, "test", ChainType.Production, ChainState.Fabricate, Instant.parse("2017-12-12T01:00:08.000000Z"), null, null, Instant.parse("2017-12-12T01:00:08.000000Z")));
    when(segmentDAO.readOneAtChainOffset(any(), eq(BigInteger.valueOf(977)), eq(Long.valueOf(1))))
      .thenReturn(newSegment(4212, 977, 1, SegmentState.Crafted, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 8, 0.6, 120, "seg123.ogg"));
    subject = fabricatorFactory.fabricate(segmentFactory.newSegment(BigInteger.valueOf(4213))
      .setOffset(2L)
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(120.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z"));
    Choice choice = subject.add(new Choice()
      .setProgramId(BigInteger.valueOf(5))
      .setTypeEnum(ProgramType.Rhythm)
      .setTranspose(-5)
      .setSegmentId(BigInteger.valueOf(1)));
    Arrangement arrangement = subject.add(new Arrangement()
      .setInstrumentId(BigInteger.valueOf(1))
      .setChoiceId(choice.getId())
      .setVoiceId(UUID.randomUUID()));
    UUID audioId = UUID.randomUUID();
    subject.add(new Pick()
      .setArrangementId(arrangement.getId())
      .setVoiceId(UUID.randomUUID())
      .setAudioId(audioId)
      .setEventId(UUID.randomUUID())
      .setStart(0.273)
      .setLength(1.571)
      .setAmplitude(0.8)
      .setPitch(432.0));

    Collection<Pick> result = subject.getSegment().getPicks();
    Pick resultPick = result.iterator().next();
    assertEquals(arrangement.getId(), resultPick.getArrangementId());
    assertEquals(audioId, resultPick.getAudioId());
    assertEquals(0.273, resultPick.getStart(), 0.001);
    assertEquals(1.571, resultPick.getLength(), 0.001);
    assertEquals(0.8, resultPick.getAmplitude(), 0.1);
    assertEquals(432.0, resultPick.getPitch(), 0.1);
  }

  @Test
  public void getOutputAudioFormat() throws Exception {
    when(chainDAO.readOne(any(), eq(BigInteger.valueOf(977))))
      .thenReturn(newChain(977, 5, "test", ChainType.Production, ChainState.Fabricate, Instant.parse("2017-12-12T01:00:08.000000Z"), null, null, Instant.parse("2017-12-12T01:00:08.000000Z")));
    when(segmentDAO.readOneAtChainOffset(any(), eq(BigInteger.valueOf(977)), eq(Long.valueOf(1))))
      .thenReturn(newSegment(4212, 977, 1, SegmentState.Crafted, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 8, 0.6, 120, "seg123.ogg"));
    subject = fabricatorFactory.fabricate(segmentFactory.newSegment(BigInteger.valueOf(4213))
      .setOffset(2L)
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(240.0)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z"));

    assertEquals("PCM_SIGNED", subject.getOutputAudioFormat().getEncoding().toString()); // instantiates a time computer; see expectation above
  }


}
