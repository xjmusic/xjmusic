// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.PlatformMessage;

import java.util.Collection;

public interface PlatformMessageDAO extends DAO<PlatformMessage> {

  /**
   Fetch many platformMessage for one Platform by id, if accessible

   @param access       control
   @param previousDays number of days back to fetch platformMessages for.
   @return JSONArray of platformMessages.
   @throws HubException on failure
   */
  Collection<PlatformMessage> readAllPreviousDays(Access access, Integer previousDays) throws HubException;

}
