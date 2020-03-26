// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.persistence;

import io.xj.service.hub.HubException;

public interface Migration {

  /**
   Migrate the database

   @throws HubException on failure
   */
  void migrate() throws HubException;

  /**
   Validate database has been migrated

   @throws HubException on failure
   */
  void validate() throws HubException;
}
