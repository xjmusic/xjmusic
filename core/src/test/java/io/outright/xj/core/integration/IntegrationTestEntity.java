package io.outright.xj.core.integration;

import io.outright.xj.core.Tables;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.tables.records.AccountRecord;
import io.outright.xj.core.tables.records.AccountUserRecord;
import io.outright.xj.core.tables.records.ArrangementRecord;
import io.outright.xj.core.tables.records.AudioChordRecord;
import io.outright.xj.core.tables.records.AudioEventRecord;
import io.outright.xj.core.tables.records.AudioRecord;
import io.outright.xj.core.tables.records.ChainLibraryRecord;
import io.outright.xj.core.tables.records.ChainRecord;
import io.outright.xj.core.tables.records.ChoiceRecord;
import io.outright.xj.core.tables.records.IdeaMemeRecord;
import io.outright.xj.core.tables.records.IdeaRecord;
import io.outright.xj.core.tables.records.InstrumentMemeRecord;
import io.outright.xj.core.tables.records.InstrumentRecord;
import io.outright.xj.core.tables.records.LibraryRecord;
import io.outright.xj.core.tables.records.LinkChordRecord;
import io.outright.xj.core.tables.records.LinkRecord;
import io.outright.xj.core.tables.records.MorphRecord;
import io.outright.xj.core.tables.records.PhaseChordRecord;
import io.outright.xj.core.tables.records.PhaseMemeRecord;
import io.outright.xj.core.tables.records.PhaseRecord;
import io.outright.xj.core.tables.records.PickRecord;
import io.outright.xj.core.tables.records.PointRecord;
import io.outright.xj.core.tables.records.UserAccessTokenRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;
import io.outright.xj.core.tables.records.VoiceEventRecord;
import io.outright.xj.core.tables.records.VoiceRecord;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Timestamp;

import static io.outright.xj.core.Tables.ACCOUNT;
import static io.outright.xj.core.Tables.ACCOUNT_USER;
import static io.outright.xj.core.Tables.ARRANGEMENT;
import static io.outright.xj.core.Tables.AUDIO;
import static io.outright.xj.core.Tables.AUDIO_CHORD;
import static io.outright.xj.core.Tables.AUDIO_EVENT;
import static io.outright.xj.core.Tables.CHAIN;
import static io.outright.xj.core.Tables.CHAIN_LIBRARY;
import static io.outright.xj.core.Tables.CHOICE;
import static io.outright.xj.core.Tables.IDEA_MEME;
import static io.outright.xj.core.Tables.INSTRUMENT;
import static io.outright.xj.core.Tables.INSTRUMENT_MEME;
import static io.outright.xj.core.Tables.LINK;
import static io.outright.xj.core.Tables.LINK_CHORD;
import static io.outright.xj.core.Tables.MORPH;
import static io.outright.xj.core.Tables.PHASE;
import static io.outright.xj.core.Tables.PHASE_CHORD;
import static io.outright.xj.core.Tables.PHASE_MEME;
import static io.outright.xj.core.Tables.PICK;
import static io.outright.xj.core.Tables.POINT;
import static io.outright.xj.core.Tables.USER;
import static io.outright.xj.core.Tables.USER_ACCESS_TOKEN;
import static io.outright.xj.core.Tables.USER_AUTH;
import static io.outright.xj.core.Tables.USER_ROLE;
import static io.outright.xj.core.Tables.VOICE;
import static io.outright.xj.core.Tables.VOICE_EVENT;
import static io.outright.xj.core.tables.Idea.IDEA;
import static io.outright.xj.core.tables.Library.LIBRARY;

public abstract class IntegrationTestEntity {
  private static Logger log = LoggerFactory.getLogger(IntegrationTestEntity.class);

  /**
   * Reset the database before an integration test.
   */
  public static void deleteAll() throws DatabaseException {
    DSLContext db = IntegrationTestService.getDb();
    try {
      // Point
      db.deleteFrom(POINT).execute(); // before Morph & Voice Event

      // Pick
      db.deleteFrom(PICK).execute(); // before Morph & Audio

      // Morph
      db.deleteFrom(MORPH).execute(); // before Arrangement

      // Arrangement
      db.deleteFrom(ARRANGEMENT).execute(); // before Instrument, Voice & Choice

      // Audio
      db.deleteFrom(AUDIO_CHORD).execute(); // before Audio
      db.deleteFrom(AUDIO_EVENT).execute(); // before Audio
      db.deleteFrom(AUDIO).execute(); // before Instrument

      // Voice
      db.deleteFrom(VOICE_EVENT).execute(); // before Voice
      db.deleteFrom(VOICE).execute(); // before Phase

      // Instrument
      db.deleteFrom(INSTRUMENT_MEME).execute(); // before Instrument
      db.deleteFrom(INSTRUMENT).execute(); // before Library & Credit

      // Choice
      db.deleteFrom(CHOICE).execute(); // before Link & Idea

      // Link
      db.deleteFrom(LINK_CHORD).execute(); // before Link
      db.deleteFrom(LINK).execute(); // before Chain

      // Chain
      db.deleteFrom(CHAIN_LIBRARY).execute(); // before Chain & Library
      db.deleteFrom(CHAIN).execute(); // before Account

      // Phase
      db.deleteFrom(PHASE_MEME).execute(); // before Phase
      db.deleteFrom(PHASE_CHORD).execute(); // before Phase
      db.deleteFrom(PHASE).execute(); // before Idea

      // Idea
      db.deleteFrom(IDEA_MEME).execute(); // before Idea
      db.deleteFrom(Tables.IDEA).execute(); // before Library & Credit

      // Library
      db.deleteFrom(Tables.LIBRARY).execute(); // before Account

      // Account
      db.deleteFrom(ACCOUNT_USER).execute(); // before Account
      db.deleteFrom(ACCOUNT).execute(); //before User

      // User Access Token
      db.deleteFrom(USER_ACCESS_TOKEN).execute(); // before User & User Auth

      // User
      db.deleteFrom(USER_AUTH).execute(); // before User
      db.deleteFrom(USER_ROLE).execute(); // before User
      db.deleteFrom(USER).execute();

    } catch (Exception e) {
      log.error(e.getClass().getName() + ": " + e);
      throw new DatabaseException(e.getClass().getName() + ": " + e);
    }

    log.info("Did delete all records from integration database.");
  }

  public static void insertUserAuth(Integer id, Integer userId, String type, String externalAccessToken, String externalRefreshToken, String externalAccount) {
    UserAuthRecord record = IntegrationTestService.getDb().newRecord(USER_AUTH);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setType(type);
    record.setExternalAccessToken(externalAccessToken);
    record.setExternalRefreshToken(externalRefreshToken);
    record.setExternalAccount(externalAccount);
    record.store();
  }

  public static void insertUser(Integer id, String name, String email, String avatarUrl) {
    UserRecord record = IntegrationTestService.getDb().newRecord(USER);
    record.setId(ULong.valueOf(id));
    record.setName(name);
    record.setEmail(email);
    record.setAvatarUrl(avatarUrl);
    record.store();
  }

  public static void insertUserRole(Integer id, Integer userId, String type) {
    UserRoleRecord record = IntegrationTestService.getDb().newRecord(USER_ROLE);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setType(type);
    record.store();
  }

  public static void insertAccountUser(Integer id, Integer accountId, Integer userId) {
    AccountUserRecord record = IntegrationTestService.getDb().newRecord(ACCOUNT_USER);
    record.setId(ULong.valueOf(id));
    record.setAccountId(ULong.valueOf(accountId));
    record.setUserId(ULong.valueOf(userId));
    record.store();
  }

  public static void insertAccount(Integer id, String name) {
    AccountRecord record = IntegrationTestService.getDb().newRecord(ACCOUNT);
    record.setId(ULong.valueOf(id));
    record.setName(name);
    record.store();
  }

  public static void insertUserAccessToken(int userId, int userAuthId, String accessToken) {
    UserAccessTokenRecord record = IntegrationTestService.getDb().newRecord(USER_ACCESS_TOKEN);
    record.setUserId(ULong.valueOf(userId));
    record.setUserAuthId(ULong.valueOf(userAuthId));
    record.setAccessToken(accessToken);
    record.store();
  }

  public static void insertLibrary(int id, int accountId, String name) {
    LibraryRecord record = IntegrationTestService.getDb().newRecord(LIBRARY);
    record.setId(ULong.valueOf(id));
    record.setAccountId(ULong.valueOf(accountId));
    record.setName(name);
    record.store();
  }

  public static void insertIdea(int id, int userId, int libraryId, String type, String name, double density, String key, double tempo) {
    IdeaRecord record = IntegrationTestService.getDb().newRecord(IDEA);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setLibraryId(ULong.valueOf(libraryId));
    record.setType(type);
    record.setName(name);
    record.setDensity(density);
    record.setKey(key);
    record.setTempo(tempo);
    record.store();
  }

  public static void insertIdeaMeme(int id, int ideaId, String name) {
    IdeaMemeRecord record = IntegrationTestService.getDb().newRecord(IDEA_MEME);
    record.setId(ULong.valueOf(id));
    record.setIdeaId(ULong.valueOf(ideaId));
    record.setName(name);
    record.store();
  }

  public static void insertPhase(int id, int ideaId, int offset, int total, String name, double density, String key, double tempo) {
    PhaseRecord record = IntegrationTestService.getDb().newRecord(PHASE);
    record.setId(ULong.valueOf(id));
    record.setIdeaId(ULong.valueOf(ideaId));
    record.setOffset(ULong.valueOf(offset));
    record.setTotal(ULong.valueOf(total));
    record.setName(name);
    record.setDensity(density);
    record.setKey(key);
    record.setTempo(tempo);
    record.store();
  }

  public static void insertPhaseMeme(int id, int phaseId, String name) {
    PhaseMemeRecord record = IntegrationTestService.getDb().newRecord(PHASE_MEME);
    record.setId(ULong.valueOf(id));
    record.setPhaseId(ULong.valueOf(phaseId));
    record.setName(name);
    record.store();
  }

  public static void insertPhaseChord(int id, int phaseId, double position, String name) {
    PhaseChordRecord record = IntegrationTestService.getDb().newRecord(PHASE_CHORD);
    record.setId(ULong.valueOf(id));
    record.setPhaseId(ULong.valueOf(phaseId));
    record.setPosition(position);
    record.setName(name);
    record.store();
  }

  public static void insertVoice(int id, int phaseId, String type, String description) {
    VoiceRecord record = IntegrationTestService.getDb().newRecord(VOICE);
    record.setId(ULong.valueOf(id));
    record.setPhaseId(ULong.valueOf(phaseId));
    record.setType(type);
    record.setDescription(description);
    record.store();
  }

  public static void insertVoiceEvent(int id, int voiceId, double position, double duration, String inflection, String note, double tonality, double velocity) {
    VoiceEventRecord record = IntegrationTestService.getDb().newRecord(VOICE_EVENT);
    record.setId(ULong.valueOf(id));
    record.setVoiceId(ULong.valueOf(voiceId));
    record.setPosition(position);
    record.setDuration(duration);
    record.setInflection(inflection);
    record.setNote(note);
    record.setTonality(tonality);
    record.setVelocity(velocity);
    record.store();
  }

  public static void insertInstrument(int id, int libraryId, int userId, String description, String type, double density) {
    InstrumentRecord record = IntegrationTestService.getDb().newRecord(INSTRUMENT);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setLibraryId(ULong.valueOf(libraryId));
    record.setType(type);
    record.setDescription(description);
    record.setDensity(density);
    record.store();
  }

  public static void insertInstrumentMeme(int id, int instrumentId, String name) {
    InstrumentMemeRecord record = IntegrationTestService.getDb().newRecord(INSTRUMENT_MEME);
    record.setId(ULong.valueOf(id));
    record.setInstrumentId(ULong.valueOf(instrumentId));
    record.setName(name);
    record.store();
  }

  public static void insertAudio(int id, int instrumentId, String name, String waveformKey, double start, double length, double tempo, double pitch) {
    AudioRecord record = IntegrationTestService.getDb().newRecord(AUDIO);
    record.setId(ULong.valueOf(id));
    record.setInstrumentId(ULong.valueOf(instrumentId));
    record.setName(name);
    record.setWaveformKey(waveformKey);
    record.setStart(start);
    record.setLength(length);
    record.setTempo(tempo);
    record.setPitch(pitch);
    record.store();
  }

  public static void insertAudioEvent(int id, int audioId, double position, double duration, String inflection, String note, double tonality, double velocity) {
    AudioEventRecord record = IntegrationTestService.getDb().newRecord(AUDIO_EVENT);
    record.setId(ULong.valueOf(id));
    record.setAudioId(ULong.valueOf(audioId));
    record.setPosition(position);
    record.setDuration(duration);
    record.setInflection(inflection);
    record.setNote(note);
    record.setTonality(tonality);
    record.setVelocity(velocity);
    record.store();
  }


  public static void insertAudioChord(int id, int audioId, double position, String name) {
    AudioChordRecord record = IntegrationTestService.getDb().newRecord(AUDIO_CHORD);
    record.setId(ULong.valueOf(id));
    record.setAudioId(ULong.valueOf(audioId));
    record.setPosition(position);
    record.setName(name);
    record.store();
  }

  public static void insertChain(int id, int accountId, String name, String state, Timestamp startAt, @Nullable Timestamp stopAt) {
    ChainRecord record = IntegrationTestService.getDb().newRecord(CHAIN);
    record.setId(ULong.valueOf(id));
    record.setAccountId(ULong.valueOf(accountId));
    record.setName(name);
    record.setState(state);
    record.setStartAt(startAt);
    if (stopAt != null) {
      record.setStopAt(stopAt);
    }
    record.store();
  }

  public static void insertChainLibrary(int id, int chainId, int libraryId) {
    ChainLibraryRecord record = IntegrationTestService.getDb().newRecord(CHAIN_LIBRARY);
    record.setId(ULong.valueOf(id));
    record.setChainId(ULong.valueOf(chainId));
    record.setLibraryId(ULong.valueOf(libraryId));
    record.store();
  }

  public static void insertLink(int id, int chainId, int offset, String state, Timestamp beginAt, @Nullable Timestamp endAt, String key, int total, double density, double tempo) {
    LinkRecord record = IntegrationTestService.getDb().newRecord(LINK);
    record.setId(ULong.valueOf(id));
    record.setChainId(ULong.valueOf(chainId));
    record.setOffset(ULong.valueOf(offset));
    record.setState(state);
    record.setBeginAt(beginAt);
    if (endAt != null) {
      record.setEndAt(endAt);
    }
    record.setTotal(ULong.valueOf(total));
    record.setKey(key);
    record.setDensity(density);
    record.setTempo(tempo);
    record.store();
  }

  public static void insertLinkChord(int id, int linkId, double position, String name) {
    LinkChordRecord record = IntegrationTestService.getDb().newRecord(LINK_CHORD);
    record.setId(ULong.valueOf(id));
    record.setLinkId(ULong.valueOf(linkId));
    record.setPosition(position);
    record.setName(name);
    record.store();
  }

  public static void insertChoice(int id, int linkId, int ideaId, String type, int phaseOffset, int transpose) {
    ChoiceRecord record = IntegrationTestService.getDb().newRecord(CHOICE);
    record.setId(ULong.valueOf(id));
    record.setLinkId(ULong.valueOf(linkId));
    record.setIdeaId(ULong.valueOf(ideaId));
    record.setType(type);
    record.setTranspose(transpose);
    record.setPhaseOffset(UInteger.valueOf(phaseOffset));
    record.store();
  }

  public static void insertArrangement(int id, int choiceId, int voiceId, int instrumentId) {
    ArrangementRecord record = IntegrationTestService.getDb().newRecord(ARRANGEMENT);
    record.setId(ULong.valueOf(id));
    record.setChoiceId(ULong.valueOf(choiceId));
    record.setVoiceId(ULong.valueOf(voiceId));
    record.setInstrumentId(ULong.valueOf(instrumentId));
    record.store();
  }

  public static void insertMorph(int id, int arrangementId, Double position, String note, Double duration) {
    MorphRecord record = IntegrationTestService.getDb().newRecord(MORPH);
    record.setId(ULong.valueOf(id));
    record.setArrangementId(ULong.valueOf(arrangementId));
    record.setPosition(position);
    record.setNote(note);
    record.setDuration(duration);
    record.store();
  }

  public static void insertPick(int id, int arrangementId, int morphId, int audioId, double start, double length, double amplitude, double pitch) {
    PickRecord record = IntegrationTestService.getDb().newRecord(PICK);
    record.setId(ULong.valueOf(id));
    record.setArrangementId(ULong.valueOf(arrangementId));
    record.setMorphId(ULong.valueOf(morphId));
    record.setAudioId(ULong.valueOf(audioId));
    record.setStart(start);
    record.setLength(length);
    record.setAmplitude(amplitude);
    record.setPitch(pitch);
    record.store();
  }

  public static void insertPoint(int id, int morphId, int voiceEventId, double position, String note, double duration) {
    PointRecord record = IntegrationTestService.getDb().newRecord(POINT);
    record.setId(ULong.valueOf(id));
    record.setMorphId(ULong.valueOf(morphId));
    record.setVoiceEventId(ULong.valueOf(voiceEventId));
    record.setPosition(position);
    record.setNote(note);
    record.setDuration(duration);
    record.store();
  }
}
