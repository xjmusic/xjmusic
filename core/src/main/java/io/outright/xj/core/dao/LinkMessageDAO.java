// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.link_message.LinkMessage;
import io.outright.xj.core.tables.records.LinkMessageRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface LinkMessageDAO {

  /**
   (TOP-LEVEL ACCESS ONLY)
   Create a new Link Message

   @param access control
   @param entity for the new Account User.
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
   Fetch many linkMessage for all Links in a Chain by id, if accessible

   @param access control
   @param chainId to fetch linkMessages for.
   @return JSONArray of linkMessages.
   @throws Exception on failure
   */
  Result<LinkMessageRecord> readAllInChain(Access access, ULong chainId) throws Exception;

  /**
   (TOP-LEVEL ACCESS ONLY)
   Delete a specified linkMessage

   @param access control
   @param id     of specific linkMessage to delete.
   */
  void delete(Access access, ULong id) throws Exception;

}
