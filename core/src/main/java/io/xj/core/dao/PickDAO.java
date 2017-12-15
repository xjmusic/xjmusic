// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.pick.Pick;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface PickDAO {
  /**
   Create a new Pick

   @param entity for the new Pick.
   @return newly readMany Pick record.
   */
  Pick create(Access access, Pick entity) throws Exception;

  /**
   Fetch one Pick by id, if accessible

   @param access control
   @param id     to fetch
   @return Pick if found
   @throws Exception on failure
   */
  @Nullable
  Pick readOne(Access access, BigInteger id) throws Exception;

  /**
   Read all Picks that are accessible

   @param access        control
   @param arrangementId to read picks from
   @return result of Pick records
   @throws Exception on failure
   */
  Collection<Pick> readAll(Access access, BigInteger arrangementId) throws Exception;

  /**
   Read all Picks that are accessible, for all arrangements of all choices in a given Link

   @param access control
   @param linkId to get all picks in
   @return result of Pick records
   @throws Exception on failure
   */
  Collection<Pick> readAllInLink(Access access, BigInteger linkId) throws Exception;

  /**
   Update a specified Pick

   @param pickId of specific Pick to update.
   @param entity for the updated Pick.
   */
  void update(Access access, BigInteger pickId, Pick entity) throws Exception;

  /**
   Delete a specified Pick

   @param pickId of specific Pick to delete.
   */
  void delete(Access access, BigInteger pickId) throws Exception;

}
