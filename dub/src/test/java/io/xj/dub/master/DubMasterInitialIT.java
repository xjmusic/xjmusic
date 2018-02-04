// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master;

import io.xj.core.CoreModule;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.PhaseState;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.craft.CraftModule;
import io.xj.craft.basis.Basis;
import io.xj.craft.basis.BasisFactory;
import io.xj.dub.DubFactory;
import io.xj.dub.DubModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Timestamp;

public class DubMasterInitialIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule(), new DubModule());
  private DubFactory dubFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Link link6;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "pigs"
    IntegrationTestEntity.insertAccount(1, "pigs");

    // Greg has "user" and "admin" roles, belongs to account "pigs", has "google" auth
    IntegrationTestEntity.insertUser(2, "greg", "greg@email.com", "http://pictures.com/greg.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Tonya has a "user" role and belongs to account "pigs"
    IntegrationTestEntity.insertUser(3, "tonya", "tonya@email.com", "http://pictures.com/tonya.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Special, Wild to Cozy" macro-pattern in house library
    IntegrationTestEntity.insertPattern(4, 3, 2, PatternType.Macro, PatternState.Published, "Special, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertPatternMeme(2, 4, "Special");
    IntegrationTestEntity.insertPhase(3, 4, PhaseType.Macro, PhaseState.Published, 0, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    IntegrationTestEntity.insertPhase(4, 4, PhaseType.Macro, PhaseState.Published, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");

    // Main pattern
    IntegrationTestEntity.insertPattern(5, 3, 2, PatternType.Main, PatternState.Published, "Main Jam", 0.2, "F# minor", 140);
    IntegrationTestEntity.insertPatternMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPhase(15, 5, PhaseType.Main, PhaseState.Published, 0, 16, "Intro", 0.5, "F# minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Pessimism");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "F# minor");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPhase(16, 5, PhaseType.Main, PhaseState.Published, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Optimism");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "G major");

    // A basic beat, first phase has voice and events
    IntegrationTestEntity.insertPattern(35, 3, 2, PatternType.Rhythm, PatternState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertPatternMeme(343, 35, "Basic");
    IntegrationTestEntity.insertPhase(315, 35, PhaseType.Loop, PhaseState.Published, 0, 4, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(346, 315, "Heavy");

    // setup voice phase events
    IntegrationTestEntity.insertVoice(1, 35, InstrumentType.Percussive, "drums");

    // Voice "Drums" has events "BOOM" and "SMACK" 2x each
    IntegrationTestEntity.insertPhaseEvent(1, 315, 1, 0, 1, "BOOM", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPhaseEvent(2, 315, 1, 1, 1, "SMACK", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPhaseEvent(3, 315, 1, 2.5, 1, "BOOM", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPhaseEvent(4, 315, 1, 3, 1, "SMACK", "G5", 0.1, 0.9);

    // basic beat second phase
    IntegrationTestEntity.insertPhase(316, 35, PhaseType.Loop, PhaseState.Published, 1, 16, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(347, 316, "Heavy");

    // Detail Pattern
    IntegrationTestEntity.insertPattern(7, 3, 2, PatternType.Detail, PatternState.Published, "Detail Jam", 0.3, "Cb minor", 170);

    // Chain "Print #2" has 1 initial link in dubbing state - Master is complete
    IntegrationTestEntity.insertChain(2, 1, "Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    link6 = IntegrationTestEntity.insertLink(6, 2, 0, LinkState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:07.384616"), "C minor", 16, 0.55, 130, "chain-2-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLinkMeme(101, 6, "Special");
    IntegrationTestEntity.insertLinkMeme(102, 6, "Wild");
    IntegrationTestEntity.insertLinkMeme(103, 6, "Pessimism");
    IntegrationTestEntity.insertLinkMeme(104, 6, "Outlook");
    IntegrationTestEntity.insertChoice(101, 6, 4, PatternType.Macro, 0, 0);
    IntegrationTestEntity.insertChoice(102, 6, 5, PatternType.Main, 10, -6);
    IntegrationTestEntity.insertLinkChord(101, 6, 0, "C minor");
    IntegrationTestEntity.insertLinkChord(102, 6, 8, "Db minor");

    // choice of rhythm-type pattern
    IntegrationTestEntity.insertChoice(103, 6, 35, PatternType.Rhythm, 0, 0);

    // Instrument, InstrumentMeme
    IntegrationTestEntity.insertInstrument(1, 2, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrumentMeme(1, 1, "heavy");

    // Audio, AudioEvent
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Kick", "https://static.xj.io/19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioEvent(1, 1, 2.5, 1, "KICK", "Eb", 0.8, 1.0);

    // Audio, AudioEvent
    IntegrationTestEntity.insertAudio(2, 1, "Published", "Snare", "https://static.xj.io/198017350afghjkjhaskjdfjhk975898.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(2, 2, 3, 1, "SNARE", "Ab", 0.1, 0.8);

    // future: insert arrangement of choice 103
    // future: insert 8 picks of audio 1
    // future: insert 8 picks of audio 2

    // bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 2, 2);

    // Instantiate the test subject
    dubFactory = injector.getInstance(DubFactory.class);
    basisFactory = injector.getInstance(BasisFactory.class);

  }

  @After
  public void tearDown() throws Exception {
    dubFactory = null;
    basisFactory = null;
  }

  @Test
  public void dubMasterInitial() throws Exception {
    Basis basis = basisFactory.createBasis(link6);

    dubFactory.master(basis).doWork();

    // future test: success of dub master continue test
  }

/*
future:

  @Test
  public void dubMasterInitial_failsIfLinkHasNoWaveformKey() throws Exception {
    IntegrationTestService.getDb().update(LINK)
      .set(LINK.WAVEFORM_KEY, DSL.value((String) null))
      .where(LINK.ID.eq(BigInteger.valueOf(6)))
      .execute();

    failure.expectMessage("Link has no waveform key!");
    failure.expect(BusinessException.class);

    Basis basis = basisFactory.createBasis(link6);

    dubFactory.master(basis).doWork();
  }
*/

}
