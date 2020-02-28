// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.PlatformMessage;

import java.util.Collection;

public interface PlatformMessageDAO extends DAO<PlatformMessage> {

  /**
   Fetch many platformMessage for one Platform by id, if accessible

   @param access       control
   @param previousDays number of days back to fetch platformMessages for.
   @return JSONArray of platformMessages.
   @throws CoreException on failure
   */
  Collection<PlatformMessage> readAllPreviousDays(Access access, Integer previousDays) throws CoreException;

}
