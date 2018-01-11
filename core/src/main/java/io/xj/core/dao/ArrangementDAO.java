// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.arrangement.Arrangement;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface ArrangementDAO {
  /**
   Create a new Arrangement

   @param entity for the new Arrangement.
   @return newly readMany Arrangement record.
   */
  Arrangement create(Access access, Arrangement entity) throws Exception;

  /**
   Fetch one Arrangement by id, if accessible

   @return Arrangement if found
   @throws Exception on failure
    @param access        control
   @param id to fetch
   */
  @Nullable
  Arrangement readOne(Access access, BigInteger id) throws Exception;

  /**
   Read all Arrangements that are accessible

   @param access   control
   @param choiceId to read all arrangements of
   @return array of arrangements as JSON
   @throws Exception on failure
   */
  Collection<Arrangement> readAll(Access access, BigInteger choiceId) throws Exception;

  /**
   Fetch many arrangement for many Links by id, if accessible

   @return JSONArray of arrangements.
   @throws Exception on failure
    @param access  control
   @param linkIds to fetch arrangements for.
   */
  Collection<Arrangement> readAllInLinks(Access access, Collection<BigInteger> linkIds) throws Exception;

  /**
   Update a specified Arrangement
   * @param id of specific Arrangement to update.
   @param entity        for the updated Arrangement.

   */
  void update(Access access, BigInteger id, Arrangement entity) throws Exception;

  /**
   Delete a specified Arrangement
   * @param arrangementId of specific Arrangement to delete.

   */
  void delete(Access access, BigInteger arrangementId) throws Exception;
}
