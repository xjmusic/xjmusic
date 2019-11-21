//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.persistence.sql.migration;

import io.xj.core.exception.CoreException;

public interface Migration {

  /**
   Migrate the database

   @throws CoreException on failure
   */
  void migrate() throws CoreException;

  /**
   Validate database has been migrated

   @throws CoreException on failure
   */
  void validate() throws CoreException;
}
