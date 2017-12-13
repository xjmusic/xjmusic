// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.link_message.LinkMessage;
import io.xj.core.tables.records.LinkMessageRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.util.List;

public interface LinkMessageDAO {

  /**
   (TOP-LEVEL ACCESS ONLY)
   Create a new Link Message

   @param access control
   @param entity for the new Link Message
   @return newly readMany record
   */
  LinkMessageRecord create(Access access, LinkMessage entity) throws Exception;

  /**
   Fetch one linkMessage if accessible

   @param access control
   @param id     of linkMessage
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  LinkMessageRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many linkMessage for one Link by id, if accessible

   @param access control
   @param linkId to fetch linkMessages for.
   @return JSONArray of linkMessages.
   @throws Exception on failure
   */
  Result<LinkMessageRecord> readAllInLink(Access access, ULong linkId) throws Exception;

  /**
   Fetch many LinkMessage for many links by id

   @param access control
   @param linkIds to fetch linkMessages for.
   @return JSONArray of linkMessages.
   @throws Exception on failure
   */
  Result<LinkMessageRecord> readAllInLinks(Access access, List<ULong> linkIds) throws Exception;

  /**
   (TOP-LEVEL ACCESS ONLY)
   Delete a specified linkMessage

   @param access control
   @param id     of specific linkMessage to delete.
   */
  void delete(Access access, ULong id) throws Exception;

}
