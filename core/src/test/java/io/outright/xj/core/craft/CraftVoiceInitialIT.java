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
import io.outright.xj.core.tables.records.MorphRecord;
import io.outright.xj.core.tables.records.PickRecord;

import org.jooq.Result;
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
import static io.outright.xj.core.Tables.MORPH;
import static io.outright.xj.core.Tables.PICK;
import static io.outright.xj.core.tables.Point.POINT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CraftVoiceInitialIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Injector injector = Guice.createInjector(new CoreModule());
  private CraftFactory craftFactory;

  // Testing entities for reference
  private Link link6;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "pigs"
    IntegrationTestEntity.insertAccount(1, "pigs");

    // Greg has "user" and "admin" roles, belongs to account "pigs", has "google" auth
    IntegrationTestEntity.insertUser(2, "greg", "greg@email.com", "http://pictures.com/greg.gif");
    IntegrationTestEntity.insertUserRole(1, 2, Role.ADMIN);

    // Tonya has a "user" role and belongs to account "pigs"
    IntegrationTestEntity.insertUser(3, "tonya", "tonya@email.com", "http://pictures.com/tonya.gif");
    IntegrationTestEntity.insertUserRole(2, 3, Role.USER);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Special, Wild to Cozy" macro-idea in house library
    IntegrationTestEntity.insertIdea(4, 3, 2, Idea.MACRO, "Special, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertIdeaMeme(2, 4, "Special");
    IntegrationTestEntity.insertPhase(3, 4, 0, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    IntegrationTestEntity.insertPhase(4, 4, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");

    // Main idea
    IntegrationTestEntity.insertIdea(5, 3, 2, Idea.MAIN, "Main Jam", 0.2, "F# minor", 140);
    IntegrationTestEntity.insertIdeaMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPhase(15, 5, 0, 16, "Intro", 0.5, "F# minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Pessimism");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "F# minor");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPhase(16, 5, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Optimism");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "G major");

    // A basic beat, first phase has voice and events
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
    IntegrationTestEntity.insertPhase(316, 35, 1, 16, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(347, 316, "Heavy");

    // Support Idea
    IntegrationTestEntity.insertIdea(7, 3, 2, Idea.SUPPORT, "Support Jam", 0.3, "Cb minor", 170);

    // Chain "Print #2" has 1 initial link in crafting state - Foundation is complete
    IntegrationTestEntity.insertChain(2, 1, "Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);
    link6 = IntegrationTestEntity.insertLink(6, 2, 0, Link.CRAFTING, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:07.384616"), "C minor", 16, 0.55, 130);
    IntegrationTestEntity.insertLinkMeme(101, 6, "Special");
    IntegrationTestEntity.insertLinkMeme(102, 6, "Wild");
    IntegrationTestEntity.insertLinkMeme(103, 6, "Pessimism");
    IntegrationTestEntity.insertLinkMeme(104, 6, "Outlook");
    IntegrationTestEntity.insertChoice(101, 6, 4, Choice.MACRO, 0, 0);
    IntegrationTestEntity.insertChoice(102, 6, 5, Choice.MAIN, 10, -6);
    IntegrationTestEntity.insertLinkChord(101, 6, 0, "C minor");
    IntegrationTestEntity.insertLinkChord(102, 6, 8, "Db minor");

    // choice of rhythm-type idea
    IntegrationTestEntity.insertChoice(103, 6, 35, Choice.RHYTHM, 0, 0);

    // Instrument "808"
    IntegrationTestEntity.insertInstrument(1, 2, 2, "808 Drums", Instrument.PERCUSSIVE, 0.9);
    IntegrationTestEntity.insertInstrumentMeme(1, 1, "heavy");

    // Audio "Kick"
    IntegrationTestEntity.insertAudio(1, 1, "Kick", "https://static.xj.outright.io/19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioEvent(1, 1, 2.5, 1, "KICK", "Eb", 0.8, 1.0);

    // Audio "Snare"
    IntegrationTestEntity.insertAudio(2, 1, "Snare", "https://static.xj.outright.io/198017350afghjkjhaskjdfjhk975898.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(2, 2, 3, 1, "SNARE", "Ab", 0.1, 0.8);

    // bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 2, 2);

    // Instantiate the test subject
    craftFactory = injector.getInstance(CraftFactory.class);

  }

  @After
  public void tearDown() throws Exception {
    craftFactory = null;
  }

  @Test
  public void craftVoiceInitial() throws Exception {
    Basis basis = craftFactory.createBasis(link6);

    craftFactory.voice(basis).craft();

    ArrangementRecord resultArrangement =
      IntegrationTestService.getDb()
        .selectFrom(ARRANGEMENT)
        .where(ARRANGEMENT.CHOICE_ID.eq(ULong.valueOf(103)))
        .fetchOne();
    assertNotNull(resultArrangement);

    assertEquals(16, IntegrationTestService.getDb()
      .selectFrom(MORPH)
      .where(MORPH.ARRANGEMENT_ID.eq(resultArrangement.getId()))
      .fetch().size());

    assertEquals(8, IntegrationTestService.getDb()
      .selectFrom(PICK)
      .where(PICK.AUDIO_ID.eq(ULong.valueOf(1)))
      .fetch().size());

    assertEquals(8, IntegrationTestService.getDb()
      .selectFrom(PICK)
      .where(PICK.AUDIO_ID.eq(ULong.valueOf(2)))
      .fetch().size());

    assertEquals(4, IntegrationTestService.getDb()
      .selectFrom(POINT)
      .where(POINT.VOICE_EVENT_ID.eq(ULong.valueOf(1)))
      .fetch().size());

    assertEquals(4, IntegrationTestService.getDb()
      .selectFrom(POINT)
      .where(POINT.VOICE_EVENT_ID.eq(ULong.valueOf(2)))
      .fetch().size());

    assertEquals(4, IntegrationTestService.getDb()
      .selectFrom(POINT)
      .where(POINT.VOICE_EVENT_ID.eq(ULong.valueOf(3)))
      .fetch().size());

    assertEquals(4, IntegrationTestService.getDb()
      .selectFrom(POINT)
      .where(POINT.VOICE_EVENT_ID.eq(ULong.valueOf(4)))
      .fetch().size());

  }
}
