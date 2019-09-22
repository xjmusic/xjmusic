//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.app;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreIT;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentState;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.core.persistence.sql.migration.LegacyMigration;
import io.xj.core.tables.records.AudioChordRecord;
import io.xj.core.tables.records.AudioEventRecord;
import io.xj.core.tables.records.AudioRecord;
import io.xj.core.tables.records.InstrumentMemeRecord;
import io.xj.core.tables.records.InstrumentRecord;
import io.xj.core.tables.records.PatternChordRecord;
import io.xj.core.tables.records.PatternEventRecord;
import io.xj.core.tables.records.PatternRecord;
import io.xj.core.tables.records.SequenceMemeRecord;
import io.xj.core.tables.records.SequencePatternMemeRecord;
import io.xj.core.tables.records.SequencePatternRecord;
import io.xj.core.tables.records.SequenceRecord;
import io.xj.core.tables.records.VoiceRecord;
import org.jooq.Table;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;

import static io.xj.core.Tables.AUDIO;
import static io.xj.core.Tables.AUDIO_CHORD;
import static io.xj.core.Tables.AUDIO_EVENT;
import static io.xj.core.Tables.INSTRUMENT;
import static io.xj.core.Tables.INSTRUMENT_MEME;
import static io.xj.core.Tables.PATTERN;
import static io.xj.core.Tables.PATTERN_CHORD;
import static io.xj.core.Tables.PATTERN_EVENT;
import static io.xj.core.Tables.SEQUENCE;
import static io.xj.core.Tables.SEQUENCE_MEME;
import static io.xj.core.Tables.SEQUENCE_PATTERN;
import static io.xj.core.Tables.SEQUENCE_PATTERN_MEME;
import static io.xj.core.Tables.VOICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 [#166708597] Instrument model handles all of its own entities
 [#166690830] Program model handles all of its own entities
 */
public class LegacyLibraryMigrationIT extends CoreIT {
  private InstrumentDAO instrumentDAO;
  private ProgramDAO programDAO;

  @Before
  public void setUp() throws Exception {
    instrumentDAO = injector.getInstance(InstrumentDAO.class);
    programDAO = injector.getInstance(ProgramDAO.class);

    resetLegacy();
    reset();
    insertLegacyFixtures();

    // avoid common ports
    System.setProperty("app.port", "9043");
  }

  @After
  public void tearDown() {
    resetLegacy();
  }

  /**
   After the migration, legacy entities ought to be migrated to new entities.
   */
  @Test
  public void migratesLegacyLibrary() throws Exception {
    LegacyMigration legacyMigration = injector.getInstance(LegacyMigration.class);

    legacyMigration.migrate();

    assertFalse(hasLegacyDataRemaining());
    //
    Instrument instrument201 = instrumentDAO.readOne(internal, BigInteger.valueOf(201));
    assertEquals(BigInteger.valueOf(3), instrument201.getUserId());
    assertEquals(2, instrument201.getMemes().size());
    assertEquals(2, instrument201.getAudios().size());
    assertEquals(4, instrument201.getAudioEvents().size());
    assertEquals(6, instrument201.getAudioChords().size());
    //
    Program program4 = programDAO.readOne(internal, BigInteger.valueOf(4));
    assertEquals(BigInteger.valueOf(3), program4.getUserId());
    assertEquals(1, program4.getMemes().size());
    assertEquals(0, program4.getPatterns().size());
    assertEquals(0, program4.getPatternEvents().size());
    assertEquals(3, program4.getSequences().size());
    assertEquals(4, program4.getSequenceBindingMemes().size());
    assertEquals(3, program4.getSequenceBindings().size());
    assertEquals(0, program4.getSequenceChords().size());
    assertEquals(0, program4.getVoices().size());
    //
    Program program5 = programDAO.readOne(internal, BigInteger.valueOf(5));
    assertEquals(BigInteger.valueOf(3), program5.getUserId());
    assertEquals(1, program5.getMemes().size());
    assertEquals(0, program5.getPatterns().size());
    assertEquals(0, program5.getPatternEvents().size());
    assertEquals(2, program5.getSequences().size());
    assertEquals(2, program5.getSequenceBindingMemes().size());
    assertEquals(2, program5.getSequenceBindings().size());
    assertEquals(4, program5.getSequenceChords().size());
    assertEquals(0, program5.getVoices().size());
    //
    Program program6 = programDAO.readOne(internal, BigInteger.valueOf(6));
    assertEquals(BigInteger.valueOf(3), program6.getUserId());
    assertEquals(0, program6.getMemes().size());
    assertEquals(0, program6.getPatterns().size());
    assertEquals(0, program6.getPatternEvents().size());
    assertEquals(1, program6.getSequences().size());
    assertEquals(0, program6.getSequenceBindingMemes().size());
    assertEquals(1, program6.getSequenceBindings().size());
    assertEquals(0, program6.getSequenceChords().size());
    assertEquals(0, program6.getVoices().size());
    //
    Program program7 = programDAO.readOne(internal, BigInteger.valueOf(7));
    assertEquals(BigInteger.valueOf(3), program7.getUserId());
    assertEquals(0, program7.getMemes().size());
    assertEquals(0, program7.getPatterns().size());
    assertEquals(0, program7.getPatternEvents().size());
    assertEquals(0, program7.getSequences().size());
    assertEquals(0, program7.getSequenceBindingMemes().size());
    assertEquals(0, program7.getSequenceBindings().size());
    assertEquals(0, program7.getSequenceChords().size());
    assertEquals(0, program7.getVoices().size());
    //
    Program program35 = programDAO.readOne(internal, BigInteger.valueOf(35));
    assertEquals(BigInteger.valueOf(3), program35.getUserId());
    assertEquals(1, program35.getMemes().size());
    assertEquals(2, program35.getPatterns().size());
    assertEquals(8, program35.getPatternEvents().size());
    assertEquals(1, program35.getSequences().size());
    assertEquals(0, program35.getSequenceBindingMemes().size());
    assertEquals(1, program35.getSequenceBindings().size());
    assertEquals(0, program35.getSequenceChords().size());
    assertEquals(1, program35.getVoices().size());
  }

  /**
   Does a specified Chain have at least N segments?

   @return true if has at least N segments
   */
  private boolean hasLegacyDataRemaining() {
    return hasRows(ImmutableList.of(
      AUDIO_CHORD,
      AUDIO_EVENT,
      AUDIO,
      INSTRUMENT_MEME,
      SEQUENCE_PATTERN_MEME,
      SEQUENCE_PATTERN,
      SEQUENCE_MEME,
      PATTERN_CHORD,
      PATTERN_EVENT,
      PATTERN,
      VOICE,
      SEQUENCE
    ));
  }

  /**
   Whether any of a collection of tables contain any rows of data

   @param tables to select count of rows
   @return true if any table contains any rows of data
   */
  private boolean hasRows(Collection<Table> tables) {
    for (Table table : tables) if (hasRows(table)) return true;
    return false;
  }

  /**
   Whether a table contains rows of data

   @param table to select count of rows
   @return true if table contains rows of data
   */
  private boolean hasRows(Table table) {
    return 0 < db.selectCount().from(table).fetchOne(0, int.class);
  }

  /**
   Insert legacy database fixtures, for migration:
   <p>
   + instrument (x2)
   + instrument_meme (x4; 2 per instrument)
   + audio (x4; 2 per instrument)
   + audio_chord (x2; 2 audios have 1 each)
   + audio_event (x2; 2 audios have 1 each)
   + sequence (x3; one macro, one main, one rhythm)
   + sequence_meme (x4; 2 per sequence)
   + voice (x2; rhythm sequence has 2)
   + pattern (x6; 2 per sequence)
   + pattern_chord (x4; main-sequence patterns have 2 each)
   + pattern_event (x4; rhythm-sequence patterns have 2 each)
   + sequence_pattern (x4; macro- and main-sequences have 2 each)
   + sequence_pattern_meme (x4; one per sequence-pattern binding)
   */
  private void insertLegacyFixtures() {
    // Account "bananas"
    insert(newAccount(1, "bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(newUser(2, "john", "john@email.com", "http://pictures.com/john.gif"));
    insert(newUserRole(2, UserRoleType.Admin));

    // Jenny has a "user" role and belongs to account "bananas"
    insert(newUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    insert(newUserRole(3, UserRoleType.User));
    insert(newAccountUser(1, 3));

    // Library "house"
    insert(newLibrary(2, 1, "house", now()));

    // "Tropical, Wild to Cozy" macro-sequence in house library
    insertSequence(4, 3, 2, ProgramType.Macro, ProgramState.Published, "Tropical, Wild to Cozy", 0.5, "C", 120, now());
    insertSequenceMeme(4, "Tropical", now());
    // " pattern offset 0
    insertPattern(3, 4, "Macro", "Published", 0, "Start Wild", 0.6, "C", 125, now());
    insertSequencePattern(340, 4, 3, 0, now());
    insertSequencePatternMeme(340, "Wild", now());
    // " pattern offset 1
    insertPattern(4, 4, "Macro", "Published", 0, "Intermediate", 0.4, "Bb minor", 115, now());
    insertSequencePattern(441, 4, 4, 1, now());
    insertSequencePatternMeme(441, "Cozy", now());
    insertSequencePatternMeme(441, "Wild", now());
    // " pattern offset 2
    insertPattern(5, 4, "Macro", "Published", 0, "Finish Cozy", 0.4, "Ab minor", 125, now());
    insertSequencePattern(542, 4, 5, 2, now());
    insertSequencePatternMeme(542, "Cozy", now());

    // Main sequence
    insertSequence(5, 3, 2, ProgramType.Main, ProgramState.Published, "Main Jam", 0.2, "C minor", 140, now());
    insertSequenceMeme(5, "Outlook", now());
    // # pattern offset 0
    insertPattern(15, 5, "Main", "Published", 16, "Intro", 0.5, "G major", 135.0, now());
    insertSequencePattern(1550, 5, 15, 0, now());
    insertSequencePatternMeme(1550, "Optimism", now());
    insertPatternChord(15, 0, "G major", now());
    insertPatternChord(15, 8, "Ab minor", now());
    // # pattern offset 1
    insertPattern(16, 5, "Main", "Published", 32, "Drop", 0.5, "G minor", 135.0, now());
    insertSequencePattern(1651, 5, 16, 1, now());
    insertSequencePatternMeme(1651, "Pessimism", now());
    insertPatternChord(16, 0, "C major", now());
    insertPatternChord(16, 8, "Bb minor", now());

    // A basic beat
    insertSequence(35, 3, 2, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", 0.2, "C", 121, now());
    insertSequenceMeme(35, "Basic", now());
    insertVoice(1, 35, InstrumentType.Percussive, "drums", now());

    // Voice "Drums" are onomatopoeic to "KICK" and "SNARE" 2x each
    insertPattern(315, 35, "Intro", "Published", 4, "Drop", 0.5, "C", 125.0, now());
    insertPatternEvent(315, 1, 0, 1, "CLOCK", "C2", 0.8, 1.0, now());
    insertPatternEvent(315, 1, 1, 1, "SNORT", "G5", 0.1, 0.8, now());
    insertPatternEvent(315, 1, 2.5, 1, "KICK", "C2", 0.8, 0.6, now());
    insertPatternEvent(315, 1, 3, 1, "SNARL", "G5", 0.1, 0.9, now());

    // this is an alternate pattern at the same offset
    insertPattern(317, 35, "Loop", "Published", 4, "Drop Alt", 0.5, "C", 125.0, now());
    insertPatternEvent(317, 1, 0, 1, "CLACK", "B5", 0.1, 0.9, now());
    insertPatternEvent(317, 1, 1, 1, "SNARL", "D2", 0.5, 1.0, now());
    insertPatternEvent(317, 1, 2.5, 1, "CLICK", "E4", 0.1, 0.7, now());
    insertPatternEvent(317, 1, 3, 1, "SNAP", "C3", 0.5, 0.5, now());

    // Detail Sequence
    insertSequence(6, 3, 2, ProgramType.Rhythm, ProgramState.Published, "Beat Jam", 0.6, "D#", 150, now());
    insertSequence(7, 3, 2, ProgramType.Detail, ProgramState.Published, "Detail Jam", 0.3, "Cb minor", 170, now());

    // Instruments
    insertInstrument(201, 3, 2, "808 Drums", InstrumentType.Percussive, InstrumentState.Published, now());
    insertInstrumentMeme(201, "Ants", now());
    insertInstrumentMeme(201, "Mold", now());
    insertAudio(401, 201, "Published", "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, now());
    insertAudio(402, 201, "Published", "Chords Cm to D", "instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440, now());
    insertAudioChord(402, 0, "E minor", now());
    insertAudioChord(402, 4, "A major", now());
    insertAudioChord(402, 8, "B minor", now());
    insertAudioChord(402, 12, "F# major", now());
    insertAudioChord(402, 16, "Ab7", now());
    insertAudioChord(402, 20, "Bb7", now());
    insertAudioEvent(401, 2.5, 1, "KICK", "Eb", 0.8, 1.0, now());
    insertAudioEvent(401, 3, 1, "SNARE", "Ab", 0.1, 0.8, now());
    insertAudioEvent(401, 0, 1, "KICK", "C", 0.8, 1.0, now());
    insertAudioEvent(401, 1, 1, "SNARE", "G", 0.1, 0.8, now());

    // Report
    log.info("Did insert legacy fixtures!");
  }

  /**
   Delete from legacy tables that are not included in the real reset
   */
  private void resetLegacy() {
    db.deleteFrom(AUDIO_CHORD).execute(); // before Audio
    db.deleteFrom(AUDIO_EVENT).execute(); // before Audio
    db.deleteFrom(AUDIO).execute(); // before Audio
    db.deleteFrom(INSTRUMENT_MEME).execute(); // before Instrument (modern parent)
    db.deleteFrom(SEQUENCE_PATTERN_MEME).execute(); // before SequencePattern
    db.deleteFrom(SEQUENCE_PATTERN).execute(); // before Sequence and Pattern
    db.deleteFrom(SEQUENCE_MEME).execute(); // before Sequence
    db.deleteFrom(PATTERN_CHORD).execute(); // before Pattern
    db.deleteFrom(PATTERN_EVENT).execute(); // before Pattern
    db.deleteFrom(PATTERN).execute(); // before Sequence
    db.deleteFrom(VOICE).execute(); // before Sequence
    db.deleteFrom(SEQUENCE).execute(); // before Program (modern parent)
  }

  /**
   insert a Instrument to the database@param id               of Instrument@param libraryId        of Instrument@param name      of Instrument@param userId

   @param type             of Instrument
   @param state            of Instrument
   @param createdUpdatedAt of Instrument
   */
  private void insertInstrument(long id, long userId, long libraryId, String name, InstrumentType type, InstrumentState state, Instant createdUpdatedAt) {
    InstrumentRecord record = db.newRecord(INSTRUMENT);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setLibraryId(ULong.valueOf(libraryId));
    record.setType(type.toString());
    record.setState(state.toString());
    record.setContent("{}");
    record.setName(name);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  /**
   insert a InstrumentMeme to the database

   @param instrumentId     of InstrumentMeme
   @param name             of InstrumentMeme
   @param createdUpdatedAt of InstrumentMeme
   */
  private void insertInstrumentMeme(long instrumentId, String name, Instant createdUpdatedAt) {
    InstrumentMemeRecord record = db.newRecord(INSTRUMENT_MEME);
    record.setId(ULong.valueOf(getNextUniqueId()));
    record.setInstrumentId(ULong.valueOf(instrumentId));
    record.setName(name);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  /**
   insert a Audio to the database

   @param id               of Audio
   @param instrumentId     of Audio
   @param state            of Audio
   @param name             of Audio
   @param waveformKey      of Audio
   @param start            of Audio
   @param length           of Audio
   @param tempo            of Audio
   @param pitch            of Audio
   @param createdUpdatedAt of Audio
   */
  private void insertAudio(long id, long instrumentId, String state, String name, String waveformKey, double start, double length, double tempo, double pitch, Instant createdUpdatedAt) {
    AudioRecord record = db.newRecord(AUDIO);
    record.setId(ULong.valueOf(id));
    record.setInstrumentId(ULong.valueOf(instrumentId));
    record.setName(name);
    record.setWaveformKey(waveformKey);
    record.setStart(start);
    record.setLength(length);
    record.setTempo(tempo);
    record.setPitch(pitch);
    record.setState(state);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  /**
   insert a AudioEvent to the database

   @param audioId          of AudioEvent
   @param position         of AudioEvent
   @param duration         of AudioEvent
   @param name             of AudioEvent
   @param note             of AudioEvent
   @param tonality         of AudioEvent
   @param velocity         of AudioEvent
   @param createdUpdatedAt of AudioEvent
   */
  private void insertAudioEvent(long audioId, double position, double duration, String name, String note, double tonality, double velocity, Instant createdUpdatedAt) {
    AudioEventRecord record = db.newRecord(AUDIO_EVENT);
    record.setId(ULong.valueOf(getNextUniqueId()));
    record.setAudioId(ULong.valueOf(audioId));
    record.setPosition(position);
    record.setDuration(duration);
    record.setInflection(name);
    record.setNote(note);
    record.setTonality(tonality);
    record.setVelocity(velocity);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  /**
   insert a AudioChord to the database

   @param audioId          of AudioChord
   @param position         of AudioChord
   @param name             of AudioChord
   @param createdUpdatedAt of AudioChord
   */
  private void insertAudioChord(long audioId, double position, String name, Instant createdUpdatedAt) {
    AudioChordRecord record = db.newRecord(AUDIO_CHORD);
    record.setId(ULong.valueOf(getNextUniqueId()));
    record.setAudioId(ULong.valueOf(audioId));
    record.setPosition(position);
    record.setName(name);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  /**
   insert a Sequence to the database

   @param id               of Sequence
   @param userId           of Sequence
   @param libraryId        of Sequence
   @param type             of Sequence
   @param state            of Sequence
   @param name             of Sequence
   @param density          of Sequence
   @param key              of Sequence
   @param tempo            of Sequence
   @param createdUpdatedAt of Sequence
   */
  private void insertSequence(long id, long userId, long libraryId, ProgramType type, ProgramState state, String name, double density, String key, double tempo, Instant createdUpdatedAt) {
    SequenceRecord record = db.newRecord(SEQUENCE);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setLibraryId(ULong.valueOf(libraryId));
    record.setType(type.toString());
    record.setName(name);
    record.setDensity(density);
    record.setKey(key);
    record.setTempo(tempo);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.setState(state.toString());
    record.store();
  }

  /**
   insert a SequenceMeme to the database

   @param sequenceId       of SequenceMeme
   @param name             of SequenceMeme
   @param createdUpdatedAt of SequenceMeme
   */
  private void insertSequenceMeme(long sequenceId, String name, Instant createdUpdatedAt) {
    SequenceMemeRecord record = db.newRecord(SEQUENCE_MEME);
    record.setId(ULong.valueOf(getNextUniqueId()));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setName(name);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  /**
   Insert a Voice to the database

   @param id               of Voice
   @param sequenceId       of Voice
   @param type             of Voice
   @param name             of Voice
   @param createdUpdatedAt of Voice
   */
  private void insertVoice(int id, int sequenceId, InstrumentType type, String name, Instant createdUpdatedAt) {
    VoiceRecord record = db.newRecord(VOICE);
    record.setId(ULong.valueOf(id));
    record.setType(type.toString());
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setDescription(name);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  /**
   insert a Pattern to the database

   @param id               of Pattern
   @param sequenceId       of Pattern
   @param type             of Pattern
   @param state            of Pattern
   @param total            of Pattern
   @param name             of Pattern
   @param density          of Pattern
   @param key              of Pattern
   @param tempo            of Pattern
   @param createdUpdatedAt of Pattern
   */
  private void insertPattern(long id, long sequenceId, String type, String state, long total, String name, double density, String key, double tempo, Instant createdUpdatedAt) {
    PatternRecord record = db.newRecord(PATTERN);
    record.setId(ULong.valueOf(id));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setTotal(UInteger.valueOf(total));
    record.setName(name);
    record.setDensity(density);
    record.setKey(key);
    record.setTempo(tempo);
    record.setType(type);
    record.setState(state);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  /**
   insert a PatternChord to the database

   @param patternId        of PatternChord
   @param position         of PatternChord
   @param name             of PatternChord
   @param createdUpdatedAt of PatternChord
   */
  private void insertPatternChord(long patternId, double position, String name, Instant createdUpdatedAt) {
    PatternChordRecord record = db.newRecord(PATTERN_CHORD);
    record.setId(ULong.valueOf(getNextUniqueId()));
    record.setPatternId(ULong.valueOf(patternId));
    record.setPosition(position);
    record.setName(name);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  /**
   insert a Event to the database

   @param patternId        of Event
   @param voiceId          of Event
   @param position         of Event
   @param duration         of Event
   @param name             of Event
   @param note             of Event
   @param tonality         of Event
   @param velocity         of Event
   @param createdUpdatedAt of Event
   */
  private void insertPatternEvent(long patternId, long voiceId, double position, double duration, String name, String note, double tonality, double velocity, Instant createdUpdatedAt) {
    PatternEventRecord record = db.newRecord(PATTERN_EVENT);
    record.setId(ULong.valueOf(getNextUniqueId()));
    record.setPatternId(ULong.valueOf(patternId));
    record.setVoiceId(ULong.valueOf(voiceId));
    record.setPosition(position);
    record.setDuration(duration);
    record.setInflection(name);
    record.setNote(note);
    record.setTonality(tonality);
    record.setVelocity(velocity);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }


  /**
   insert a SequencePattern to the database

   @param id               of SequencePattern
   @param sequenceId       of SequencePattern
   @param patternId        of SequencePattern
   @param offset           of SequencePattern
   @param createdUpdatedAt of SequencePattern
   */
  private void insertSequencePattern(long id, long sequenceId, long patternId, long offset, Instant createdUpdatedAt) {
    SequencePatternRecord record = db.newRecord(SEQUENCE_PATTERN);
    record.setId(ULong.valueOf(id));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setPatternId(ULong.valueOf(patternId));
    record.setOffset(ULong.valueOf(offset));
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  /**
   insert a SequencePatternMeme to the database

   @param sequencePatternId of SequencePatternMeme
   @param name              of SequencePatternMeme
   @param createdUpdatedAt  of SequencePatternMeme
   */
  private void insertSequencePatternMeme(long sequencePatternId, String name, Instant createdUpdatedAt) {
    SequencePatternMemeRecord record = db.newRecord(SEQUENCE_PATTERN_MEME);
    record.setId(ULong.valueOf(getNextUniqueId()));
    record.setSequencePatternId(ULong.valueOf(sequencePatternId));
    record.setName(name);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

}
