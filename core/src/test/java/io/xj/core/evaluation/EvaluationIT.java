// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.evaluation;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.evaluation.digest_chords.DigestChords;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.Entity;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.library.Library;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.JSON;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class EvaluationIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private EvaluationFactory evaluation;
  private DigestFactory digest;

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

    // Instantiate the test evaluation and digest
    evaluation = injector.getInstance(EvaluationFactory.class);
    digest = injector.getInstance(DigestFactory.class);
  }

  @After
  public void tearDown() throws Exception {
    evaluation = null;
  }

  @Test
  public void digestChords_ofLibrary() throws  Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Collection<Entity> entities = ImmutableList.of(new Library(10000001));

    DigestChords digestChords = digest.chordsOf(evaluation.of(access, entities));

    JSONObject result = digestChords.toJSONObject();
    assertNotNull(result);
    JSONArray resultSequences = result.getJSONArray("chordSequencesByDescriptor");
    assertNotNull(resultSequences);
    assertEquals(24, resultSequences.length());
  }

}
