//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.testing;

import io.xj.core.exception.CoreException;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import org.jooq.DSLContext;

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
   Get the master connection to integration database

   @return DSL Context
   */
  DSLContext getDb();
}
