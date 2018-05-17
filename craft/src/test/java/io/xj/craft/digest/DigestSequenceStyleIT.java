// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.library.Library;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.craft.CraftModule;
import io.xj.craft.digest.sequence_style.DigestSequenceStyle;
import io.xj.craft.ingest.IngestFactory;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestSequenceStyleIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private IngestFactory ingestFactory;
  private DigestFactory digestFactory;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

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
    IntegrationTestEntity.insertSequence(701, 101, 10000001, SequenceType.Rhythm, SequenceState.Published, "leaves", 0.342, "C#", 120.4, at);
    IntegrationTestEntity.insertSequence(702, 101, 10000001, SequenceType.Main, SequenceState.Published, "coconuts", 0.25, "F#", 110.3, at);
    IntegrationTestEntity.insertSequence(703, 101, 10000001, SequenceType.Main, SequenceState.Published, "bananas", 0.27, "Gb", 100.6, at);
    IntegrationTestEntity.insertSequenceMeme(801, 701, "Ants", at);
    IntegrationTestEntity.insertSequenceMeme(802, 701, "Mold", at);
    IntegrationTestEntity.insertSequenceMeme(803, 703, "Peel", at);
    IntegrationTestEntity.insertPattern(901, 701, PatternType.Main, PatternState.Published, 0, 16, "growth", 0.342, "C#", 120.4, at);
    IntegrationTestEntity.insertPattern(902, 701, PatternType.Main, PatternState.Published, 1, 16, "decay", 0.25, "F#", 110.3, at);
    IntegrationTestEntity.insertPattern(1905, 702, PatternType.Main, PatternState.Published, 0, 32, "bang", 5, "D", 121, at);
    IntegrationTestEntity.insertPattern(2530, 703, PatternType.Main, PatternState.Published, 0, 16, "one banana", 0.3, "G", 110, at);
    IntegrationTestEntity.insertPattern(2531, 703, PatternType.Main, PatternState.Published, 0, 32, "two banana", 0.4, "G#", 115, at);
    IntegrationTestEntity.insertPattern(2532, 703, PatternType.Main, PatternState.Published, 0, 16, "four banana", 0.5, "A", 120, at);
    IntegrationTestEntity.insertPatternChord(1001, 902, 0, "G minor", at);
    IntegrationTestEntity.insertPatternChord(1002, 902, 4, "C major", at);
    IntegrationTestEntity.insertPatternChord(1003, 902, 8, "F7", at);
    IntegrationTestEntity.insertPatternChord(1004, 902, 12, "G7", at);
    IntegrationTestEntity.insertPatternChord(1005, 902, 16, "F minor", at);
    IntegrationTestEntity.insertPatternChord(1006, 902, 20, "Bb major", at);
    IntegrationTestEntity.insertPatternMeme(1101, 901, "Gravel", at);
    IntegrationTestEntity.insertPatternMeme(1102, 901, "Fuzz", at);
    IntegrationTestEntity.insertPatternMeme(1103, 902, "Peel", at);
    IntegrationTestEntity.insertVoice(1201, 701, InstrumentType.Percussive, "Drums", at);
    IntegrationTestEntity.insertVoice(1202, 702, InstrumentType.Harmonic, "Bass", at);
    IntegrationTestEntity.insertPatternEvent(1401, 901, 1201, 0, 1, "BOOM", "C", 0.8, 1.0, at);
    IntegrationTestEntity.insertPatternEvent(1402, 901, 1201, 1, 1, "SMACK", "G", 0.1, 0.8, at);
    IntegrationTestEntity.insertPatternEvent(1403, 901, 1201, 2.5, 1, "BOOM", "C", 0.8, 0.6, at);
    IntegrationTestEntity.insertPatternEvent(1404, 901, 1201, 3, 1, "SMACK", "G", 0.1, 0.9, at);
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
    IntegrationTestEntity.insertSequence(751, 101, 10000002, SequenceType.Rhythm, SequenceState.Published, "Garbage Sequence A", 0.342, "C#", 120.4, at);
    IntegrationTestEntity.insertSequence(752, 101, 10000002, SequenceType.Detail, SequenceState.Published, "Garbage Sequence B", 0.25, "F#", 110.3, at);
    IntegrationTestEntity.insertSequence(753, 101, 10000002, SequenceType.Main, SequenceState.Published, "Garbage Sequence C", 0.27, "Gb", 100.6, at);
    IntegrationTestEntity.insertSequenceMeme(851, 751, "Garbage Sequence Meme A", at);
    IntegrationTestEntity.insertSequenceMeme(852, 751, "Garbage Sequence Meme B", at);
    IntegrationTestEntity.insertSequenceMeme(853, 753, "Garbage Sequence Meme C", at);
    IntegrationTestEntity.insertPattern(951, 751, PatternType.Main, PatternState.Published, 0, 16, "Garbage Pattern A", 0.342, "C#", 120.4, at);
    IntegrationTestEntity.insertPattern(952, 751, PatternType.Main, PatternState.Published, 1, 16, "Garbage Pattern A", 0.25, "F#", 110.3, at);
    IntegrationTestEntity.insertPatternChord(1051, 952, 0, "G minor garbage", at);
    IntegrationTestEntity.insertPatternChord(1052, 952, 4, "C major garbage", at);
    IntegrationTestEntity.insertPatternChord(1053, 952, 8, "F7 garbage", at);
    IntegrationTestEntity.insertPatternChord(1054, 952, 12, "G7 garbage", at);
    IntegrationTestEntity.insertPatternChord(1055, 952, 16, "F minor garbage", at);
    IntegrationTestEntity.insertPatternChord(1056, 952, 20, "Bb major garbage", at);
    IntegrationTestEntity.insertPatternMeme(1151, 951, "Garbage Pattern Meme A", at);
    IntegrationTestEntity.insertPatternMeme(1152, 951, "Garbage Pattern Meme B", at);
    IntegrationTestEntity.insertPatternMeme(1153, 952, "Garbage Pattern Meme C", at);
    IntegrationTestEntity.insertVoice(1251, 751, InstrumentType.Percussive, "Garbage Voice A", at);
    IntegrationTestEntity.insertVoice(1252, 752, InstrumentType.Harmonic, "Garbage Voice B", at);
    IntegrationTestEntity.insertPatternEvent(1451, 951, 1251, 0, 1, "GARBAGE", "C", 0.8, 1.0, at);
    IntegrationTestEntity.insertPatternEvent(1452, 951, 1251, 1, 1, "GARBAGE", "G", 0.1, 0.8, at);
    IntegrationTestEntity.insertPatternEvent(1453, 951, 1251, 2.5, 1, "GARBAGE", "C", 0.8, 0.6, at);
    IntegrationTestEntity.insertPatternEvent(1454, 951, 1251, 3, 1, "GARBAGE", "G", 0.1, 0.9, at);

    // Instantiate the test ingest and digestFactory
    ingestFactory = injector.getInstance(IngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @After
  public void tearDown() throws Exception {
    ingestFactory = null;
  }

  @Test
  public void digest() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    DigestSequenceStyle result = digestFactory.sequenceStyle(ingestFactory.evaluate(access, ImmutableList.of(new Library(10000001))));

    assertNotNull(result);
    assertEquals(1.0, result.getMainPatternsPerSequenceStats().min(), 0.1);
    assertEquals(3.0, result.getMainPatternsPerSequenceStats().max(), 0.1);
    assertEquals(2.0, result.getMainPatternsPerSequenceStats().mean(), 0.1);
    assertEquals(2.0, result.getMainPatternsPerSequenceStats().count(), 0.1);
    assertEquals(1, result.getMainPatternsPerSequenceHistogram().count(1));
    assertEquals(1, result.getMainPatternsPerSequenceHistogram().count(3));
    assertEquals(16.0, result.getMainPatternTotalStats().min(), 0.1);
    assertEquals(32.0, result.getMainPatternTotalStats().max(), 0.1);
    assertEquals(24.0, result.getMainPatternTotalStats().mean(), 0.1);
    assertEquals(4.0, result.getMainPatternTotalStats().count(), 0.1);
    assertEquals(2, result.getMainPatternTotalHistogram().count(16));
    assertEquals(2, result.getMainPatternTotalHistogram().count(32));
  }

  @Test
  public void toJSONObject() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONObject result = digestFactory.sequenceStyle(ingestFactory.evaluate(access, ImmutableList.of(new Library(10000001)))).toJSONObject();

    assertNotNull(result.getJSONObject(Digest.KEY_SEQUENCE_STYLE).getJSONObject(Digest.KEY_MAIN_PATTERNS_PER_SEQUENCE).get(Digest.KEY_STAT_COUNT));
    assertNotNull(result.getJSONObject(Digest.KEY_SEQUENCE_STYLE).getJSONObject(Digest.KEY_MAIN_PATTERNS_PER_SEQUENCE).get(Digest.KEY_STAT_MAX));
    assertNotNull(result.getJSONObject(Digest.KEY_SEQUENCE_STYLE).getJSONObject(Digest.KEY_MAIN_PATTERNS_PER_SEQUENCE).get(Digest.KEY_STAT_MIN));
    assertNotNull(result.getJSONObject(Digest.KEY_SEQUENCE_STYLE).getJSONObject(Digest.KEY_MAIN_PATTERNS_PER_SEQUENCE).get(Digest.KEY_STAT_MEAN));
    assertEquals(2, result.getJSONObject(Digest.KEY_SEQUENCE_STYLE).getJSONObject(Digest.KEY_MAIN_PATTERNS_PER_SEQUENCE).getJSONArray(Digest.KEY_HISTOGRAM).length());
    assertNotNull(result.getJSONObject(Digest.KEY_SEQUENCE_STYLE).getJSONObject(Digest.KEY_MAIN_PATTERN_TOTAL).get(Digest.KEY_STAT_COUNT));
    assertNotNull(result.getJSONObject(Digest.KEY_SEQUENCE_STYLE).getJSONObject(Digest.KEY_MAIN_PATTERN_TOTAL).get(Digest.KEY_STAT_MAX));
    assertNotNull(result.getJSONObject(Digest.KEY_SEQUENCE_STYLE).getJSONObject(Digest.KEY_MAIN_PATTERN_TOTAL).get(Digest.KEY_STAT_MIN));
    assertNotNull(result.getJSONObject(Digest.KEY_SEQUENCE_STYLE).getJSONObject(Digest.KEY_MAIN_PATTERN_TOTAL).get(Digest.KEY_STAT_MEAN));
    assertEquals(2, result.getJSONObject(Digest.KEY_SEQUENCE_STYLE).getJSONObject(Digest.KEY_MAIN_PATTERN_TOTAL).getJSONArray(Digest.KEY_HISTOGRAM).length());
  }

}
