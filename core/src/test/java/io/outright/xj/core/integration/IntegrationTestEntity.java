package io.outright.xj.core.integration;

import io.outright.xj.core.Tables;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.tables.records.AccountRecord;
import io.outright.xj.core.tables.records.AccountUserRecord;
import io.outright.xj.core.tables.records.IdeaMemeRecord;
import io.outright.xj.core.tables.records.IdeaRecord;
import io.outright.xj.core.tables.records.LibraryRecord;
import io.outright.xj.core.tables.records.PhaseChordRecord;
import io.outright.xj.core.tables.records.PhaseMemeRecord;
import io.outright.xj.core.tables.records.PhaseRecord;
import io.outright.xj.core.tables.records.UserAccessTokenRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public static void insertUserRole(Integer userId, String type) {
    UserRoleRecord record = IntegrationTestService.getDb().newRecord(USER_ROLE);
    record.setUserId(ULong.valueOf(userId));
    record.setType(type);
    record.store();
  }

  public static void insertAccountUser(Integer accountId, Integer userId) {
    AccountUserRecord record = IntegrationTestService.getDb().newRecord(ACCOUNT_USER);
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
}
