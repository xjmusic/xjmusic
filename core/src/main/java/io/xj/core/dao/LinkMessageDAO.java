// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.link_message.LinkMessage;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface LinkMessageDAO {

  /**
   (TOP-LEVEL ACCESS ONLY)
   Create a new Link Message

   @param access control
   @param entity for the new Link Message
   @return newly readMany record
   */
  LinkMessage create(Access access, LinkMessage entity) throws Exception;

  /**
   Fetch one linkMessage if accessible

   @param access control
   @param id     of linkMessage
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  LinkMessage readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch many linkMessage for one Link by id, if accessible

   @param access control
   @param linkId to fetch linkMessages for.
   @return JSONArray of linkMessages.
   @throws Exception on failure
   */
  Collection<LinkMessage> readAllInLink(Access access, BigInteger linkId) throws Exception;

  /**
   Fetch many LinkMessage for many links by id

   @return JSONArray of linkMessages.
   @throws Exception on failure
    @param access  control
   @param linkIds to fetch linkMessages for.
   */
  Collection<LinkMessage> readAllInLinks(Access access, Collection<BigInteger> linkIds) throws Exception;

  /**
   (TOP-LEVEL ACCESS ONLY)
   Delete a specified linkMessage

   @param access control
   @param id     of specific linkMessage to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;

}
