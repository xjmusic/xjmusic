// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.craft;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.voice.Voice;
import io.outright.xj.core.tables.records.ArrangementRecord;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Timestamp;

import static io.outright.xj.core.Tables.ARRANGEMENT;
import static io.outright.xj.core.Tables.PICK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CraftVoiceNextMacroIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Injector injector = Guice.createInjector(new CoreModule());
  private CraftFactory craftFactory;

  // Testing entities for reference
  private Link link4;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "crows"
    IntegrationTestEntity.insertAccount(1, "crows");

    // John has "user" and "admin" roles, belongs to account "crows", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, Role.ADMIN);

    // Jenny has a "user" role and belongs to account "crows"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(2, 3, Role.USER);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Tropical, Wild to Cozy" macro-idea in house library
    IntegrationTestEntity.insertIdea(4, 3, 2, Idea.MACRO, "Tropical, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertIdeaMeme(2, 4, "Tropical");
    // " phase offset 0
    IntegrationTestEntity.insertPhase(3, 4, 0, 0, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    // " phase offset 1
    IntegrationTestEntity.insertPhase(4, 4, 1, 0, "Intermediate", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPhaseMeme(49, 4, "Wild");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");
    // " phase offset 2
    IntegrationTestEntity.insertPhase(5, 4, 2, 0, "Finish Cozy", 0.4, "Ab minor", 125);
    IntegrationTestEntity.insertPhaseMeme(5, 4, "Cozy");
    IntegrationTestEntity.insertPhaseChord(5, 5, 0, "Ab minor");

    // "Tangy, Chunky to Smooth" macro-idea in house library
    IntegrationTestEntity.insertIdea(3, 3, 2, Idea.MACRO, "Tangy, Chunky to Smooth", 0.5, "G minor", 120);
    IntegrationTestEntity.insertIdeaMeme(1, 3, "Tangy");
    // # phase offset 0
    IntegrationTestEntity.insertPhase(1, 3, 0, 0, "Start Chunky", 0.4, "G minor", 115);
    IntegrationTestEntity.insertPhaseMeme(1, 1, "Chunky");
    IntegrationTestEntity.insertPhaseChord(1, 1, 0, "G minor");
    // # phase offset 1
    IntegrationTestEntity.insertPhase(2, 3, 1, 0, "Finish Smooth", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(2, 2, "Smooth");
    IntegrationTestEntity.insertPhaseChord(2, 2, 0, "C");

    // Main idea
    IntegrationTestEntity.insertIdea(5, 3, 2, Idea.MAIN, "Main Jam", 0.2, "C minor", 140);
    IntegrationTestEntity.insertIdeaMeme(3, 5, "Outlook");
    // # phase offset 0
    IntegrationTestEntity.insertPhase(15, 5, 0, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Optimism");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "G major");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "Ab minor");
    // # phase offset 1
    IntegrationTestEntity.insertPhase(16, 5, 1, 16, "Drop", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Pessimism");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "C major");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "Bb minor");

    // Another Main idea to go to
    IntegrationTestEntity.insertIdea(15, 3, 2, Idea.MAIN, "Next Jam", 0.2, "Db minor", 140);
    IntegrationTestEntity.insertIdeaMeme(43, 15, "Hindsight");
    IntegrationTestEntity.insertPhase(415, 15, 0, 16, "Intro", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(46, 415, "Regret");
    IntegrationTestEntity.insertPhaseChord(412, 415, 0, "G minor");
    IntegrationTestEntity.insertPhaseChord(414, 415, 8, "Ab minor");
    IntegrationTestEntity.insertPhase(416, 15, 1, 16, "Outro", 0.5, "A major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(47, 416, "Pride");
    IntegrationTestEntity.insertPhaseMeme(149, 416, "Shame");
    IntegrationTestEntity.insertPhaseChord(416, 416, 0, "C major");
    IntegrationTestEntity.insertPhaseChord(418, 416, 8, "Bb major");

    // A basic beat
    IntegrationTestEntity.insertIdea(35, 3, 2, Idea.RHYTHM, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertIdeaMeme(343, 35, "Basic");
    IntegrationTestEntity.insertPhase(315, 35, 0, 4, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(346, 315, "Heavy");

    // setup voice phase events
    IntegrationTestEntity.insertVoice(1, 315, Voice.PERCUSSIVE, "drums");

    // Voice "Drums" has events "BOOM" and "SMACK" 2x each
    IntegrationTestEntity.insertVoiceEvent(1, 1, 0, 1, "BOOM", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertVoiceEvent(2, 1, 1, 1, "SMACK", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertVoiceEvent(3, 1, 2.5, 1, "BOOM", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertVoiceEvent(4, 1, 3, 1, "SMACK", "G5", 0.1, 0.9);

    // basic beat second phase
    IntegrationTestEntity.insertPhase(316, 35, 1, 4, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(347, 316, "Heavy");

    // Chain "Test Print #1" has 5 total links
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);
    IntegrationTestEntity.insertLink(1, 1, 0, Link.DUBBED, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120);
    IntegrationTestEntity.insertLink(2, 1, 1, Link.DUBBING, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120);

    // Chain "Test Print #1" has this link that was just crafted
    IntegrationTestEntity.insertLink(3, 1, 2, Link.CRAFTED, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "Ab minor", 64, 0.30, 120); // final key is based on phase of main idea
    IntegrationTestEntity.insertChoice(25, 3, 4, Choice.MACRO, 1, 3); // macro-idea current phase is transposed to be Db minor
    IntegrationTestEntity.insertChoice(26, 3, 5, Choice.MAIN, 1, 1); // main-key of previous link is transposed to match, Db minor
    IntegrationTestEntity.insertChoice(27, 3, 35, Choice.RHYTHM, 0, -4);

    // Chain "Test Print #1" has a link in crafting state - Structure is complete
    link4 = IntegrationTestEntity.insertLink(4, 1, 3, Link.CRAFTING, Timestamp.valueOf("2017-02-14 12:03:08.000001"),Timestamp.valueOf("2017-02-14 12:03:15.836735"),"F minor", 16, 0.45, 125);
    IntegrationTestEntity.insertLinkMeme(101,4,"Hindsight");
    IntegrationTestEntity.insertLinkMeme(102,4,"Chunky");
    IntegrationTestEntity.insertLinkMeme(103,4,"Regret");
    IntegrationTestEntity.insertLinkMeme(104,4,"Tangy");
    IntegrationTestEntity.insertChoice(101,4, 3, Choice.MACRO,0,4);
    IntegrationTestEntity.insertChoice(102,4, 15, Choice.MAIN,0,-2);
    IntegrationTestEntity.insertLinkChord(101,4,0,"F minor");
    IntegrationTestEntity.insertLinkChord(102,4,8,"Gb minor");

    // choice of rhythm-type idea
    IntegrationTestEntity.insertChoice(103,4,35,Choice.RHYTHM,0,5);

    // Instrument "808"
    IntegrationTestEntity.insertInstrument(1, 2, 2, "808 Drums", Instrument.PERCUSSIVE, 0.9);
    IntegrationTestEntity.insertInstrumentMeme(1,1,"heavy");

    // Audio "Kick"
    IntegrationTestEntity.insertAudio(1, 1, "Kick", "https://static.xj.outright.io/19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioEvent(1, 1, 2.5, 1, "KICK", "Eb", 0.8, 1.0);

    // Audio "Snare"
    IntegrationTestEntity.insertAudio(2, 1, "Snare", "https://static.xj.outright.io/198017350afghjkjhaskjdfjhk975898.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(2, 2, 3, 1, "SNARE", "Ab", 0.1, 0.8);

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 1, 2);

    // Instantiate the test subject
    craftFactory = injector.getInstance(CraftFactory.class);
  }

  @After
  public void tearDown() throws Exception {
    craftFactory = null;
  }

  @Test
  public void craftVoiceNextMacro() throws Exception {
    Basis basis =craftFactory.createBasis(link4);

    craftFactory.voice(basis).craft();

    ArrangementRecord resultArrangement =
      IntegrationTestService.getDb()
        .selectFrom(ARRANGEMENT)
        .where(ARRANGEMENT.CHOICE_ID.eq(ULong.valueOf(103)))
        .fetchOne();
    assertNotNull(resultArrangement);

    assertEquals(8, IntegrationTestService.getDb()
      .selectFrom(PICK)
      .where(PICK.AUDIO_ID.eq(ULong.valueOf(1)))
      .fetch().size());

    assertEquals(8, IntegrationTestService.getDb()
      .selectFrom(PICK)
      .where(PICK.AUDIO_ID.eq(ULong.valueOf(2)))
      .fetch().size());
  }

}
