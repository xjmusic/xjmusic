// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.digest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.PhaseEventDAO;
import io.xj.core.digest.chord_sequence.DigestChordProgression;
import io.xj.core.digest.hash.DigestHash;
import io.xj.core.digest.meme.DigestMeme;
import io.xj.core.digest.meme.impl.DigestMemesItem;
import io.xj.core.evaluation.EvaluationFactory;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.library.Library;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.model.phase_event.PhaseEvent;
import io.xj.core.model.user_role.UserRoleType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestHashIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private EvaluationFactory evaluationFactory;
  private DigestFactory digestFactory;

  /**
   assert if two collections are equivalent, irrelevant of what type of collection

   @param o1 collection to compare
   @param o2 collection to compare
   */
  private static void assertEquivalent(Collection<BigInteger> o1, Collection<BigInteger> o2) {
    ImmutableList.Builder<String> builder1 = ImmutableList.builder();
    o1.forEach(bigInteger -> builder1.add(bigInteger.toString()));
    ImmutableList.Builder<String> builder2 = ImmutableList.builder();
    o2.forEach(bigInteger -> builder2.add(bigInteger.toString()));
    assertEquals(builder1.build(), builder2.build());
  }

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    IntegrationTestEntity.insertAccount(1, "testing");
    IntegrationTestEntity.insertUser(101, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 101, UserRoleType.Admin);
    Timestamp at = Timestamp.valueOf("2014-08-12 12:17:02.527142");
    //
    IntegrationTestEntity.insertLibrary(10000001, 1, "leaves", at);
    IntegrationTestEntity.insertInstrument(201, 10000001, 101, "808 Drums", InstrumentType.Percussive, 0.9, at);
    IntegrationTestEntity.insertInstrument(202, 10000001, 101, "909 Drums", InstrumentType.Percussive, 0.8, at);
    IntegrationTestEntity.insertInstrumentMeme(301, 201, "Ants", at);
    IntegrationTestEntity.insertInstrumentMeme(302, 201, "Mold", at);
    IntegrationTestEntity.insertInstrumentMeme(303, 202, "Peel", at);
    IntegrationTestEntity.insertAudio(401, 201, "Published", "Beat", "https://static.xj.io/19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, at);
    IntegrationTestEntity.insertAudio(402, 201, "Published", "Chords Cm to D", "https://static.xj.io/instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440, at);
    IntegrationTestEntity.insertAudioChord(501, 402, 0, "E minor", at);
    IntegrationTestEntity.insertAudioChord(502, 402, 4, "A major", at);
    IntegrationTestEntity.insertAudioChord(503, 402, 8, "B minor", at);
    IntegrationTestEntity.insertAudioChord(504, 402, 12, "F# major", at);
    IntegrationTestEntity.insertAudioChord(505, 402, 16, "Ab7", at);
    IntegrationTestEntity.insertAudioChord(506, 402, 20, "Bb7", at);
    IntegrationTestEntity.insertAudioEvent(601, 401, 2.5, 1, "KICK", "Eb", 0.8, 1.0, at);
    IntegrationTestEntity.insertAudioEvent(602, 401, 3, 1, "SNARE", "Ab", 0.1, 0.8, at);
    IntegrationTestEntity.insertAudioEvent(603, 401, 0, 1, "KICK", "C", 0.8, 1.0, at);
    IntegrationTestEntity.insertAudioEvent(604, 401, 1, 1, "SNARE", "G", 0.1, 0.8, at);
    IntegrationTestEntity.insertPattern(701, 101, 10000001, PatternType.Rhythm, "leaves", 0.342, "C#", 120.4, at);
    IntegrationTestEntity.insertPattern(702, 101, 10000001, PatternType.Detail, "coconuts", 0.25, "F#", 110.3, at);
    IntegrationTestEntity.insertPattern(703, 101, 10000001, PatternType.Main, "bananas", 0.27, "Gb", 100.6, at);
    IntegrationTestEntity.insertPatternMeme(801, 701, "Ants", at);
    IntegrationTestEntity.insertPatternMeme(802, 701, "Mold", at);
    IntegrationTestEntity.insertPatternMeme(803, 703, "Peel", at);
    IntegrationTestEntity.insertPhase(901, 701, PhaseType.Main, 0, 16, "growth", 0.342, "C#", 120.4, at);
    IntegrationTestEntity.insertPhase(902, 701, PhaseType.Main, 1, 16, "decay", 0.25, "F#", 110.3, at);
    IntegrationTestEntity.insertPhaseChord(1001, 902, 0, "G minor", at);
    IntegrationTestEntity.insertPhaseChord(1002, 902, 4, "C major", at);
    IntegrationTestEntity.insertPhaseChord(1003, 902, 8, "F7", at);
    IntegrationTestEntity.insertPhaseChord(1004, 902, 12, "G7", at);
    IntegrationTestEntity.insertPhaseChord(1005, 902, 16, "F minor", at);
    IntegrationTestEntity.insertPhaseChord(1006, 902, 20, "Bb major", at);
    IntegrationTestEntity.insertPhaseMeme(1101, 901, "Gravel", at);
    IntegrationTestEntity.insertPhaseMeme(1102, 901, "Fuzz", at);
    IntegrationTestEntity.insertPhaseMeme(1103, 902, "Peel", at);
    IntegrationTestEntity.insertVoice(1201, 701, InstrumentType.Percussive, "Drums", at);
    IntegrationTestEntity.insertVoice(1202, 702, InstrumentType.Harmonic, "Bass", at);
    IntegrationTestEntity.insertPhaseEvent(1401, 901, 1201, 0, 1, "BOOM", "C", 0.8, 1.0, at);
    IntegrationTestEntity.insertPhaseEvent(1402, 901, 1201, 1, 1, "SMACK", "G", 0.1, 0.8, at);
    IntegrationTestEntity.insertPhaseEvent(1403, 901, 1201, 2.5, 1, "BOOM", "C", 0.8, 0.6, at);
    IntegrationTestEntity.insertPhaseEvent(1404, 901, 1201, 3, 1, "SMACK", "G", 0.1, 0.9, at);
    //
    // stuff that should not get used because it's in a different library
    IntegrationTestEntity.insertLibrary(10000002, 1, "Garbage Library", at);
    IntegrationTestEntity.insertInstrument(251, 10000002, 101, "Garbage Instrument A", InstrumentType.Percussive, 0.9, at);
    IntegrationTestEntity.insertInstrument(252, 10000002, 101, "Garbage Instrument B", InstrumentType.Percussive, 0.8, at);
    IntegrationTestEntity.insertInstrumentMeme(351, 251, "Garbage Instrument Meme A", at);
    IntegrationTestEntity.insertInstrumentMeme(352, 251, "Garbage Instrument Meme B", at);
    IntegrationTestEntity.insertInstrumentMeme(353, 252, "Garbage Instrument Meme C", at);
    IntegrationTestEntity.insertAudio(451, 251, "Published", "Garbage Audio A", "https://static.xj.io/19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, at);
    IntegrationTestEntity.insertAudio(452, 251, "Published", "Garbage audio B", "https://static.xj.io/instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440, at);
    IntegrationTestEntity.insertAudioChord(551, 452, 0, "E garbage", at);
    IntegrationTestEntity.insertAudioChord(552, 452, 4, "A major garbage", at);
    IntegrationTestEntity.insertAudioChord(553, 452, 8, "B minor garbage", at);
    IntegrationTestEntity.insertAudioChord(554, 452, 12, "F# major garbage", at);
    IntegrationTestEntity.insertAudioChord(555, 452, 16, "Ab7 garbage", at);
    IntegrationTestEntity.insertAudioChord(556, 452, 20, "Bb7 garbage", at);
    IntegrationTestEntity.insertAudioEvent(651, 451, 2.5, 1, "GARBAGE", "Eb", 0.8, 1.0, at);
    IntegrationTestEntity.insertAudioEvent(652, 451, 3, 1, "GARBAGE", "Ab", 0.1, 0.8, at);
    IntegrationTestEntity.insertAudioEvent(653, 451, 0, 1, "GARBAGE", "C", 0.8, 1.0, at);
    IntegrationTestEntity.insertAudioEvent(654, 451, 1, 1, "GARBAGE", "G", 0.1, 0.8, at);
    IntegrationTestEntity.insertPattern(751, 101, 10000002, PatternType.Rhythm, "Garbage Pattern A", 0.342, "C#", 120.4, at);
    IntegrationTestEntity.insertPattern(752, 101, 10000002, PatternType.Detail, "Garbage Pattern B", 0.25, "F#", 110.3, at);
    IntegrationTestEntity.insertPattern(753, 101, 10000002, PatternType.Main, "Garbage Pattern C", 0.27, "Gb", 100.6, at);
    IntegrationTestEntity.insertPatternMeme(851, 751, "Garbage Pattern Meme A", at);
    IntegrationTestEntity.insertPatternMeme(852, 751, "Garbage Pattern Meme B", at);
    IntegrationTestEntity.insertPatternMeme(853, 753, "Garbage Pattern Meme C", at);
    IntegrationTestEntity.insertPhase(951, 751, PhaseType.Main, 0, 16, "Garbage Phase A", 0.342, "C#", 120.4, at);
    IntegrationTestEntity.insertPhase(952, 751, PhaseType.Main, 1, 16, "Garbage Phase A", 0.25, "F#", 110.3, at);
    IntegrationTestEntity.insertPhaseChord(1051, 952, 0, "G minor garbage", at);
    IntegrationTestEntity.insertPhaseChord(1052, 952, 4, "C major garbage", at);
    IntegrationTestEntity.insertPhaseChord(1053, 952, 8, "F7 garbage", at);
    IntegrationTestEntity.insertPhaseChord(1054, 952, 12, "G7 garbage", at);
    IntegrationTestEntity.insertPhaseChord(1055, 952, 16, "F minor garbage", at);
    IntegrationTestEntity.insertPhaseChord(1056, 952, 20, "Bb major garbage", at);
    IntegrationTestEntity.insertPhaseMeme(1151, 951, "Garbage Phase Meme A", at);
    IntegrationTestEntity.insertPhaseMeme(1152, 951, "Garbage Phase Meme B", at);
    IntegrationTestEntity.insertPhaseMeme(1153, 952, "Garbage Phase Meme C", at);
    IntegrationTestEntity.insertVoice(1251, 751, InstrumentType.Percussive, "Garbage Voice A", at);
    IntegrationTestEntity.insertVoice(1252, 752, InstrumentType.Harmonic, "Garbage Voice B", at);
    IntegrationTestEntity.insertPhaseEvent(1451, 951, 1251, 0, 1, "GARBAGE", "C", 0.8, 1.0, at);
    IntegrationTestEntity.insertPhaseEvent(1452, 951, 1251, 1, 1, "GARBAGE", "G", 0.1, 0.8, at);
    IntegrationTestEntity.insertPhaseEvent(1453, 951, 1251, 2.5, 1, "GARBAGE", "C", 0.8, 0.6, at);
    IntegrationTestEntity.insertPhaseEvent(1454, 951, 1251, 3, 1, "GARBAGE", "G", 0.1, 0.9, at);

    // Instantiate the test evaluation and digestFactory
    evaluationFactory = injector.getInstance(EvaluationFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @After
  public void tearDown() throws Exception {
    evaluationFactory = null;
  }

  @Test
  public void readHash_ofLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Collection<Entity> entities = ImmutableList.of(new Library(10000001));

    DigestHash result = digestFactory.hashOf(evaluationFactory.evaluate(access, entities));

    assertNotNull(result);
    assertEquals("Audio-401=1407871023,Audio-402=1407871023,AudioChord-501=1407871023,AudioChord-502=1407871023,AudioChord-503=1407871023,AudioChord-504=1407871023,AudioChord-505=1407871023,AudioChord-506=1407871023,AudioEvent-601=1407871023,AudioEvent-602=1407871023,AudioEvent-603=1407871023,AudioEvent-604=1407871023,Instrument-201=1407871023,Instrument-202=1407871023,InstrumentMeme-301=1407871023,InstrumentMeme-302=1407871023,InstrumentMeme-303=1407871023,Library-10000001=1407871023,Pattern-701=1407871023,Pattern-702=1407871023,Pattern-703=1407871023,PatternMeme-801=1407871023,PatternMeme-802=1407871023,PatternMeme-803=1407871023,Phase-901=1407871023,Phase-902=1407871023,PhaseChord-1001=1407871023,PhaseChord-1002=1407871023,PhaseChord-1003=1407871023,PhaseChord-1004=1407871023,PhaseChord-1005=1407871023,PhaseChord-1006=1407871023,PhaseEvent-1401=1407871023,PhaseEvent-1402=1407871023,PhaseEvent-1403=1407871023,PhaseEvent-1404=1407871023,PhaseMeme-1101=1407871023,PhaseMeme-1102=1407871023,PhaseMeme-1103=1407871023,Voice-1201=1407871023,Voice-1202=1407871023", result.toString());
    JSONObject resultJson = result.toJSONObject();
    assertEquals(41, resultJson.length());
    assertEquals("a22531890987364093f470e0d846a7469b494e83ca014b075724a9c722f99391", result.sha256());
  }

  @Test
  public void readHash_ofLibrary_afterUpdateEntity() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Collection<Entity> entities = ImmutableList.of(new Library(10000001));
    injector.getInstance(PhaseEventDAO.class).update(Access.internal(), BigInteger.valueOf(1404),
      new PhaseEvent()
        .setDuration(0.21)
        .setInflection("ding")
        .setPosition(7.23)
        .setTonality(0.1)
        .setVelocity(0.9)
        .setVoiceId(BigInteger.valueOf(1201))
        .setPhaseId(BigInteger.valueOf(901))
        .setNote("D4"));

    DigestHash result = digestFactory.hashOf(evaluationFactory.evaluate(access, entities));

    assertNotNull(result);
    PhaseEvent updatedEvent = injector.getInstance(PhaseEventDAO.class).readOne(Access.internal(), BigInteger.valueOf(1404));
    assertNotNull(updatedEvent);
    assertEquals(String.format("Audio-401=1407871023,Audio-402=1407871023,AudioChord-501=1407871023,AudioChord-502=1407871023,AudioChord-503=1407871023,AudioChord-504=1407871023,AudioChord-505=1407871023,AudioChord-506=1407871023,AudioEvent-601=1407871023,AudioEvent-602=1407871023,AudioEvent-603=1407871023,AudioEvent-604=1407871023,Instrument-201=1407871023,Instrument-202=1407871023,InstrumentMeme-301=1407871023,InstrumentMeme-302=1407871023,InstrumentMeme-303=1407871023,Library-10000001=1407871023,Pattern-701=1407871023,Pattern-702=1407871023,Pattern-703=1407871023,PatternMeme-801=1407871023,PatternMeme-802=1407871023,PatternMeme-803=1407871023,Phase-901=1407871023,Phase-902=1407871023,PhaseChord-1001=1407871023,PhaseChord-1002=1407871023,PhaseChord-1003=1407871023,PhaseChord-1004=1407871023,PhaseChord-1005=1407871023,PhaseChord-1006=1407871023,PhaseEvent-1401=1407871023,PhaseEvent-1402=1407871023,PhaseEvent-1403=1407871023,PhaseEvent-1404=%s,PhaseMeme-1101=1407871023,PhaseMeme-1102=1407871023,PhaseMeme-1103=1407871023,Voice-1201=1407871023,Voice-1202=1407871023", updatedEvent.getUpdatedAt().toInstant().getEpochSecond()), result.toString());
    JSONObject resultJson = result.toJSONObject();
    assertEquals(41, resultJson.length());
  }

  @Test
  public void readHash_ofLibrary_afterDestroyEntity() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Collection<Entity> entities = ImmutableList.of(new Library(10000001));
    injector.getInstance(PhaseEventDAO.class).destroy(Access.internal(), BigInteger.valueOf(1404));

    DigestHash result = digestFactory.hashOf(evaluationFactory.evaluate(access, entities));

    assertNotNull(result);
    assertEquals("Audio-401=1407871023,Audio-402=1407871023,AudioChord-501=1407871023,AudioChord-502=1407871023,AudioChord-503=1407871023,AudioChord-504=1407871023,AudioChord-505=1407871023,AudioChord-506=1407871023,AudioEvent-601=1407871023,AudioEvent-602=1407871023,AudioEvent-603=1407871023,AudioEvent-604=1407871023,Instrument-201=1407871023,Instrument-202=1407871023,InstrumentMeme-301=1407871023,InstrumentMeme-302=1407871023,InstrumentMeme-303=1407871023,Library-10000001=1407871023,Pattern-701=1407871023,Pattern-702=1407871023,Pattern-703=1407871023,PatternMeme-801=1407871023,PatternMeme-802=1407871023,PatternMeme-803=1407871023,Phase-901=1407871023,Phase-902=1407871023,PhaseChord-1001=1407871023,PhaseChord-1002=1407871023,PhaseChord-1003=1407871023,PhaseChord-1004=1407871023,PhaseChord-1005=1407871023,PhaseChord-1006=1407871023,PhaseEvent-1401=1407871023,PhaseEvent-1402=1407871023,PhaseEvent-1403=1407871023,PhaseMeme-1101=1407871023,PhaseMeme-1102=1407871023,PhaseMeme-1103=1407871023,Voice-1201=1407871023,Voice-1202=1407871023", result.toString());
    JSONObject resultJson = result.toJSONObject();
    assertEquals(40, resultJson.length());
    assertEquals("8779ddab2a59d459088939ef0503d522e37f4482b591febbc303f9d4b48289c3", result.sha256());
  }

}
