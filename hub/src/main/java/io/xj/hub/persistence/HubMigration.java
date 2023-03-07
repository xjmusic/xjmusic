// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.persistence;

public interface HubMigration {

  /**
   * Migrate the database
   *
   * @throws HubPersistenceException on failure
   */
  void migrate() throws HubPersistenceException;

  /**
   * Validate database has been migrated
   *
   * @throws HubPersistenceException on failure
   */
  void validate() throws HubPersistenceException;
}
