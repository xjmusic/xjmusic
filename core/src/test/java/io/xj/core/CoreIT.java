package io.xj.core;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.DAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.account.Account;
import io.xj.core.model.account.AccountUser;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.library.Library;
import io.xj.core.model.message.platform.PlatformMessage;
import io.xj.core.model.program.Program;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.user.User;
import io.xj.core.model.user.access_token.UserAccessToken;
import io.xj.core.model.user.auth.UserAuth;
import io.xj.core.model.user.role.UserRole;
import io.xj.core.tables.records.AccountRecord;
import io.xj.core.tables.records.AccountUserRecord;
import io.xj.core.tables.records.ChainRecord;
import io.xj.core.tables.records.InstrumentRecord;
import io.xj.core.tables.records.LibraryRecord;
import io.xj.core.tables.records.PlatformMessageRecord;
import io.xj.core.tables.records.ProgramRecord;
import io.xj.core.tables.records.SegmentRecord;
import io.xj.core.tables.records.UserAccessTokenRecord;
import io.xj.core.tables.records.UserAuthRecord;
import io.xj.core.tables.records.UserRecord;
import io.xj.core.tables.records.UserRoleRecord;
import io.xj.core.testing.IntegrationTestProvider;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.junit.After;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Objects;

import static io.xj.core.Tables.ACCOUNT;
import static io.xj.core.Tables.ACCOUNT_USER;
import static io.xj.core.Tables.CHAIN;
import static io.xj.core.Tables.INSTRUMENT;
import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.Tables.PLATFORM_MESSAGE;
import static io.xj.core.Tables.SEGMENT;
import static io.xj.core.Tables.USER;
import static io.xj.core.Tables.USER_ACCESS_TOKEN;
import static io.xj.core.Tables.USER_AUTH;
import static io.xj.core.Tables.USER_ROLE;
import static io.xj.core.tables.Program.PROGRAM;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CoreIT extends CoreTest {
  protected IntegrationTestProvider integrationTestProvider = injector.getInstance(IntegrationTestProvider.class);
  protected DSLContext db = integrationTestProvider.getDb();
  private long nextUniqueId = 1;

  /**
   [#165951041] DAO methods throw exception when record is not found (instead of returning null)
   <p>
   Assert an entity does not exist, by making a DAO.readOne() request and asserting the exception

   @param testDAO to use for attempting to retrieve entity
   @param id      of entity
   @param <N>     DAO class
   */
  protected static <N extends DAO> void assertNotExist(N testDAO, BigInteger id) {
    try {
      testDAO.readOne(Access.internal(), id);
      fail();
    } catch (CoreException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  /**
   get a file from java resources

   @param filePath to get
   @return File
   */
  protected static File resourceFile(String filePath) throws FileNotFoundException {
    ClassLoader classLoader = CoreIT.class.getClassLoader();
    URL resource = classLoader.getResource(filePath);
    if (Objects.isNull(resource))
      throw new FileNotFoundException(String.format("Failed to load resource: %s", filePath));
    return new File(resource.getFile());
  }

  /**
   After test completion, shutdown the database connection
   */
  @After
  public void shutdownDatabase() {
    integrationTestProvider.shutdown();
  }

  /**
   Reset the database before an integration test.
   */
  protected void reset() throws CoreException {
    try {
      // Tables
      db.deleteFrom(SEGMENT).execute(); // before Chain
      db.deleteFrom(CHAIN).execute(); // before Account
      db.deleteFrom(INSTRUMENT).execute(); // before Library
      db.deleteFrom(PROGRAM).execute(); // before Library
      db.deleteFrom(LIBRARY).execute(); // before Account
      db.deleteFrom(ACCOUNT_USER).execute(); // before Account
      db.deleteFrom(ACCOUNT).execute(); //before User
      db.deleteFrom(PLATFORM_MESSAGE).execute();
      db.deleteFrom(USER_ACCESS_TOKEN).execute(); // before User & User Auth
      db.deleteFrom(USER_AUTH).execute(); // before User
      db.deleteFrom(USER_ROLE).execute(); // before User
      db.deleteFrom(USER).execute();

      // Finally, all queues
      integrationTestProvider.flushRedis();

    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      throw new CoreException(e.getClass().getName(), e);
    }
    log.info("Did delete all records from integration database.");
  }

  /**
   Insert a User  record to the database

   @param model to insert
   @return the same User (for chaining methods)
   */
  protected User insert(User model) {
    UserRecord record = db.newRecord(USER);
    record.setId(ULong.valueOf(model.getId()));
    record.setName(model.getName());
    record.setEmail(model.getEmail());
    record.setAvatarUrl(model.getAvatarUrl());
    record.store();
    model.setId(record.getId().toBigInteger());
    return model;
  }

  /**
   Insert a UserRole  record to the database

   @param model to insert
   @return the same UserRole (for chaining methods)
   */
  protected UserRole insert(UserRole model) {
    UserRoleRecord record = db.newRecord(USER_ROLE);
    if (Objects.nonNull(model.getId())) record.setId(ULong.valueOf(model.getId()));
    record.setUserId(ULong.valueOf(model.getUserId()));
    record.setType(model.getType().toString());
    record.store();
    model.setId(record.getId().toBigInteger());
    return model;
  }

  /**
   Insert an AccountUser to the database

   @param model to insert
   @return same Account User (for chaining methods)
   */
  protected AccountUser insert(AccountUser model) {
    AccountUserRecord record = db.newRecord(ACCOUNT_USER);
    if (Objects.nonNull(model.getId())) record.setId(ULong.valueOf(model.getId()));
    record.setAccountId(ULong.valueOf(model.getAccountId()));
    record.setUserId(ULong.valueOf(model.getUserId()));
    record.store();
    model.setId(record.getId().toBigInteger());
    return model;
  }

  /**
   Insert an Account to the database

   @param model to insert
   @return same Account (for chaining methods)
   */
  protected Account insert(Account model) {
    AccountRecord record = db.newRecord(ACCOUNT);
    record.setId(ULong.valueOf(model.getId()));
    record.setName(model.getName());
    record.store();
    model.setId(record.getId().toBigInteger());
    return model;
  }

  /**
   Insert Chain to database

   @param model to insert
   @return the same chain (for chaining methods)
   */
  protected Chain insert(Chain model) {
    ChainRecord record = db.newRecord(CHAIN);
    record.setId(ULong.valueOf(model.getId()));
    record.setAccountId(ULong.valueOf(model.getAccountId()));
    record.setType(model.getType().toString());
    record.setName(model.getName());
    record.setState(model.getState().toString());
    record.setStartAt(Timestamp.from(model.getStartAt()));
    if (Objects.nonNull(model.getStopAt())) record.setStopAt(Timestamp.from(model.getStopAt()));
    if (Objects.nonNull(model.getEmbedKey())) record.setEmbedKey(model.getEmbedKey());
    record.setContent(gsonProvider.gson().toJson(model.getContent()));
    record.setCreatedAt(Timestamp.from(model.getCreatedAt()));
    record.setUpdatedAt(Timestamp.from(model.getUpdatedAt()));
    record.store();
    model.setId(record.getId().toBigInteger());
    return model;
  }

  /**
   Insert a Library to the database

   @param model to insert
   @return the same library (for chaining methods)
   */
  protected Library insert(Library model) {
    LibraryRecord record = db.newRecord(LIBRARY);
    record.setId(ULong.valueOf(model.getId()));
    record.setAccountId(ULong.valueOf(model.getAccountId()));
    record.setName(model.getName());
    record.setCreatedAt(Timestamp.from(model.getCreatedAt()));
    record.setUpdatedAt(Timestamp.from(model.getUpdatedAt()));
    record.store();
    model.setId(record.getId().toBigInteger());
    return model;
  }

  /**
   Insert a PlatformMessage to the database

   @param model to insert
   @return the same platform message (for chaining methods)
   */
  protected PlatformMessage insert(PlatformMessage model) {
    PlatformMessageRecord record = db.newRecord(PLATFORM_MESSAGE);
    record.setId(ULong.valueOf(model.getId()));
    record.setType(String.valueOf(model.getType()));
    record.setBody(model.getBody());
    record.setCreatedAt(Timestamp.from(model.getCreatedAt()));
    record.setUpdatedAt(Timestamp.from(model.getUpdatedAt()));
    record.store();
    model.setId(record.getId().toBigInteger());
    return model;
  }

  /**
   Insert a new Instrument model to database record

   @param model to insert
   @return the same instrument (for chaining methods)
   */
  protected Instrument insert(Instrument model) {
    InstrumentRecord record = db.newRecord(INSTRUMENT);
    record.setId(ULong.valueOf(model.getId()));
    record.setDescription(model.getDescription());
    record.setUserId(ULong.valueOf(model.getUserId()));
    record.setLibraryId(ULong.valueOf(model.getLibraryId()));
    record.setId(ULong.valueOf(model.getId()));
    record.setType(String.valueOf(model.getType()));
    record.setState(String.valueOf(model.getState()));
    record.setCreatedAt(Timestamp.from(model.getCreatedAt()));
    record.setUpdatedAt(Timestamp.from(model.getUpdatedAt()));
    record.setContent(gsonProvider.gson().toJson(model.getContent()));
    record.store();
    model.setId(record.getId().toBigInteger());
    return model;
  }

  /**
   Insert a new Program model to database record

   @param model to insert
   @return the same program (for chaining methods)
   */
  protected Program insert(Program model) {
    ProgramRecord record = db.newRecord(PROGRAM);
    record.setContent(gsonProvider.gson().toJson(model.getContent()));
    record.setId(ULong.valueOf(model.getId()));
    record.setUserId(ULong.valueOf(model.getUserId()));
    record.setLibraryId(ULong.valueOf(model.getLibraryId()));
    record.setName(model.getName());
    record.setKey(model.getKey());
    record.setTempo(model.getTempo());
    record.setType(model.getType().toString());
    record.setState(model.getState().toString());
    record.setCreatedAt(Timestamp.from(model.getCreatedAt()));
    record.setUpdatedAt(Timestamp.from(model.getUpdatedAt()));
    record.store();
    model.setId(record.getId().toBigInteger());
    return model;
  }

  /**
   Insert a new Segment model to database record

   @param model to insert
   @return the same segment (for chaining methods)
   */
  protected Segment insert(Segment model) {
    SegmentRecord record = db.newRecord(SEGMENT);
    record.setContent(gsonProvider.gson().toJson(model.getContent()));
    record.setId(ULong.valueOf(model.getId()));
    record.setChainId(ULong.valueOf(model.getChainId()));
    record.setOffset(ULong.valueOf(model.getOffset()));
    record.setState(model.getState().toString());
    if (Objects.nonNull(model.getBeginAt())) record.setBeginAt(Timestamp.from(model.getBeginAt()));
    if (Objects.nonNull(model.getEndAt())) record.setEndAt(Timestamp.from(model.getEndAt()));
    if (Objects.nonNull(model.getTotal())) record.setTotal(UInteger.valueOf(model.getTotal()));
    record.setKey(model.getKey());
    record.setDensity(model.getDensity());
    record.setTempo(model.getTempo());
    record.setWaveformKey(model.getWaveformKey());
    if (Objects.nonNull(model.getCreatedAt())) record.setCreatedAt(Timestamp.from(model.getCreatedAt()));
    if (Objects.nonNull(model.getUpdatedAt())) record.setUpdatedAt(Timestamp.from(model.getUpdatedAt()));
    record.store();
    model.setId(record.getId().toBigInteger());
    return model;
  }

  /**
   Insert UserAccessToken to database

   @param model to insert
   @return the same UserAccessToken (For chaining methods)
   */
  protected UserAccessToken insert(UserAccessToken model) {
    UserAccessTokenRecord record = db.newRecord(USER_ACCESS_TOKEN);
    if (Objects.nonNull(model.getId())) record.setId(ULong.valueOf(model.getId()));
    record.setUserId(ULong.valueOf(model.getUserId()));
    record.setUserAuthId(ULong.valueOf(model.getUserAuthId()));
    record.setAccessToken(model.getAccessToken());
    if (Objects.nonNull(model.getCreatedAt()))
      record.setCreatedAt(Timestamp.from(model.getCreatedAt()));
    if (Objects.nonNull(model.getUpdatedAt()))
      record.setUpdatedAt(Timestamp.from(model.getUpdatedAt()));
    record.store();
    model.setId(record.getId().toBigInteger());
    return model;
  }

  /**
   Insert a new UserAuth model to database record

   @param model to insert
   @return the same UserAuth (for chaining methods)
   */
  protected UserAuth insert(UserAuth model) {
    UserAuthRecord record = db.newRecord(USER_AUTH);
    if (Objects.nonNull(model.getId())) record.setId(ULong.valueOf(model.getId()));
    record.setUserId(ULong.valueOf(model.getUserId()));
    record.setType(model.getType().toString());
    record.setExternalAccessToken(model.getExternalAccessToken());
    record.setExternalRefreshToken(model.getExternalRefreshToken());
    record.setExternalAccount(model.getExternalAccount());
    if (Objects.nonNull(model.getCreatedAt())) record.setCreatedAt(Timestamp.from(model.getCreatedAt()));
    if (Objects.nonNull(model.getUpdatedAt())) record.setUpdatedAt(Timestamp.from(model.getUpdatedAt()));
    record.store();
    model.setId(record.getId().toBigInteger());
    return model;
  }

  /**
   Get next unique id

   @return next unique id
   */
  protected long getNextUniqueId() {
    nextUniqueId++;
    return nextUniqueId;
  }

}
