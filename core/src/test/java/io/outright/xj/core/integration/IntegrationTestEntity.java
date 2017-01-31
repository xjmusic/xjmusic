package io.outright.xj.core.integration;

import io.outright.xj.core.Tables;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.tables.records.AccountRecord;
import io.outright.xj.core.tables.records.AccountUserRecord;
import io.outright.xj.core.tables.records.IdeaRecord;
import io.outright.xj.core.tables.records.LibraryRecord;
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
      // Point (before Morph & Voice Event)
      db.deleteFrom(POINT).execute();

      // Pick (before Morph & Audio)
      db.deleteFrom(PICK).execute();

      // Morph (before Arrangement)
      db.deleteFrom(MORPH).execute();

      // Arrangement (before Instrument, Voice & Choice)
      db.deleteFrom(ARRANGEMENT).execute();

      // Audio (before Instrument)
      db.deleteFrom(AUDIO_CHORD).execute();
      db.deleteFrom(AUDIO_EVENT).execute();
      db.deleteFrom(AUDIO).execute();

      // Voice (before Phase)
      db.deleteFrom(VOICE_EVENT).execute();
      db.deleteFrom(VOICE).execute();

      // Instrument (before Library & Credit)
      db.deleteFrom(INSTRUMENT_MEME).execute();
      db.deleteFrom(INSTRUMENT).execute();

      // Choice (before Link & Idea)
      db.deleteFrom(CHOICE).execute();

      // Link (before Chain)
      db.deleteFrom(LINK_CHORD).execute();
      db.deleteFrom(LINK).execute();

      // Chain (before Library & Account)
      db.deleteFrom(CHAIN_LIBRARY).execute();
      db.deleteFrom(CHAIN).execute();

      // Phase (before Idea)
      db.deleteFrom(PHASE_MEME).execute();
      db.deleteFrom(PHASE_CHORD).execute();
      db.deleteFrom(PHASE).execute();

      // Idea (before Library & Credit)
      db.deleteFrom(IDEA_MEME).execute();
      db.deleteFrom(Tables.IDEA).execute();

      // Library (before Account)
      db.deleteFrom(Tables.LIBRARY).execute();

      // Account (before User)
      db.deleteFrom(ACCOUNT_USER).execute();
      db.deleteFrom(ACCOUNT).execute();

      // User Access Token (before User & User Auth)
      db.deleteFrom(USER_ACCESS_TOKEN).execute();

      // User (last)
      db.deleteFrom(USER_AUTH).execute();
      db.deleteFrom(USER_ROLE).execute();
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


}
