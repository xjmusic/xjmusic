// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.core.persistence;

import io.xj.lib.core.exception.CoreException;

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
