// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.testing;

import io.xj.service.hub.HubException;
import org.jooq.DSLContext;
import org.jooq.Table;

import java.util.Collection;

/**
 An Integration test needs to:
 1. create injector with a Config binding
 2. inject IntegrationTestProvider
 AND
 3. call integrationTestProvider.shutdown()
 */
public interface HubIntegrationTestProvider {

  /**
   Reset redis keys
   */
  void resetRedis() throws HubException;

  /**
   Runs on program exit
   */
  void shutdown();

  /**
   Reset the database before an integration test.
   */
  void reset() throws HubException;

  /**
   Delete all of a given table, if the count of records is > 0

   @param table to delete all of
   */
  void reset(Table<?> table);

  /**
   Insert entity to database

   @param entity to insert
   @return the same entity (for chaining methods)
   */
  <N> N insert(N entity) throws HubException;

  /**
   Batch Insert many entities to the database

   @param entities to insert
   */
  <N> void batchInsert(Collection<N> entities) throws HubException;

  /**
   Get the master connection to integration database

   @return DSL Context
   */
  DSLContext getDSL();
}
