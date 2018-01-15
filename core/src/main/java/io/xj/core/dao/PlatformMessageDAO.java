// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.platform_message.PlatformMessage;

import java.util.Collection;

public interface PlatformMessageDAO extends DAO<PlatformMessage> {

  /**
   Fetch many platformMessage for one Platform by id, if accessible

   @param access       control
   @param previousDays number of days back to fetch platformMessages for.
   @return JSONArray of platformMessages.
   @throws Exception on failure
   */
  Collection<PlatformMessage> readAllPreviousDays(Access access, Integer previousDays) throws Exception;

}
