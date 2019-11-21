// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.Access;
import io.xj.core.dao.DAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.PlatformMessage;

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
