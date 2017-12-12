package io.xj.core.work.basis.impl;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

import io.xj.core.CoreModule;
import io.xj.core.dao.ArrangementDAO;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.dao.ChainConfigDAO;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.LinkChordDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.dao.LinkMemeDAO;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternMemeDAO;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.dao.PhaseMemeDAO;
import io.xj.core.dao.PickDAO;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.dao.VoiceEventDAO;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.tables.records.LinkRecord;
import io.xj.core.work.basis.Basis;
import io.xj.core.work.basis.BasisFactory;
import io.xj.music.Tuning;

import org.jooq.types.ULong;

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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BasisImplTest {
  private Injector injector;
  private Basis subject;
  private BasisFactory basisFactory;
  @Mock ArrangementDAO arrangementDAO;
  @Mock AudioDAO audioDAO;
  @Mock AudioEventDAO audioEventDAO;
  @Mock ChainConfigDAO chainConfigDAO;
  @Mock ChoiceDAO choiceDAO;
  @Mock PatternDAO patternDAO;
  @Mock PatternMemeDAO patternMemeDAO;
  @Mock LinkChordDAO linkChordDAO;
  @Mock LinkDAO linkDAO;
  @Mock LinkMemeDAO linkMemeDAO;
  @Mock LinkMessageDAO linkMessageDAO;
  @Mock PhaseDAO phaseDAO;
  @Mock PhaseMemeDAO phaseMemeDAO;
  @Mock PickDAO pickDAO;
  @Mock Tuning tuning;
  @Mock VoiceDAO voiceDAO;
  @Mock VoiceEventDAO voiceEventDAO;

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
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ArrangementDAO.class).toInstance(arrangementDAO);
          bind(AudioDAO.class).toInstance(audioDAO);
          bind(AudioEventDAO.class).toInstance(audioEventDAO);
          bind(ChainConfigDAO.class).toInstance(chainConfigDAO);
          bind(ChoiceDAO.class).toInstance(choiceDAO);
          bind(PatternDAO.class).toInstance(patternDAO);
          bind(PatternMemeDAO.class).toInstance(patternMemeDAO);
          bind(LinkChordDAO.class).toInstance(linkChordDAO);
          bind(LinkDAO.class).toInstance(linkDAO);
          bind(LinkMemeDAO.class).toInstance(linkMemeDAO);
          bind(LinkMessageDAO.class).toInstance(linkMessageDAO);
          bind(PhaseDAO.class).toInstance(phaseDAO);
          bind(PhaseMemeDAO.class).toInstance(phaseMemeDAO);
          bind(PickDAO.class).toInstance(pickDAO);
          bind(Tuning.class).toInstance(tuning);
          bind(VoiceDAO.class).toInstance(voiceDAO);
          bind(VoiceEventDAO.class).toInstance(voiceEventDAO);
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
  public void link() throws Exception {
  }

  @Test
  public void isInitialLink() throws Exception {
  }

  @Test
  public void linkId() throws Exception {
  }

  @Test
  public void chainId() throws Exception {
  }

  @Test
  public void chainConfig() throws Exception {
  }

  @Test
  public void linkBeginAt() throws Exception {
  }

  @Test
  public void previousLink() throws Exception {
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
  public void previousMacroPhase() throws Exception {
  }

  @Test
  public void previousMacroNextPhase() throws Exception {
  }

  @Test
  public void pattern() throws Exception {
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
    when(linkDAO.readOneAtChainOffset(any(),eq(ULong.valueOf(977)),eq(ULong.valueOf(1)))).thenReturn(new Link(4212)
      .setOffset(BigInteger.valueOf(1))
      .setDensity(0.6)
      .setKey("F major")
      .setState("Crafted")
      .setTempo(60)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:08.000000")
      .setEndAt("2017-12-12 01:00:16.000000"));
    subject = basisFactory.createBasis(new Link(4213)
      .setOffset(BigInteger.valueOf(2))
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(120)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:16.000000")
      .setEndAt("2017-12-12 01:00:22.000000"));

    assertEquals(Double.valueOf(0),subject.secondsAtPosition(0));
  }

  @Test
  public void secondsAtPosition_tempoChangeMiddle() throws Exception {
    when(linkDAO.readOneAtChainOffset(any(),eq(ULong.valueOf(977)),eq(ULong.valueOf(1)))).thenReturn(new Link(4212)
      .setOffset(BigInteger.valueOf(1))
      .setDensity(0.6)
      .setKey("F major")
      .setState("Crafted")
      .setTempo(60)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:08.000000")
      .setEndAt("2017-12-12 01:00:16.000000"));
    subject = basisFactory.createBasis(new Link(4213)
      .setOffset(BigInteger.valueOf(2))
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(120)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:16.000000")
      .setEndAt("2017-12-12 01:00:22.000000"));

    assertEquals(Double.valueOf(3.53125),subject.secondsAtPosition(4));
  }

  @Test
  public void secondsAtPosition_tempoChangeEnd() throws Exception {
    when(linkDAO.readOneAtChainOffset(any(),eq(ULong.valueOf(977)),eq(ULong.valueOf(1)))).thenReturn(new Link(4212)
      .setOffset(BigInteger.valueOf(1))
      .setDensity(0.6)
      .setKey("F major")
      .setState("Crafted")
      .setTempo(60)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:08.000000")
      .setEndAt("2017-12-12 01:00:16.000000"));
    subject = basisFactory.createBasis(new Link(4213)
      .setOffset(BigInteger.valueOf(2))
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(120)
      .setChainId(BigInteger.valueOf(977))
      .setTotal(8)
      .setBeginAt("2017-12-12 01:00:16.000000")
      .setEndAt("2017-12-12 01:00:22.000000"));

    assertEquals(Double.valueOf(6.0625),subject.secondsAtPosition(8));
  }

  @Test
  public void patternMemes() throws Exception {
  }

  @Test
  public void voiceEvents() throws Exception {
  }

  @Test
  public void instrumentAudioEvents() throws Exception {
  }

  @Test
  public void instrumentAudios() throws Exception {
  }

  @Test
  public void linkAudio() throws Exception {
  }

  @Test
  public void linkAudios() throws Exception {
  }

  @Test
  public void linkAudioIds() throws Exception {
  }

  @Test
  public void linkChords() throws Exception {
  }

  @Test
  public void linkMemes() throws Exception {
  }

  @Test
  public void picks() throws Exception {
  }

  @Test
  public void linkTotalLength() throws Exception {
  }

  @Test
  public void linkMeme() throws Exception {
  }

  @Test
  public void phaseMemes() throws Exception {
  }

  @Test
  public void phaseByOffset() throws Exception {
  }

  @Test
  public void linkByOffset() throws Exception {
  }

  @Test
  public void linkChoiceByType() throws Exception {
  }

  @Test
  public void voices() throws Exception {
  }

  @Test
  public void updateLink() throws Exception {
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
