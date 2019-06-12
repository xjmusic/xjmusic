//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.persistence.sql.migration;

import io.xj.core.exception.CoreException;

/**
 [#166708597] Instrument model handles all of its own entities
 [#166690830] Program model handles all of its own entities
 */

@FunctionalInterface
public interface LegacyMigration {
  /**
   Migrate legacy entites

   @throws CoreException on failure
   */
  void migrate() throws CoreException;
}
