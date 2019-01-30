// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.integration;

import io.xj.core.exception.DatabaseException;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_auth.UserAuthType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.tables.records.AccountRecord;
import io.xj.core.tables.records.AccountUserRecord;
import io.xj.core.tables.records.ArrangementRecord;
import io.xj.core.tables.records.AudioChordRecord;
import io.xj.core.tables.records.AudioEventRecord;
import io.xj.core.tables.records.AudioRecord;
import io.xj.core.tables.records.ChainConfigRecord;
import io.xj.core.tables.records.ChainInstrumentRecord;
import io.xj.core.tables.records.ChainLibraryRecord;
import io.xj.core.tables.records.ChainRecord;
import io.xj.core.tables.records.ChainSequenceRecord;
import io.xj.core.tables.records.ChoiceRecord;
import io.xj.core.tables.records.InstrumentMemeRecord;
import io.xj.core.tables.records.InstrumentRecord;
import io.xj.core.tables.records.LibraryRecord;
import io.xj.core.tables.records.PatternChordRecord;
import io.xj.core.tables.records.PatternEventRecord;
import io.xj.core.tables.records.PatternRecord;
import io.xj.core.tables.records.PlatformMessageRecord;
import io.xj.core.tables.records.SegmentChordRecord;
import io.xj.core.tables.records.SegmentMemeRecord;
import io.xj.core.tables.records.SegmentMessageRecord;
import io.xj.core.tables.records.SegmentRecord;
import io.xj.core.tables.records.SequenceMemeRecord;
import io.xj.core.tables.records.SequencePatternMemeRecord;
import io.xj.core.tables.records.SequencePatternRecord;
import io.xj.core.tables.records.SequenceRecord;
import io.xj.core.tables.records.UserAccessTokenRecord;
import io.xj.core.tables.records.UserAuthRecord;
import io.xj.core.tables.records.UserRecord;
import io.xj.core.tables.records.UserRoleRecord;
import io.xj.core.tables.records.VoiceRecord;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

import static io.xj.core.Tables.ACCOUNT;
import static io.xj.core.Tables.ACCOUNT_USER;
import static io.xj.core.Tables.ARRANGEMENT;
import static io.xj.core.Tables.AUDIO;
import static io.xj.core.Tables.AUDIO_CHORD;
import static io.xj.core.Tables.AUDIO_EVENT;
import static io.xj.core.Tables.CHAIN;
import static io.xj.core.Tables.CHAIN_CONFIG;
import static io.xj.core.Tables.CHAIN_INSTRUMENT;
import static io.xj.core.Tables.CHAIN_LIBRARY;
import static io.xj.core.Tables.CHAIN_SEQUENCE;
import static io.xj.core.Tables.CHOICE;
import static io.xj.core.Tables.INSTRUMENT;
import static io.xj.core.Tables.INSTRUMENT_MEME;
import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.Tables.PATTERN;
import static io.xj.core.Tables.PATTERN_CHORD;
import static io.xj.core.Tables.PATTERN_EVENT;
import static io.xj.core.Tables.PLATFORM_MESSAGE;
import static io.xj.core.Tables.SEGMENT;
import static io.xj.core.Tables.SEGMENT_CHORD;
import static io.xj.core.Tables.SEGMENT_MEME;
import static io.xj.core.Tables.SEGMENT_MESSAGE;
import static io.xj.core.Tables.SEQUENCE;
import static io.xj.core.Tables.SEQUENCE_MEME;
import static io.xj.core.Tables.SEQUENCE_PATTERN;
import static io.xj.core.Tables.SEQUENCE_PATTERN_MEME;
import static io.xj.core.Tables.USER;
import static io.xj.core.Tables.USER_ACCESS_TOKEN;
import static io.xj.core.Tables.USER_AUTH;
import static io.xj.core.Tables.USER_ROLE;
import static io.xj.core.Tables.VOICE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public interface IntegrationTestEntity {
  Logger log = LoggerFactory.getLogger(IntegrationTestEntity.class);

  /**
   Reset the database before an integration test.
   */
  static void reset() throws DatabaseException {
    DSLContext db = IntegrationTestService.getDb();
    try {
      // Arrangement
      db.deleteFrom(ARRANGEMENT).execute(); // before Instrument, Voice & Choice

      // Audio
      db.deleteFrom(AUDIO_CHORD).execute(); // before Audio
      db.deleteFrom(AUDIO_EVENT).execute(); // before Audio
      db.deleteFrom(AUDIO).execute(); // before Instrument

      // Voice
      db.deleteFrom(PATTERN_EVENT).execute(); // before Voice
      db.deleteFrom(VOICE).execute(); // before Pattern

      // Choice
      db.deleteFrom(CHOICE).execute(); // before Segment & Sequence

      // Segment
      db.deleteFrom(SEGMENT_MESSAGE).execute(); // before Segment
      db.deleteFrom(SEGMENT_MEME).execute(); // before Segment
      db.deleteFrom(SEGMENT_CHORD).execute(); // before Segment
      db.deleteFrom(SEGMENT).execute(); // before Chain

      // Chain
      db.deleteFrom(CHAIN_SEQUENCE).execute(); // before Chain & Sequence
      db.deleteFrom(CHAIN_INSTRUMENT).execute(); // before Chain & Instrument
      db.deleteFrom(CHAIN_LIBRARY).execute(); // before Chain & Library
      db.deleteFrom(CHAIN_CONFIG).execute(); // before Chain
      db.deleteFrom(CHAIN).execute(); // before Account

      // Instrument
      db.deleteFrom(INSTRUMENT_MEME).execute(); // before Instrument
      db.deleteFrom(INSTRUMENT).execute(); // before Library & Credit

      // Pattern
      db.deleteFrom(SEQUENCE_PATTERN_MEME).execute(); // before Pattern
      db.deleteFrom(PATTERN_CHORD).execute(); // before Pattern
      db.deleteFrom(SEQUENCE_PATTERN).execute(); // before Sequence
      db.deleteFrom(PATTERN).execute(); // before Sequence

      // Sequence
      db.deleteFrom(SEQUENCE_MEME).execute(); // before Sequence
      db.deleteFrom(SEQUENCE).execute(); // before Library & Credit

      // Library
      db.deleteFrom(LIBRARY).execute(); // before Account

      // Account
      db.deleteFrom(ACCOUNT_USER).execute(); // before Account
      db.deleteFrom(ACCOUNT).execute(); //before User

      // Platform Messages
      db.deleteFrom(PLATFORM_MESSAGE).execute(); // before Segment

      // User Access Token
      db.deleteFrom(USER_ACCESS_TOKEN).execute(); // before User & User Auth

      // User
      db.deleteFrom(USER_AUTH).execute(); // before User
      db.deleteFrom(USER_ROLE).execute(); // before User
      db.deleteFrom(USER).execute();

      // Finally, all queues
      IntegrationTestService.flushRedis();

    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      throw new DatabaseException(e.getClass().getName(), e);
    }

    log.info("Did delete all records from integration database.");
  }

  static void insertUserAuth(Integer id, Integer userId, UserAuthType type, String externalAccessToken, String externalRefreshToken, String externalAccount) {
    UserAuthRecord record = IntegrationTestService.getDb().newRecord(USER_AUTH);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setType(type.toString());
    record.setExternalAccessToken(externalAccessToken);
    record.setExternalRefreshToken(externalRefreshToken);
    record.setExternalAccount(externalAccount);
    record.store();
  }

  static void insertUser(Integer id, String name, String email, String avatarUrl) {
    UserRecord record = IntegrationTestService.getDb().newRecord(USER);
    record.setId(ULong.valueOf(id));
    record.setName(name);
    record.setEmail(email);
    record.setAvatarUrl(avatarUrl);
    record.store();
  }

  static void insertUserRole(Integer id, Integer userId, UserRoleType type) {
    UserRoleRecord record = IntegrationTestService.getDb().newRecord(USER_ROLE);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setType(type.toString());
    record.store();
  }

  static void insertUserRole(Integer id, Integer userId, String legacyType) {
    UserRoleRecord record = IntegrationTestService.getDb().newRecord(USER_ROLE);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setType(legacyType);
    record.store();
  }

  static void insertAccountUser(Integer id, Integer accountId, Integer userId) {
    AccountUserRecord record = IntegrationTestService.getDb().newRecord(ACCOUNT_USER);
    record.setId(ULong.valueOf(id));
    record.setAccountId(ULong.valueOf(accountId));
    record.setUserId(ULong.valueOf(userId));
    record.store();
  }

  static void insertAccount(Integer id, String name) {
    AccountRecord record = IntegrationTestService.getDb().newRecord(ACCOUNT);
    record.setId(ULong.valueOf(id));
    record.setName(name);
    record.store();
  }

  static void insertUserAccessToken(int userId, int userAuthId, String accessToken) {
    UserAccessTokenRecord record = IntegrationTestService.getDb().newRecord(USER_ACCESS_TOKEN);
    record.setUserId(ULong.valueOf(userId));
    record.setUserAuthId(ULong.valueOf(userAuthId));
    record.setAccessToken(accessToken);
    record.store();
  }

  static void insertLibrary(int id, int accountId, String name) {
    insertLibrary(id, accountId, name, Timestamp.from(Instant.now()));
  }

  static void insertLibrary(int id, int accountId, String name, Timestamp createdUpdatedAt) {
    LibraryRecord record = IntegrationTestService.getDb().newRecord(LIBRARY);
    record.setId(ULong.valueOf(id));
    record.setAccountId(ULong.valueOf(accountId));
    record.setName(name);
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.store();
  }

  static Sequence insertSequence(int id, int userId, int libraryId, SequenceType type, SequenceState state, String name, double density, String key, double tempo) {
    return insertSequence(id, userId, libraryId, type, state, name, density, key, tempo, Timestamp.from(Instant.now()));
  }

  static Sequence insertSequence(int id, int userId, int libraryId, SequenceType type, SequenceState state, String name, double density, String key, double tempo, Timestamp createdUpdatedAt) {
    SequenceRecord record = IntegrationTestService.getDb().newRecord(SEQUENCE);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setLibraryId(ULong.valueOf(libraryId));
    record.setType(type.toString());
    record.setName(name);
    record.setDensity(density);
    record.setKey(key);
    record.setTempo(tempo);
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.setState(state.toString());
    record.store();

    Sequence result = new Sequence();
    result.setId(BigInteger.valueOf(id));
    result.setUserId(BigInteger.valueOf(userId));
    result.setLibraryId(BigInteger.valueOf(libraryId));
    result.setType(type.toString());
    result.setName(name);
    result.setDensity(density);
    result.setKey(key);
    result.setTempo(tempo);
    return result;
  }

  static void insertSequenceMeme(int id, int sequenceId, String name) {
    insertSequenceMeme(id, sequenceId, name, Timestamp.from(Instant.now()));
  }

  static void insertSequenceMeme(int id, int sequenceId, String name, Timestamp createdUpdatedAt) {
    SequenceMemeRecord record = IntegrationTestService.getDb().newRecord(SEQUENCE_MEME);
    record.setId(ULong.valueOf(id));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setName(name);
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.store();
  }

  static void insertPattern(int id, int sequenceId, PatternType type, PatternState state, int total, String name, double density, String key, double tempo) {
    insertPattern(id, sequenceId, type, state, total, name, density, key, tempo, Timestamp.from(Instant.now()), 4, 4, 0);
  }

  static void insertPattern(int id, int sequenceId, PatternType type, PatternState state, int total, String name, double density, String key, double tempo, int meterSuper, int meterSub, int meterSwing) {
    insertPattern(id, sequenceId, type, state, total, name, density, key, tempo, Timestamp.from(Instant.now()), meterSuper, meterSub, meterSwing);
  }

  static void insertPattern(int id, int sequenceId, PatternType type, PatternState state, int total, String name, double density, String key, double tempo, Timestamp createdUpdatedAt, int meterSuper, int meterSub, int meterSwing) {
    PatternRecord record = IntegrationTestService.getDb().newRecord(PATTERN);
    record.setId(ULong.valueOf(id));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setTotal(UInteger.valueOf(total));
    record.setName(name);
    record.setDensity(density);
    record.setKey(key);
    record.setTempo(tempo);
    record.setType(type.toString());
    record.setState(state.toString());
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.setMeterSuper(meterSuper);
    record.setMeterSub(meterSub);
    record.setMeterSwing(meterSwing);
    record.store();
  }

  static void insertPatternAndSequencePattern(int id, int sequenceId, PatternType type, PatternState state, int offset, int total, String name, double density, String key, double tempo) {
    insertPatternAndSequencePattern(id, sequenceId, type, state, offset, total, name, density, key, tempo, Timestamp.from(Instant.now()), 4, 4, 0);
  }

  static void insertPatternAndSequencePattern(int id, int sequenceId, PatternType type, PatternState state, int offset, int total, String name, double density, String key, double tempo, int meterSuper, int meterSub, int meterSwing) {
    insertPatternAndSequencePattern(id, sequenceId, type, state, offset, total, name, density, key, tempo, Timestamp.from(Instant.now()), meterSuper, meterSub, meterSwing);
  }

  static void insertPatternAndSequencePattern(int id, int sequenceId, PatternType type, PatternState state, int offset, int total, String name, double density, String key, double tempo, Timestamp createdUpdatedAt, int meterSuper, int meterSub, int meterSwing) {
    PatternRecord record = IntegrationTestService.getDb().newRecord(PATTERN);
    record.setId(ULong.valueOf(id));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setTotal(UInteger.valueOf(total));
    record.setName(name);
    record.setDensity(density);
    record.setKey(key);
    record.setTempo(tempo);
    record.setType(type.toString());
    record.setState(state.toString());
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.setMeterSuper(meterSuper);
    record.setMeterSub(meterSub);
    record.setMeterSwing(meterSwing);
    record.store();

    insertSequencePattern(id, sequenceId, id, offset, createdUpdatedAt);
  }

  static void insertSequencePattern(int id, int sequenceId, int patternId, int offset, Timestamp createdUpdatedAt) {
    SequencePatternRecord record = IntegrationTestService.getDb().newRecord(SEQUENCE_PATTERN);
    record.setId(ULong.valueOf(id));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setPatternId(ULong.valueOf(patternId));
    record.setOffset(ULong.valueOf(offset));
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.store();
  }

  static void insertSequencePatternMeme(int id, int sequenceId, int patternId, String name) {
    insertSequencePatternMeme(id, sequenceId, patternId, name, Timestamp.from(Instant.now()));
  }

  static void insertSequencePatternMeme(int id, int sequenceId, int patternId, String name, Timestamp createdUpdatedAt) {
    Result<SequencePatternRecord> parents = IntegrationTestService.getDb().selectFrom(SEQUENCE_PATTERN)
      .where(SEQUENCE_PATTERN.SEQUENCE_ID.eq(ULong.valueOf(sequenceId)))
      .and(SEQUENCE_PATTERN.PATTERN_ID.eq(ULong.valueOf(patternId)))
      .fetch();
    assertFalse(parents.isEmpty());
    SequencePatternRecord parent = parents.get(0);
    SequencePatternMemeRecord record = IntegrationTestService.getDb().newRecord(SEQUENCE_PATTERN_MEME);
    record.setId(ULong.valueOf(id));
    record.setSequencePatternId(parent.getId());
    record.setName(name);
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.store();
  }

  static void insertPatternChord(int id, int patternId, int position, String name) {
    insertPatternChord(id, patternId, position, name, Timestamp.from(Instant.now()));
  }

  static void insertPatternChord(int id, int patternId, double position, String name, Timestamp createdUpdatedAt) {
    PatternChordRecord record = IntegrationTestService.getDb().newRecord(PATTERN_CHORD);
    record.setId(ULong.valueOf(id));
    record.setPatternId(ULong.valueOf(patternId));
    record.setPosition(position);
    record.setName(name);
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.store();
  }

  static void insertVoice(int id, int sequenceId, InstrumentType type, String description) {
    insertVoice(id, sequenceId, type, description, Timestamp.from(Instant.now()));
  }

  static void insertVoice(int id, int sequenceId, InstrumentType type, String description, Timestamp createdUpdatedAt) {
    VoiceRecord record = IntegrationTestService.getDb().newRecord(VOICE);
    record.setId(ULong.valueOf(id));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setType(type.toString());
    record.setDescription(description);
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.store();
  }

  static void insertPatternEvent(int id, int patternId, int voiceId, double position, double duration, String inflection, String note, double tonality, double velocity) {
    insertPatternEvent(id, patternId, voiceId, position, duration, inflection, note, tonality, velocity, Timestamp.from(Instant.now()));
  }

  static void insertPatternEvent(int id, int patternId, int voiceId, double position, double duration, String inflection, String note, double tonality, double velocity, Timestamp createdUpdatedAt) {
    PatternEventRecord record = IntegrationTestService.getDb().newRecord(PATTERN_EVENT);
    record.setId(ULong.valueOf(id));
    record.setPatternId(ULong.valueOf(patternId));
    record.setVoiceId(ULong.valueOf(voiceId));
    record.setPosition(position);
    record.setDuration(duration);
    record.setInflection(inflection);
    record.setNote(note);
    record.setTonality(tonality);
    record.setVelocity(velocity);
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.store();
  }

  static void insertInstrument(int id, int libraryId, int userId, String description, InstrumentType type, double density) {
    insertInstrument(id, libraryId, userId, description, type, density, Timestamp.from(Instant.now()));
  }

  static void insertInstrument(int id, int libraryId, int userId, String description, InstrumentType type, double density, Timestamp createdUpdatedAt) {
    InstrumentRecord record = IntegrationTestService.getDb().newRecord(INSTRUMENT);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setLibraryId(ULong.valueOf(libraryId));
    record.setType(type.toString());
    record.setDescription(description);
    record.setDensity(density);
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.store();
  }

  static void insertInstrumentMeme(int id, int instrumentId, String name) {
    insertInstrumentMeme(id, instrumentId, name, Timestamp.from(Instant.now()));
  }

  static void insertInstrumentMeme(int id, int instrumentId, String name, Timestamp createdUpdatedAt) {
    InstrumentMemeRecord record = IntegrationTestService.getDb().newRecord(INSTRUMENT_MEME);
    record.setId(ULong.valueOf(id));
    record.setInstrumentId(ULong.valueOf(instrumentId));
    record.setName(name);
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.store();
  }

  static void insertAudio(int id, int instrumentId, String state, String name, String waveformKey, double start, double length, double tempo, double pitch) {
    insertAudio(id, instrumentId, state, name, waveformKey, start, length, tempo, pitch, Timestamp.from(Instant.now()));
  }

  static void insertAudio(int id, int instrumentId, String state, String name, String waveformKey, double start, double length, double tempo, double pitch, Timestamp createdUpdatedAt) {
    AudioRecord record = IntegrationTestService.getDb().newRecord(AUDIO);
    record.setId(ULong.valueOf(id));
    record.setInstrumentId(ULong.valueOf(instrumentId));
    record.setName(name);
    record.setWaveformKey(waveformKey);
    record.setStart(start);
    record.setLength(length);
    record.setTempo(tempo);
    record.setPitch(pitch);
    record.setState(state);
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.store();
  }

  static void insertAudioEvent(int id, int audioId, double position, double duration, String inflection, String note, double tonality, double velocity) {
    insertAudioEvent(id, audioId, position, duration, inflection, note, tonality, velocity, Timestamp.from(Instant.now()));
  }

  static void insertAudioEvent(int id, int audioId, double position, double duration, String inflection, String note, double tonality, double velocity, Timestamp createdUpdatedAt) {
    AudioEventRecord record = IntegrationTestService.getDb().newRecord(AUDIO_EVENT);
    record.setId(ULong.valueOf(id));
    record.setAudioId(ULong.valueOf(audioId));
    record.setPosition(position);
    record.setDuration(duration);
    record.setInflection(inflection);
    record.setNote(note);
    record.setTonality(tonality);
    record.setVelocity(velocity);
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.store();
  }

  static void insertAudioChord(int id, int audioId, int position, String name) {
    insertAudioChord(id, audioId, position, name, Timestamp.from(Instant.now()));
  }

  static void insertAudioChord(int id, int audioId, double position, String name, Timestamp createdUpdatedAt) {
    AudioChordRecord record = IntegrationTestService.getDb().newRecord(AUDIO_CHORD);
    record.setId(ULong.valueOf(id));
    record.setAudioId(ULong.valueOf(audioId));
    record.setPosition(position);
    record.setName(name);
    record.setCreatedAt(createdUpdatedAt);
    record.setUpdatedAt(createdUpdatedAt);
    record.store();
  }

  static Chain insertChain(int id, int accountId, String name, ChainType type, ChainState state, Timestamp startAt, @Nullable Timestamp stopAt, String embedKey) {
    ChainRecord record = IntegrationTestService.getDb().newRecord(CHAIN);
    record.setId(ULong.valueOf(id));
    record.setAccountId(ULong.valueOf(accountId));
    record.setType(type.toString());
    record.setName(name);
    record.setState(state.toString());
    record.setStartAt(startAt);
    if (Objects.nonNull(stopAt)) {
      record.setStopAt(stopAt);
    }
    if (Objects.nonNull(embedKey)) {
      record.setEmbedKey(embedKey);
    }
    record.store();

    Chain result = new Chain();
    result.setId(BigInteger.valueOf(id));
    result.setAccountId(BigInteger.valueOf(accountId));
    result.setTypeEnum(type);
    result.setName(name);
    result.setStateEnum(state);
    result.setStartAtTimestamp(startAt);
    if (Objects.nonNull(stopAt)) {
      result.setStopAtTimestamp(stopAt);
    }
    if (Objects.nonNull(embedKey)) {
      result.setEmbedKey(embedKey);
    }
    return result;
  }

  static void insertChainConfig(int id, int chainId, ChainConfigType chainConfigType, String value) {
    ChainConfigRecord record = IntegrationTestService.getDb().newRecord(CHAIN_CONFIG);
    record.setId(ULong.valueOf(id));
    record.setChainId(ULong.valueOf(chainId));
    record.setType(chainConfigType.toString());
    record.setValue(value);
    record.store();
  }

  static void insertChainLibrary(int id, int chainId, int libraryId) {
    ChainLibraryRecord record = IntegrationTestService.getDb().newRecord(CHAIN_LIBRARY);
    record.setId(ULong.valueOf(id));
    record.setChainId(ULong.valueOf(chainId));
    record.setLibraryId(ULong.valueOf(libraryId));
    record.store();
  }

  static void insertChainSequence(int id, int chainId, int sequenceId) {
    ChainSequenceRecord record = IntegrationTestService.getDb().newRecord(CHAIN_SEQUENCE);
    record.setId(ULong.valueOf(id));
    record.setChainId(ULong.valueOf(chainId));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.store();
  }

  static void insertChainInstrument(int id, int chainId, int instrumentId) {
    ChainInstrumentRecord record = IntegrationTestService.getDb().newRecord(CHAIN_INSTRUMENT);
    record.setId(ULong.valueOf(id));
    record.setChainId(ULong.valueOf(chainId));
    record.setInstrumentId(ULong.valueOf(instrumentId));
    record.store();
  }

  static Segment insertSegment(int id, int chainId, int offset, SegmentState state, Timestamp beginAt, Timestamp endAt, String key, int total, double density, double tempo, String waveformKey, JSONObject basis) {
    SegmentRecord record = IntegrationTestService.getDb().newRecord(SEGMENT);
    record.setId(ULong.valueOf(id));
    record.setChainId(ULong.valueOf(chainId));
    record.setOffset(ULong.valueOf(offset));
    record.setState(state.toString());
    record.setBeginAt(beginAt);
    record.setEndAt(endAt);
    record.setTotal(UInteger.valueOf(total));
    record.setKey(key);
    record.setDensity(density);
    record.setTempo(tempo);
    record.setWaveformKey(waveformKey);
    record.setBasis(basis.toString());
    record.store();

    Segment result = new Segment();
    result.setId(BigInteger.valueOf(id));
    result.setChainId(BigInteger.valueOf(chainId));
    result.setOffset(BigInteger.valueOf(offset));
    result.setState(state.toString());
    result.setBeginAtTimestamp(beginAt);
    result.setEndAtTimestamp(endAt);
    result.setTotal(total);
    result.setKey(key);
    result.setDensity(density);
    result.setTempo(tempo);
    result.setWaveformKey(waveformKey);
    result.setBasis(basis);
    return result;
  }

  static void insertSegmentChord(int id, int segmentId, double position, String name) {
    SegmentChordRecord record = IntegrationTestService.getDb().newRecord(SEGMENT_CHORD);
    record.setId(ULong.valueOf(id));
    record.setSegmentId(ULong.valueOf(segmentId));
    record.setPosition(position);
    record.setName(name);
    record.store();
  }

  static void insertSegmentMessage(int id, int segmentId, MessageType type, String body) {
    SegmentMessageRecord record = IntegrationTestService.getDb().newRecord(SEGMENT_MESSAGE);
    record.setId(ULong.valueOf(id));
    record.setSegmentId(ULong.valueOf(segmentId));
    record.setType(type.toString());
    record.setBody(body);
    record.store();
  }

  static void insertChoice(int id, int segmentId, int sequenceId, SequenceType type, int sequencePatternOffset, int transpose) {
    ChoiceRecord record = IntegrationTestService.getDb().newRecord(CHOICE);
    record.setId(ULong.valueOf(id));
    record.setSegmentId(ULong.valueOf(segmentId));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setType(type.toString());
    record.setTranspose(transpose);
    record.setSequencePatternOffset(ULong.valueOf(sequencePatternOffset));
    record.store();
  }

  static void insertArrangement(int id, int choiceId, int voiceId, int instrumentId) {
    ArrangementRecord record = IntegrationTestService.getDb().newRecord(ARRANGEMENT);
    record.setId(ULong.valueOf(id));
    record.setChoiceId(ULong.valueOf(choiceId));
    record.setVoiceId(ULong.valueOf(voiceId));
    record.setInstrumentId(ULong.valueOf(instrumentId));
    record.store();
  }

  static void insertSegmentMeme(int id, int segmentId, String name) {
    SegmentMemeRecord record = IntegrationTestService.getDb().newRecord(SEGMENT_MEME);
    record.setId(ULong.valueOf(id));
    record.setSegmentId(ULong.valueOf(segmentId));
    record.setName(name);
    record.store();
  }

  static Segment insertSegment_Planned(int id, int chainId, int offset, Timestamp beginAt, JSONObject basis) {
    SegmentRecord record = IntegrationTestService.getDb().newRecord(SEGMENT);
    record.setId(ULong.valueOf(id));
    record.setChainId(ULong.valueOf(chainId));
    record.setOffset(ULong.valueOf(offset));
    record.setState(SegmentState.Planned.toString());
    record.setBeginAt(beginAt);
    record.setBasis(basis.toString());
    record.store();

    Segment result = new Segment();
    result.setId(BigInteger.valueOf(id));
    result.setChainId(BigInteger.valueOf(chainId));
    result.setOffset(BigInteger.valueOf(offset));
    result.setStateEnum(SegmentState.Planned);
    result.setBeginAtTimestamp(beginAt);
    result.setBasis(basis);
    return result;
  }

  static void insertPlatformMessage(int id, MessageType type, String body, Timestamp createdAt) {
    PlatformMessageRecord record = IntegrationTestService.getDb().newRecord(PLATFORM_MESSAGE);
    record.setId(ULong.valueOf(id));
    record.setType(type.toString());
    record.setBody(body);
    record.setCreatedAt(createdAt);
    record.store();
  }

}
