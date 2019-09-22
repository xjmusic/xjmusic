//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.persistence.sql.migration.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentFactory;
import io.xj.core.model.instrument.sub.Audio;
import io.xj.core.model.instrument.sub.AudioChord;
import io.xj.core.model.instrument.sub.AudioEvent;
import io.xj.core.model.instrument.sub.InstrumentMeme;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramFactory;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.Pattern;
import io.xj.core.model.program.sub.Event;
import io.xj.core.model.program.sub.ProgramMeme;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.program.sub.SequenceBindingMeme;
import io.xj.core.model.program.sub.SequenceChord;
import io.xj.core.model.program.sub.Voice;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.migration.LegacyMigration;
import io.xj.core.tables.records.InstrumentRecord;
import io.xj.core.tables.records.SequenceRecord;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.INSTRUMENT_MEME;
import static io.xj.core.tables.Audio.AUDIO;
import static io.xj.core.tables.AudioChord.AUDIO_CHORD;
import static io.xj.core.tables.AudioEvent.AUDIO_EVENT;
import static io.xj.core.tables.Instrument.INSTRUMENT;
import static io.xj.core.tables.Pattern.PATTERN;
import static io.xj.core.tables.PatternChord.PATTERN_CHORD;
import static io.xj.core.tables.PatternEvent.PATTERN_EVENT;
import static io.xj.core.tables.Sequence.SEQUENCE;
import static io.xj.core.tables.SequenceMeme.SEQUENCE_MEME;
import static io.xj.core.tables.SequencePattern.SEQUENCE_PATTERN;
import static io.xj.core.tables.SequencePatternMeme.SEQUENCE_PATTERN_MEME;
import static io.xj.core.tables.Voice.VOICE;
import static io.xj.core.util.Value.eitherOr;

/**
 [#166708597] Instrument model handles all of its own entities
 [#166690830] Program model handles all of its own entities
 */
public class LegacyMigrationImpl implements LegacyMigration {
  final Logger log = LoggerFactory.getLogger(LegacyMigrationImpl.class);
  private final InstrumentDAO instrumentDAO;
  private final ProgramDAO programDAO;
  private final InstrumentFactory instrumentFactory;
  private final ProgramFactory programFactory;
  private final Access access = Access.internal();
  private final DSLContext db;

  @Inject
  LegacyMigrationImpl(
    InstrumentDAO instrumentDAO,
    ProgramDAO programDAO,
    InstrumentFactory instrumentFactory, ProgramFactory programFactory, SQLDatabaseProvider sqlDatabaseProvider
  ) throws CoreException {
    this.instrumentDAO = instrumentDAO;
    this.programDAO = programDAO;
    this.instrumentFactory = instrumentFactory;
    this.programFactory = programFactory;
    db = sqlDatabaseProvider.getConnection().getContext();
  }

  /**
   State defaults to "Published" if null or empty

   @param state original
   @return state or "Published"
   */
  private static String safeState(String state) {
    if (Objects.nonNull(state) && !state.trim().isEmpty()) return state.trim();
    return "Published";
  }

  @Override
  public void migrate() throws CoreException {
    // Instruments updated with migrated contents
    for (InstrumentRecord instrumentRecord : db.selectFrom(INSTRUMENT).fetch())
      if (hasNoContent(instrumentRecord.getContent()))
        migrate(instrumentRecord);

    // Sequences migrated to Programs
    for (SequenceRecord sequenceRecord : db.selectFrom(SEQUENCE).fetch())
      migrate(sequenceRecord);

    deleteFromLegacyTables();
  }

  /**
   True if the provided object has no content

   @param obj to assert emptiness of
   @return true if has no content
   */
  private boolean hasNoContent(Object obj) {
    if (Objects.isNull(obj)) return true;
    String str = String.valueOf(obj);
    return
      Objects.equals("null", str) ||
        Objects.equals("{}", str) ||
        String.valueOf(str).isEmpty() ||
        String.valueOf(str).isBlank();
  }

  /**
   Migrate a legacy Instrument into an updated Instrument

   @param instrumentRecord to migrate
   */
  private void migrate(InstrumentRecord instrumentRecord) throws CoreException {
    Instrument instrument = instrumentFactory.newInstrument(instrumentRecord.getId().toBigInteger());

    instrument.setName(instrumentRecord.getName());
    instrument.setUserId(instrumentRecord.getUserId().toBigInteger());
    instrument.setLibraryId(instrumentRecord.getLibraryId().toBigInteger());
    instrument.setType(instrumentRecord.getType());
    instrument.setState(safeState(instrumentRecord.getState()));

    addContent(instrument);

    addMemes(instrument);

    instrumentDAO.update(access, instrument.getId(), instrument);
    log.info("Migrated legacy instrumentId={} to instrumentId={}", instrumentRecord.getId(), instrument.getId());
  }

  /**
   Migrate a legacy Sequence into a new Program

   @param sequenceRecord to migrate
   */
  private void migrate(SequenceRecord sequenceRecord) throws CoreException {
    Program program = programFactory.newProgram(sequenceRecord.getId().toBigInteger());

    program.setKey(sequenceRecord.getKey());
    program.setUserId(sequenceRecord.getUserId().toBigInteger());
    program.setLibraryId(sequenceRecord.getLibraryId().toBigInteger());
    program.setType(sequenceRecord.getType());
    program.setState(safeState(sequenceRecord.getState()));
    program.setName(sequenceRecord.getName());
    program.setTempo(sequenceRecord.getTempo());

    if (ProgramType.Rhythm == program.getType())
      addRhythmContent(program);

    if (ImmutableList.of(ProgramType.Main, ProgramType.Macro).contains(program.getType()))
      addMacroMainContent(program);

    addMemes(program);

    programDAO.create(access, program);
    log.info("Migrated legacy sequenceId={} to programId={}", sequenceRecord.getId(), program.getId());
  }

  /**
   Add Memes to Instrument

   @param instrument to which memes will be added
   */
  private void addMemes(Instrument instrument) {
    db.selectFrom(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(ULong.valueOf(instrument.getId())))
      .fetch().forEach(instrumentMemeRecord -> instrument.add(new InstrumentMeme()
      .setName(instrumentMemeRecord.getName())));
  }

  /**
   Add Memes to Program

   @param program to which memes will be added
   */
  private void addMemes(Program program) {
    db.selectFrom(SEQUENCE_MEME)
      .where(SEQUENCE_MEME.SEQUENCE_ID.eq(ULong.valueOf(program.getId())))
      .fetch().forEach(sequenceMemeRecord -> program.add(new ProgramMeme()
      .setName(sequenceMemeRecord.getName())));
  }

  /**
   Add Content to Instrument

   @param instrument to which content will be added
   */
  private void addContent(Instrument instrument) {
    db.selectFrom(AUDIO)
      .where(AUDIO.INSTRUMENT_ID.eq(ULong.valueOf(instrument.getId())))
      .fetch().forEach(audioRecord -> {
      Audio audio = instrument.add(new Audio()
        .setDensity(0.6)
        .setLength(audioRecord.getLength())
        .setStart(audioRecord.getStart())
        .setWaveformKey(audioRecord.getWaveformKey())
        .setName(audioRecord.getName())
        .setPitch(audioRecord.getPitch())
        .setTempo(audioRecord.getTempo()));

      db.selectFrom(AUDIO_CHORD)
        .where(AUDIO_CHORD.AUDIO_ID.eq(audioRecord.getId()))
        .fetch().forEach(pcRecord -> {
        instrument.add(new AudioChord()
          .setAudio(audio)
          .setPosition(pcRecord.getPosition())
          .setName(pcRecord.getName()));
      });

      db.selectFrom(AUDIO_EVENT)
        .where(AUDIO_EVENT.AUDIO_ID.eq(audioRecord.getId()))
        .fetch().forEach(pcRecord -> {
        instrument.add(new AudioEvent()
          .setAudio(audio)
          .setPosition(pcRecord.getPosition())
          .setName(pcRecord.getInflection())
          .setDuration(pcRecord.getDuration())
          .setNote(pcRecord.getNote())
          .setVelocity(pcRecord.getVelocity()));
      });
    });
  }

  /**
   Add content to Rhythm program

   @param program to which content will be added
   */
  private void addRhythmContent(Program program) throws CoreException {
    Sequence sequence = program.add(new Sequence()
      .setName("Beat")
      .setDensity(program.getDensity())
      .setKey(program.getKey())
      .setTempo(program.getTempo())
      .setTotal(16));
    program.add(new SequenceBinding()
      .setSequence(sequence)
      .setOffset(0L));

    Map<BigInteger, Voice> voiceMap = Maps.newHashMap();
    db.selectFrom(VOICE)
      .where(VOICE.SEQUENCE_ID.eq(ULong.valueOf(program.getId())))
      .fetch().forEach(voiceRecord -> {
      Voice voice = program.add(new Voice()
        .setName(voiceRecord.getDescription())
        .setType(voiceRecord.getType()));
      voiceMap.put(voiceRecord.getId().toBigInteger(), voice);

    });

    db.selectFrom(PATTERN)
      .where(PATTERN.SEQUENCE_ID.eq(ULong.valueOf(program.getId())))
      .fetch().forEach(patternRecord -> voiceMap.forEach((voiceId, voice) -> {
      Pattern pattern = program.add(new Pattern()
        .setSequence(sequence)
        .setVoice(voice)
        .setSequence(sequence)
        .setType(patternRecord.getType())
        .setName(patternRecord.getName())
        .setTotal(patternRecord.getTotal().intValue()));

      db.selectFrom(PATTERN_EVENT)
        .where(PATTERN_EVENT.PATTERN_ID.eq(patternRecord.getId()))
        .and(PATTERN_EVENT.VOICE_ID.eq(ULong.valueOf(voiceId)))
        .fetch().forEach(patternEventRecord -> {
        program.add(new Event()
          .setName(patternEventRecord.getInflection())
          .setPattern(pattern)
          .setPosition(patternEventRecord.getPosition())
          .setDuration(patternEventRecord.getDuration())
          .setNote(patternEventRecord.getNote())
          .setVelocity(patternEventRecord.getVelocity()));
      });
    }));
  }

  /**
   Add content to Macro or Main program program

   @param program to which content will be added
   */
  private void addMacroMainContent(Program program) {
    Map<BigInteger, Sequence> sequenceMap = Maps.newHashMap();
    db.selectFrom(PATTERN)
      .where(PATTERN.SEQUENCE_ID.eq(ULong.valueOf(program.getId())))
      .fetch().forEach(patternRecord -> {
      Sequence sequence = program.add(new Sequence()
        .setDensity(eitherOr(patternRecord.getDensity(), 0.6))
        .setKey(eitherOr(patternRecord.getKey(), program.getKey()))
        .setName(patternRecord.getName())
        .setTempo(eitherOr(patternRecord.getTempo(), program.getTempo()))
        .setTotal(patternRecord.getTotal().intValue()));
      sequenceMap.put(patternRecord.getId().toBigInteger(), sequence);
      db.selectFrom(PATTERN_CHORD)
        .where(PATTERN_CHORD.PATTERN_ID.eq(patternRecord.getId()))
        .fetch().forEach(pcRecord -> {
        program.add(new SequenceChord()
          .setSequence(sequence)
          .setPosition(pcRecord.getPosition())
          .setName(pcRecord.getName()));
      });
    });

    db.selectFrom(SEQUENCE_PATTERN)
      .where(SEQUENCE_PATTERN.SEQUENCE_ID.eq(ULong.valueOf(program.getId())))
      .fetch().forEach(spRecord -> {
      SequenceBinding sequenceBinding = program.add(new SequenceBinding()
        .setSequence(sequenceMap.get(spRecord.getPatternId().toBigInteger()))
        .setOffset(spRecord.getOffset().longValue()));

      db.selectFrom(SEQUENCE_PATTERN_MEME)
        .where(SEQUENCE_PATTERN_MEME.SEQUENCE_PATTERN_ID.eq(spRecord.getId()))
        .fetch().forEach(spmRecord -> {
        program.add(new SequenceBindingMeme()
          .setSequenceBinding(sequenceBinding)
          .setName(spmRecord.getName()));
      });
    });
  }

  /**
   Delete all rows from legacy tables
   */
  private void deleteFromLegacyTables() {
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
}
