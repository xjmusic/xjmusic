// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.platform_message.PlatformMessage;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface PlatformMessageDAO {

  /**
   (TOP-LEVEL ACCESS ONLY)
   Create a new Platform Message

   @param access control
   @param entity for the new Platform Message
   @return newly readMany record
   */
  PlatformMessage create(Access access, PlatformMessage entity) throws Exception;

  /**
   Fetch one platformMessage if accessible

   @param access control
   @param id     of platformMessage
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  PlatformMessage readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch many platformMessage for one Platform by id, if accessible

   @param access       control
   @param previousDays number of days back to fetch platformMessages for.
   @return JSONArray of platformMessages.
   @throws Exception on failure
   */
  Collection<PlatformMessage> readAllPreviousDays(Access access, Integer previousDays) throws Exception;

  /**
   (TOP-LEVEL ACCESS ONLY)
   Delete a specified platformMessage

   @param access control
   @param id     of specific platformMessage to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;

}
