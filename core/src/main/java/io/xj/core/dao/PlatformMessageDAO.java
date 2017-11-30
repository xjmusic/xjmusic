// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.platform_message.PlatformMessage;
import io.xj.core.tables.records.PlatformMessageRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.util.List;

public interface PlatformMessageDAO {

  /**
   (TOP-LEVEL ACCESS ONLY)
   Create a new Platform Message

   @param access control
   @param entity for the new Platform Message
   @return newly readMany record
   */
  PlatformMessageRecord create(Access access, PlatformMessage entity) throws Exception;

  /**
   Fetch one platformMessage if accessible

   @param access control
   @param id     of platformMessage
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  PlatformMessageRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many platformMessage for one Platform by id, if accessible

   @param access control
   @param previousDays number of days back to fetch platformMessages for.
   @return JSONArray of platformMessages.
   @throws Exception on failure
   */
  Result<PlatformMessageRecord> readAllPreviousDays(Access access, Integer previousDays) throws Exception;

  /**
   (TOP-LEVEL ACCESS ONLY)
   Delete a specified platformMessage

   @param access control
   @param id     of specific platformMessage to delete.
   */
  void delete(Access access, ULong id) throws Exception;

}
