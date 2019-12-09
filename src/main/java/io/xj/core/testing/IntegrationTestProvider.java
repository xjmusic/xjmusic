// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.testing;

import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
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
public interface IntegrationTestProvider {

  /**
   Flush entire Redis database
   */
  void flushRedis();


  /**
   Runs on program exit
   */
  void shutdown();

  /**
   Reset the database before an integration test.
   */
  void reset() throws CoreException;

  /**
   Delete all of a given table, if the count of records is > 0

   @param table to delete all of
   */
  void reset(Table table);

  /**
   Insert Chain to database

   @param entity to insert
   @return the same chain (for chaining methods)
   */
  <N extends Entity> N insert(N entity) throws CoreException;

  /**
   Batch Insert many entities to the database

   @param entities to insert
   */
  <N extends Entity> void batchInsert(Collection<N> entities) throws CoreException;

  /**
   Get the master connection to integration database

   @return DSL Context
   */
  DSLContext getDb();
}
