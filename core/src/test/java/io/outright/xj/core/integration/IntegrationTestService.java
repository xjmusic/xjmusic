// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.integration;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.migration.MigrationService;

import org.jooq.DSLContext;

import com.google.inject.Guice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

import static io.outright.xj.core.Tables.ACCOUNT;
import static io.outright.xj.core.Tables.ACCOUNT_USER;
import static io.outright.xj.core.Tables.ARRANGEMENT;
import static io.outright.xj.core.Tables.AUDIO;
import static io.outright.xj.core.Tables.AUDIO_CHORD;
import static io.outright.xj.core.Tables.AUDIO_EVENT;
import static io.outright.xj.core.Tables.CHAIN;
import static io.outright.xj.core.Tables.CHAIN_LIBRARY;
import static io.outright.xj.core.Tables.CHOICE;
import static io.outright.xj.core.Tables.IDEA;
import static io.outright.xj.core.Tables.IDEA_MEME;
import static io.outright.xj.core.Tables.INSTRUMENT;
import static io.outright.xj.core.Tables.INSTRUMENT_MEME;
import static io.outright.xj.core.Tables.LIBRARY;
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

public enum IntegrationTestService {
  INSTANCE;
  private Logger log = LoggerFactory.getLogger(IntegrationTestService.class);
  private Connection dbConnection;
  private DSLContext db;

  IntegrationTestService() {
    log.info("Will prepare integration database.");

    // One database connection remains open until main program exit
    System.setProperty("db.mysql.database", "xj_test");
    SQLDatabaseProvider dbProvider = Guice.createInjector(new CoreModule())
      .getInstance(SQLDatabaseProvider.class);
    try {
      dbConnection = dbProvider.getConnection();
    } catch (DatabaseException e) {
      log.error("DatabaseException: " + e);
      System.exit(1);
    }
    db = dbProvider.getContext(dbConnection);

    // Shut it down before program exit
    Runtime.getRuntime().addShutdownHook(new Thread(IntegrationTestService::shutdown));

    // Migrate the test database
    try {
      MigrationService.GLOBAL.migrate(dbProvider);
    } catch (ConfigException e) {
      log.error("ConfigException: " + e);
      System.exit(1);
    }

    // Like a boy scout
    log.info("Did open master connection and prepare integration database.");
  }

  /**
   * Run this before every integration test.
   * It'll only execute once, the first time it's run.
   */
  public static void setup() throws ConfigException, DatabaseException {
    deleteFromAllTables();
  }

  /**
   * Get the master connection to integration database
   *
   * @return DSL Context
   */
  public static DSLContext getDb() {
    return INSTANCE.db;
  }

  /**
   * Reset the database before an integration test.
   */
  private static void deleteFromAllTables() throws DatabaseException {
    try {
      // Point (before Morph & Voice Event)
      INSTANCE.db.deleteFrom(POINT).execute();

      // Pick (before Morph & Audio)
      INSTANCE.db.deleteFrom(PICK).execute();

      // Morph (before Arrangement)
      INSTANCE.db.deleteFrom(MORPH).execute();

      // Arrangement (before Instrument, Voice & Choice)
      INSTANCE.db.deleteFrom(ARRANGEMENT).execute();

      // Audio (before Instrument)
      INSTANCE.db.deleteFrom(AUDIO_CHORD).execute();
      INSTANCE.db.deleteFrom(AUDIO_EVENT).execute();
      INSTANCE.db.deleteFrom(AUDIO).execute();

      // Voice (before Phase)
      INSTANCE.db.deleteFrom(VOICE_EVENT).execute();
      INSTANCE.db.deleteFrom(VOICE).execute();

      // Instrument (before Library & Credit)
      INSTANCE.db.deleteFrom(INSTRUMENT_MEME).execute();
      INSTANCE.db.deleteFrom(INSTRUMENT).execute();

      // Choice (before Link & Idea)
      INSTANCE.db.deleteFrom(CHOICE).execute();

      // Link (before Chain)
      INSTANCE.db.deleteFrom(LINK_CHORD).execute();
      INSTANCE.db.deleteFrom(LINK).execute();

      // Chain (before Library & Account)
      INSTANCE.db.deleteFrom(CHAIN_LIBRARY).execute();
      INSTANCE.db.deleteFrom(CHAIN).execute();

      // Phase (before Idea)
      INSTANCE.db.deleteFrom(PHASE_MEME).execute();
      INSTANCE.db.deleteFrom(PHASE_CHORD).execute();
      INSTANCE.db.deleteFrom(PHASE).execute();

      // Idea (before Library & Credit)
      INSTANCE.db.deleteFrom(IDEA_MEME).execute();
      INSTANCE.db.deleteFrom(IDEA).execute();

      // Library (before Account)
      INSTANCE.db.deleteFrom(LIBRARY).execute();

      // Account (before User)
      INSTANCE.db.deleteFrom(ACCOUNT_USER).execute();
      INSTANCE.db.deleteFrom(ACCOUNT).execute();

      // User Access Token (before User & User Auth)
      INSTANCE.db.deleteFrom(USER_ACCESS_TOKEN).execute();

      // User (last)
      INSTANCE.db.deleteFrom(USER_AUTH).execute();
      INSTANCE.db.deleteFrom(USER_ROLE).execute();
      INSTANCE.db.deleteFrom(USER).execute();

    } catch (Exception e) {
      INSTANCE.log.error(e.getClass().getName() + ": " + e);
      throw new DatabaseException(e.getClass().getName() + ": " + e);
    }

    INSTANCE.log.info("Did delete all records from integration database.");
  }

  /**
   * Runs on program exit
   */
  private static void shutdown() {
    try {
      INSTANCE.dbConnection.close();
      INSTANCE.log.info("Did close master connection to integration database.");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
