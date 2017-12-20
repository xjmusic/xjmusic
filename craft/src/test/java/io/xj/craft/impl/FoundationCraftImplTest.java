package io.xj.craft.impl;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

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
import io.xj.core.work.basis.Basis;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import io.xj.craft.FoundationCraft;
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

@RunWith(MockitoJUnitRunner.class)
public class FoundationCraftImplTest {
  private Injector injector;
  private FoundationCraft subject;
  private CraftFactory craftFactory;
  @Mock Basis basis;
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
    craftFactory = injector.getInstance(CraftFactory.class);
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
  public void doWork() throws Exception {
/*
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
*/
    subject = craftFactory.foundation(basis);

    // This is an astronomically complex unit test for some time in the future...
//    subject.doWork();

  }

}
